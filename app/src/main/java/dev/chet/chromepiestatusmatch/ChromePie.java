package dev.chet.chromepiestatusmatch;

import android.app.Activity;
import android.content.res.Resources;
import android.view.ViewGroup;
import dev.chet.chromepiestatusmatch.settings.PieSettings;
import dev.chet.mypixelmodpack.PackRuntime;
import dev.chet.mypixelmodpack.R;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Installed by the Pack after context/settings are ready; never in the zygote. */
public final class ChromePie implements IXposedHookLoadPackage {
    static final String PACKAGE_NAME = "dev.chet.mypixelmodpack";
    private static final String CONTROL = "pack_pie_control", PENDING = "pack_pie_pending";
    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam load) {
        XposedHelpers.findAndHookMethod(Activity.class, "onStart", new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam p) {
                Activity activity = (Activity)p.thisObject;
                if (!PieSettings.CHROME_ACTIVITY_CLASSES.contains(activity.getClass().getName())) return;
                if (XposedHelpers.getAdditionalInstanceField(activity, CONTROL) != null
                        || XposedHelpers.getAdditionalInstanceField(activity, PENDING) != null) return;
                ViewGroup container = activity.findViewById(android.R.id.content);
                if (container == null) return;
                Runnable attach = () -> {
                    XposedHelpers.removeAdditionalInstanceField(activity, PENDING);
                    if (activity.isFinishing() || activity.isDestroyed()
                            || XposedHelpers.getAdditionalInstanceField(activity, CONTROL) != null) return;
                    PieControl control = null;
                    try {
                        Resources res = activity.getPackageManager().getResourcesForApplication(PACKAGE_NAME);
                        res.getInteger(R.integer.qc_radius_increment);
                        control = new PieControl(activity, res, PackRuntime.preferences("chromepie_v1"));
                        control.attachToContainer(container);
                        XposedHelpers.setAdditionalInstanceField(activity, CONTROL, control);
                        XposedBridge.log("ChromePieMatch: menu attached in " + activity.getPackageName());
                    } catch (Throwable error) {
                        if (control != null) try { control.destroy(); } catch (Throwable ignored) { }
                        XposedBridge.log("ChromePieMatch: attach failed: " + error);
                    }
                };
                XposedHelpers.setAdditionalInstanceField(activity, PENDING, attach);
                container.postDelayed(attach, 1000);
            }
        });
        XposedHelpers.findAndHookMethod(Activity.class, "onDestroy", new XC_MethodHook() {
            @Override protected void beforeHookedMethod(MethodHookParam p) {
                Activity activity = (Activity)p.thisObject;
                Object pending = XposedHelpers.getAdditionalInstanceField(activity, PENDING);
                ViewGroup container = activity.findViewById(android.R.id.content);
                if (pending instanceof Runnable && container != null) container.removeCallbacks((Runnable)pending);
                XposedHelpers.removeAdditionalInstanceField(activity, PENDING);
                Object control = XposedHelpers.getAdditionalInstanceField(activity, CONTROL);
                if (control instanceof PieControl) {
                    try { ((PieControl)control).destroy(); }
                    catch (Throwable error) { XposedBridge.log("ChromePieMatch: cleanup failed: " + error); }
                    XposedHelpers.removeAdditionalInstanceField(activity, CONTROL);
                }
            }
        });
        XposedBridge.log("ChromePieMatch: activity hooks installed in " + load.packageName);
    }
}
