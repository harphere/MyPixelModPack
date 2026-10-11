package com.pixeldt2w.module;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Pixel 8 Pro / Android 16 oriented double-tap-to-wake hook.
 *
 * Two SystemUI wake paths are handled:
 *  1) true screen-off / doze sensor: suppress first REASON_SENSOR_TAP, re-arm
 *     TriggerSensor, then allow the second native sensor event through;
 *  2) pulsing / AoD transition: suppress PulsingGestureListener.onSingleTapUp()
 *     and leave SystemUI's native onDoubleTapEvent() untouched.
 *
 * The implementation is deliberately fail-open for the doze-sensor path.
 */
public final class DoubleTapWakeHook implements IXposedHookLoadPackage {
    private static final String TAG = "PixelDT2W";
    private static final String SYSTEM_UI = "com.android.systemui";

    // com.android.systemui.doze.DozeTriggers.REASON_SENSOR_TAP
    private static final int REASON_SENSOR_TAP = 9;
    private static final long DOUBLE_TAP_WINDOW_MS = 400L;

    private final AtomicBoolean waitingForSecondTap = new AtomicBoolean(false);
    private final AtomicBoolean modernPulsingHookInstalled = new AtomicBoolean(false);
    private final AtomicBoolean legacyPulsingHookInstalled = new AtomicBoolean(false);

    private volatile Object tapTriggerSensor;
    private Handler mainHandler;
    private Runnable resetTapWindow;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!SYSTEM_UI.equals(lpparam.packageName)) {
            return;
        }

        try {
            mainHandler = new Handler(Looper.getMainLooper());
            log("loading in process=" + lpparam.processName);

            installDozeSensorHooks(lpparam.classLoader);
            installModernPulsingGestureHook(lpparam.classLoader);
            installLegacyPulsingCompatibilityHook(lpparam.classLoader);

            log("initial hook installation complete");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": failed to initialize");
            XposedBridge.log(t);
        }
    }

    /**
     * True screen-off / doze-sensor path. This mirrors PixelXpert's DT2W logic:
     * consume tap #1 only after updateListening() succeeds, then allow tap #2.
     */
    private void installDozeSensorHooks(ClassLoader classLoader) {
        Class<?> triggerSensorClass = XposedHelpers.findClassIfExists(
                "com.android.systemui.doze.DozeSensors$TriggerSensor", classLoader);
        Class<?> dozeTriggersClass = XposedHelpers.findClassIfExists(
                "com.android.systemui.doze.DozeTriggers", classLoader);

        log("DozeSensors$TriggerSensor=" + (triggerSensorClass != null));
        log("DozeTriggers=" + (dozeTriggersClass != null));

        if (triggerSensorClass == null || dozeTriggersClass == null) {
            log("doze sensor path unavailable; stock behavior retained for this path");
            return;
        }

        XposedBridge.hookAllConstructors(triggerSensorClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                Integer pulseReason = readPulseReason(param.thisObject);
                if (pulseReason != null && pulseReason == REASON_SENSOR_TAP) {
                    tapTriggerSensor = param.thisObject;
                    log("captured tap TriggerSensor, class="
                            + param.thisObject.getClass().getName());
                }
            }
        });

        int hookedCount = 0;
        for (Method method : dozeTriggersClass.getDeclaredMethods()) {
            if (!"onSensor".equals(method.getName())) {
                continue;
            }
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 0 || (params[0] != int.class && params[0] != Integer.class)) {
                continue;
            }

            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (param.args.length == 0 || !(param.args[0] instanceof Integer)) {
                        return;
                    }
                    if (((Integer) param.args[0]) != REASON_SENSOR_TAP) {
                        return;
                    }

                    if (!waitingForSecondTap.get()) {
                        Object trigger = tapTriggerSensor;

                        // Never swallow the first tap unless we can re-arm the
                        // underlying trigger immediately. This prevents a broken
                        // hook from making the screen impossible to tap-wake.
                        if (trigger == null) {
                            log("tap event received but TriggerSensor not captured; fail-open");
                            return;
                        }
                        if (!rearmTapSensor(trigger)) {
                            log("tap TriggerSensor re-arm failed; fail-open");
                            return;
                        }

                        try {
                            scheduleTapWindowReset();
                            waitingForSecondTap.set(true);
                        } catch (Throwable error) {
                            waitingForSecondTap.set(false);
                            log("tap reset scheduling failed; retaining stock wake: " + error);
                            return;
                        }
                        param.setResult(null);
                        log("first doze tap suppressed; 400 ms window opened");
                    } else {
                        cancelTapWindowReset();
                        waitingForSecondTap.set(false);
                        log("second doze tap accepted; stock SystemUI wake continues");
                    }
                }
            });
            hookedCount++;
        }

        log("DozeTriggers.onSensor hooks installed=" + hookedCount);
    }

    private Integer readPulseReason(Object triggerSensor) {
        try {
            return XposedHelpers.getIntField(triggerSensor, "mPulseReason");
        } catch (Throwable first) {
            try {
                Object value = XposedHelpers.getObjectField(triggerSensor, "mPulseReason");
                return value instanceof Integer ? (Integer) value : null;
            } catch (Throwable second) {
                log("mPulseReason unavailable on " + triggerSensor.getClass().getName());
                return null;
            }
        }
    }

    private boolean rearmTapSensor(Object trigger) {
        try {
            XposedHelpers.callMethod(trigger, "updateListening");
            return true;
        } catch (Throwable t) {
            log("updateListening() failed: " + t.getClass().getSimpleName()
                    + (t.getMessage() == null ? "" : " - " + t.getMessage()));
            return false;
        }
    }

    private void scheduleTapWindowReset() {
        cancelTapWindowReset();
        resetTapWindow = () -> {
            waitingForSecondTap.set(false);
            resetTapWindow = null;
            log("double-tap window expired");
        };
        mainHandler.postDelayed(resetTapWindow, DOUBLE_TAP_WINDOW_MS);
    }

    private void cancelTapWindowReset() {
        Runnable reset = resetTapWindow;
        if (reset != null) {
            mainHandler.removeCallbacks(reset);
            resetTapWindow = null;
        }
    }

    /**
     * Android 16 pulsing/AoD-transition path.
     *
     * AOSP PulsingGestureListener has separate onSingleTapUp() and
     * onDoubleTapEvent() handlers. Its double-tap handler accepts a double tap
     * when either the double-tap setting OR the single-tap setting is enabled.
     * Therefore we only suppress onSingleTapUp(); native onDoubleTapEvent()
     * remains responsible for proximity/falsing checks and waking the device.
     */
    private void installModernPulsingGestureHook(ClassLoader classLoader) {
        Class<?> pulsingClass = XposedHelpers.findClassIfExists(
                "com.android.systemui.shade.PulsingGestureListener", classLoader);

        log("PulsingGestureListener=" + (pulsingClass != null));
        if (pulsingClass == null) {
            return;
        }

        int count = 0;
        for (Method method : pulsingClass.getDeclaredMethods()) {
            if (!"onSingleTapUp".equals(method.getName())) {
                continue;
            }

            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    // GestureDetector.SimpleOnGestureListener.onSingleTapUp()
                    // returns boolean. False means we do not consume/wake on the
                    // single tap; the same GestureDetector can still recognize
                    // and dispatch the subsequent double-tap sequence.
                    param.setResult(false);
                    log("pulsing single tap suppressed");
                }
            });
            count++;
        }

        if (count > 0) {
            modernPulsingHookInstalled.set(true);
        }
        log("modern pulsing onSingleTapUp hooks installed=" + count);

        boolean doubleTapFound = false;
        for (Method method : pulsingClass.getDeclaredMethods()) {
            if ("onDoubleTapEvent".equals(method.getName())) {
                doubleTapFound = true;
                break;
            }
        }
        log("native PulsingGestureListener.onDoubleTapEvent present=" + doubleTapFound);
    }

    /**
     * Older fallback. Some custom-ROM merges may retain the GestureDetector
     * field/listener route even when the directly hookable Kotlin class is absent.
     * We only install this if the modern direct hook was not found.
     */
    private void installLegacyPulsingCompatibilityHook(ClassLoader classLoader) {
        if (modernPulsingHookInstalled.get()) {
            log("legacy pulsing hook not needed; modern direct hook active");
            return;
        }

        Class<?> shadeController = XposedHelpers.findClassIfExists(
                "com.android.systemui.shade.NotificationShadeWindowViewController", classLoader);
        if (shadeController == null) {
            log("NotificationShadeWindowViewController absent; no legacy pulsing fallback");
            return;
        }

        XposedBridge.hookAllConstructors(shadeController, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (legacyPulsingHookInstalled.get()) {
                    return;
                }

                try {
                    Object gestureHandler = XposedHelpers.getObjectField(
                            param.thisObject, "mPulsingWakeupGestureHandler");
                    if (gestureHandler == null) {
                        return;
                    }
                    Object listener = XposedHelpers.getObjectField(gestureHandler, "mListener");
                    if (listener == null) {
                        return;
                    }

                    Method singleTapMethod = findMethodByName(listener.getClass(), "onSingleTapUp");
                    if (singleTapMethod == null) {
                        return;
                    }

                    XposedBridge.hookMethod(singleTapMethod, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult(false);
                            log("legacy pulsing single tap suppressed");
                        }
                    });

                    legacyPulsingHookInstalled.set(true);
                    log("legacy pulsing compatibility hook installed on "
                            + listener.getClass().getName());
                } catch (Throwable t) {
                    log("legacy pulsing hook unavailable: " + t.getClass().getSimpleName());
                }
            }
        });
    }

    private static Method findMethodByName(Class<?> clazz, String name) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (name.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

    private static void log(String message) {
        XposedBridge.log(TAG + ": " + message);
    }
}
