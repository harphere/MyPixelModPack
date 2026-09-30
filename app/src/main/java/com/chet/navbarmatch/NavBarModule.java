package com.chet.navbarmatch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import dev.chet.mypixelmodpack.LegacyModule;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

/**
 * Android 16 / 3-button navigation LSPosed module.
 * Samples the app surface directly under the status bar and applies a representative
 * colour to the 3-button navigation bar. Blacklisted packages are no-ops.
 */
public final class NavBarModule extends LegacyModule {
    private static final String TAG = "NavBarStatusMatch";
    private static final String MODULE_PACKAGE = "dev.chet.mypixelmodpack";
    private static final String PREFS = "nav_match_v2";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_BLACKLIST = "blacklist";
    private static final long MIN_SAMPLE_INTERVAL_MS = 250L;
    private static final long[] RESUME_RECHECK_DELAYS_MS = { 250L, 700L, 1500L };

    private SharedPreferences prefs;
    private volatile boolean enabled = true;
    private volatile Set<String> blacklist = Collections.emptySet();
    private final WeakHashMap<Activity, ActivityState> states = new WeakHashMap<>();
    private final WeakHashMap<Window, Integer> desiredNavColors = new WeakHashMap<>();
    // Vector instantiates legacy modules during zygote specialization, before
    // the app's main Looper exists. Create this only after an Activity hook runs.
    private volatile Handler main;

    private Handler mainHandler() {
        Handler current = main;
        if (current != null) return current;
        synchronized (this) {
            if (main == null) main = new Handler(Looper.getMainLooper());
            return main;
        }
    }

    private static final class ActivityState {
        long lastSample;
        boolean requestInFlight;
        int lastApplied = Color.TRANSPARENT;
        View.OnLayoutChangeListener layoutListener;
        View navOverlay;
    }

    @Override public void onModuleLoaded(ModuleLoadedParam param) {
        try {
            prefs = getRemotePreferences(PREFS);
            reloadPrefs();
        } catch (Throwable t) {
            log(Log.WARN, TAG, "Remote preferences unavailable; using defaults", t);
        }
    }

    private void reloadPrefs() {
        if (prefs == null) return;
        enabled = prefs.getBoolean(KEY_ENABLED, true);
        Set<String> set = prefs.getStringSet(KEY_BLACKLIST, Collections.emptySet());
        blacklist = set == null ? Collections.emptySet() : Set.copyOf(set);
    }

    @Override public void onPackageReady(PackageReadyParam param) {
        final String pkg = param.getPackageName();
        if (pkg == null || pkg.equals(MODULE_PACKAGE) || "com.android.systemui".equals(pkg)) return;
        try {
            Method onPostResume = Activity.class.getDeclaredMethod("onPostResume");
            XposedBridge.hookMethod(onPostResume, new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam param) {
                    try { attach((Activity) param.thisObject, pkg); }
                    catch (Throwable error) { log(Log.WARN, TAG, "Activity attach failed", error); }
                }
            });

            Method onDestroy = Activity.class.getDeclaredMethod("onDestroy");
            XposedBridge.hookMethod(onDestroy, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    try { detach((Activity) param.thisObject); }
                    catch (Throwable error) { log(Log.WARN, TAG, "Activity detach failed", error); }
                }
            });

            installNavigationBarInterceptors(pkg);
        } catch (Throwable t) {
            log(Log.ERROR, TAG, "Failed to install Activity hooks for " + pkg, t);
        }
    }


    /**
     * Intercept app attempts to repaint the 3-button navigation bar after we have
     * sampled the desired top colour. This is necessary for apps that repeatedly
     * force a white navigation bar from their own edge-to-edge/system-bar code.
     */
    private void installNavigationBarInterceptors(String pkg) {
        try {
            // Window#setNavigationBarColor is abstract; the real implementation is
            // PhoneWindow#setNavigationBarColor. Hook the concrete implementation so
            // late app calls are actually intercepted.
            Class<?> phoneWindow = Class.forName("com.android.internal.policy.PhoneWindow");
            Method setNavColor = phoneWindow.getDeclaredMethod("setNavigationBarColor", int.class);
            setNavColor.setAccessible(true);
            XposedBridge.hookMethod(setNavColor, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                try {
                Window window = (Window) param.thisObject;
                if (!enabled || blacklist.contains(pkg) || window == null) return;

                Integer desired;
                synchronized (desiredNavColors) { desired = desiredNavColors.get(window); }
                if (desired == null) return;

                int requested = (Integer) param.args[0];
                int requestedOpaque = Color.rgb(Color.red(requested), Color.green(requested), Color.blue(requested));
                int desiredOpaque = Color.rgb(Color.red(desired), Color.green(desired), Color.blue(desired));

                // Once a sampled colour exists, keep the nav bar matched even if the
                // app later requests white/black/another colour. Our own call passes
                // desiredOpaque and therefore proceeds unchanged.
                if (colorDistance(requestedOpaque, desiredOpaque) > 3) {
                    param.args[0] = desiredOpaque;
                }
                } catch (Throwable error) { log(Log.WARN, TAG, "Color interception failed", error); }
                }
            });
        } catch (Throwable t) {
            log(Log.WARN, TAG, "Unable to hook Window.setNavigationBarColor for " + pkg, t);
        }

        try {
            Class<?> phoneWindow = Class.forName("com.android.internal.policy.PhoneWindow");
            Method setContrast = phoneWindow.getDeclaredMethod("setNavigationBarContrastEnforced", boolean.class);
            setContrast.setAccessible(true);
            XposedBridge.hookMethod(setContrast, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                try {
                Window window = (Window) param.thisObject;
                Integer desired;
                synchronized (desiredNavColors) { desired = window == null ? null : desiredNavColors.get(window); }
                if (enabled && !blacklist.contains(pkg) && desired != null) {
                    param.args[0] = false;
                }
                } catch (Throwable error) { log(Log.WARN, TAG, "Contrast interception failed", error); }
                }
            });
        } catch (Throwable t) {
            log(Log.DEBUG, TAG, "Unable to hook navigation-bar contrast for " + pkg, t);
        }
    }

    private void attach(Activity activity, String pkg) {
        if (!enabled || blacklist.contains(pkg) || activity.isFinishing()) return;
        ActivityState state;
        synchronized (states) {
            state = states.get(activity);
            if (state == null) {
                state = new ActivityState();
                final ActivityState captured = state;
                state.layoutListener = (v, l, t, r, b, ol, ot, orr, ob) -> scheduleSample(activity, pkg, captured);
                states.put(activity, state);
                activity.getWindow().getDecorView().addOnLayoutChangeListener(state.layoutListener);
            }
        }
        scheduleSample(activity, pkg, state);

        // Some apps (notably ones with their own edge-to-edge/system-bar handling)
        // set the navigation-bar colour again shortly after onPostResume. Re-sample
        // a few times so our match wins after those late app-side updates.
        for (long delay : RESUME_RECHECK_DELAYS_MS) {
            final ActivityState captured = state;
            mainHandler().postDelayed(() -> {
                if (!activity.isFinishing() && !activity.isDestroyed()) {
                    // Force this delayed check to be eligible even if no layout change occurred.
                    captured.lastSample = 0L;
                    sample(activity, pkg, captured);
                }
            }, delay);
        }
    }

    private void detach(Activity activity) {
        if (activity == null) return;
        ActivityState state;
        synchronized (states) { state = states.remove(activity); }
        if (state != null && state.layoutListener != null) {
            try { activity.getWindow().getDecorView().removeOnLayoutChangeListener(state.layoutListener); }
            catch (Throwable ignored) { }
        }
        if (state != null && state.navOverlay != null) {
            try {
                ViewGroup parent = (ViewGroup) state.navOverlay.getParent();
                if (parent != null) parent.removeView(state.navOverlay);
            } catch (Throwable ignored) { }
            state.navOverlay = null;
        }
        try {
            synchronized (desiredNavColors) { desiredNavColors.remove(activity.getWindow()); }
        } catch (Throwable ignored) { }
    }

    private void scheduleSample(Activity activity, String pkg, ActivityState state) {
        if (!enabled || blacklist.contains(pkg)) return;
        long now = SystemClock.uptimeMillis();
        long wait = Math.max(0L, MIN_SAMPLE_INTERVAL_MS - (now - state.lastSample));
        mainHandler().postDelayed(() -> sample(activity, pkg, state), wait);
    }

    private void sample(Activity activity, String pkg, ActivityState state) {
        if (!enabled || blacklist.contains(pkg) || activity.isFinishing() || activity.isDestroyed()) return;
        if (state.requestInFlight) return;
        long now = SystemClock.uptimeMillis();
        if (now - state.lastSample < MIN_SAMPLE_INTERVAL_MS) return;
        state.lastSample = now;

        Window window = activity.getWindow();
        View decor = window.getDecorView();
        if (!decor.isShown() || decor.getWidth() < 2 || decor.getHeight() < 2) return;

        int statusHeight = 0;
        WindowInsets insets = decor.getRootWindowInsets();
        if (insets != null) statusHeight = insets.getInsets(WindowInsets.Type.statusBars()).top;
        float density = decor.getResources().getDisplayMetrics().density;
        if (statusHeight <= 0) statusHeight = Math.max(1, (int) (24 * density));

        // Android 15/16 apps frequently report a transparent or otherwise misleading
        // statusBarColor. Some non-edge-to-edge apps can also make PixelCopy return
        // the decor background (often white) for the system-bar portion even while
        // the visible status bar is coloured to match the toolbar. Sample both the
        // status-bar area and a short strip immediately below it, then compare them.
        int belowHeight = Math.max(1, (int) (32 * density));
        int srcBottom = Math.min(decor.getHeight(), statusHeight + belowHeight);
        if (srcBottom <= 1) {
            applyFallback(window, state);
            return;
        }

        int dstWidth = Math.min(decor.getWidth(), 360);
        int dstHeight = Math.min(srcBottom, Math.max(8, (int) (56 * density)));
        Bitmap bitmap = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        Rect src = new Rect(0, 0, decor.getWidth(), srcBottom);
        state.requestInFlight = true;

        final int capturedStatusHeight = statusHeight;
        final int capturedSrcBottom = srcBottom;
        try {
            PixelCopy.request(window, src, bitmap, copyResult -> {
                state.requestInFlight = false;
                try {
                    if (copyResult == PixelCopy.SUCCESS && enabled && !blacklist.contains(pkg)) {
                        int splitY = Math.max(1, Math.min(bitmap.getHeight() - 1,
                                Math.round(bitmap.getHeight() * (capturedStatusHeight / (float) capturedSrcBottom))));

                        int upper = representativeColor(bitmap, 0, splitY);
                        int lower = representativeColor(bitmap, splitY, bitmap.getHeight());
                        int chosen = chooseTopColor(upper, lower);
                        applyNavigationColor(window, state, chosen);
                    } else {
                        applyFallback(window, state);
                    }
                } finally {
                    bitmap.recycle();
                }
            }, mainHandler());
        } catch (Throwable t) {
            state.requestInFlight = false;
            bitmap.recycle();
            applyFallback(window, state);
        }
    }

    /**
     * Returns a dominant-ish colour for a rectangular horizontal band. A small
     * quantized histogram is more resistant to status icons, text and antialiasing
     * than a straight RGB average.
     */
    private int representativeColor(Bitmap bitmap, int top, int bottom) {
        top = Math.max(0, top);
        bottom = Math.min(bitmap.getHeight(), bottom);
        if (bottom <= top) return Color.BLACK;

        // 5 bits per channel = 32,768 buckets. Allocate lazily per sample; the
        // bitmap itself is already small and sampling is throttled.
        int[] counts = new int[32 * 32 * 32];
        long[] sumR = new long[counts.length];
        long[] sumG = new long[counts.length];
        long[] sumB = new long[counts.length];

        int stepX = Math.max(1, bitmap.getWidth() / 120);
        int stepY = Math.max(1, (bottom - top) / 24);
        int bestBucket = -1;
        int bestCount = 0;

        for (int y = top; y < bottom; y += stepY) {
            for (int x = 0; x < bitmap.getWidth(); x += stepX) {
                int c = bitmap.getPixel(x, y);
                if (Color.alpha(c) < 200) continue;
                int r = Color.red(c), g = Color.green(c), b = Color.blue(c);
                int bucket = ((r >> 3) << 10) | ((g >> 3) << 5) | (b >> 3);
                int n = ++counts[bucket];
                sumR[bucket] += r;
                sumG[bucket] += g;
                sumB[bucket] += b;
                if (n > bestCount) {
                    bestCount = n;
                    bestBucket = bucket;
                }
            }
        }

        if (bestBucket < 0 || bestCount == 0) return Color.BLACK;
        return Color.rgb((int) (sumR[bestBucket] / bestCount),
                (int) (sumG[bestBucket] / bestCount),
                (int) (sumB[bestBucket] / bestCount));
    }

    /**
     * Prefer what PixelCopy sees in the status-bar region, except for the common
     * Android 15/16 failure mode where that region comes back white/neutral while
     * the adjacent app bar is clearly coloured.
     */
    private int chooseTopColor(int upper, int lower) {
        int distance = colorDistance(upper, lower);
        if (distance < 42) return upper;

        int upperChroma = chroma(upper);
        int lowerChroma = chroma(lower);
        int upperLum = luminance(upper);

        boolean upperLooksLikeDecorWhite = upperLum >= 224 && upperChroma <= 18;
        boolean lowerClearlyColored = lowerChroma >= 28;

        if (upperLooksLikeDecorWhite && lowerClearlyColored) return lower;

        // Also catch pale gray decor backgrounds where the toolbar is strongly
        // chromatic, while avoiding needless changes for genuinely neutral UIs.
        if (upperChroma <= 12 && lowerChroma >= 48 && lowerChroma >= upperChroma + 32) {
            return lower;
        }

        return upper;
    }

    private int chroma(int c) {
        int r = Color.red(c), g = Color.green(c), b = Color.blue(c);
        return Math.max(r, Math.max(g, b)) - Math.min(r, Math.min(g, b));
    }

    private int luminance(int c) {
        return (299 * Color.red(c) + 587 * Color.green(c) + 114 * Color.blue(c)) / 1000;
    }

    private int colorDistance(int a, int b) {
        int dr = Color.red(a) - Color.red(b);
        int dg = Color.green(a) - Color.green(b);
        int db = Color.blue(a) - Color.blue(b);
        return (int) Math.sqrt(dr * dr + dg * dg + db * db);
    }

    @SuppressWarnings("deprecation")
    private Integer getExplicitStatusBarColor(Window window) {
        try {
            int c = window.getStatusBarColor();
            // Transparent status bars are normal for edge-to-edge apps on Android 15/16.
            // Treat only substantially opaque colours as intentional status-bar colours.
            if (Color.alpha(c) >= 200) {
                return Color.rgb(Color.red(c), Color.green(c), Color.blue(c));
            }
        } catch (Throwable ignored) { }
        return null;
    }

    @SuppressWarnings("deprecation")
    private void applyFallback(Window window, ActivityState state) {
        Integer c = getExplicitStatusBarColor(window);
        if (c != null) applyNavigationColor(window, state, c);
    }

    @SuppressWarnings("deprecation")
    private void applyNavigationColor(Window window, ActivityState state, int color) {
        int desired = Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
        synchronized (desiredNavColors) { desiredNavColors.put(window, desired); }
        try {
            // First use the legacy 3-button API where it still works. Android 15/16
            // continue to honor this for button navigation, but modern AndroidX
            // edge-to-edge code may draw its own protection layer on top of it.
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.setNavigationBarColor(desired);
            window.setNavigationBarContrastEnforced(false);

            // Keep the real system appearance signal synchronized with the
            // background we apply. Black glyphs are requested only for a truly
            // white/near-white navigation bar; every coloured or dark background
            // requests white glyphs. Launcher3/Quickstep consumes this signal.
            updateNavigationIconAppearance(window, desired);

            // Android 15+ guidance is to draw a background behind the navigation /
            // tappable inset instead of relying on navigationBarColor. Do exactly
            // that as a second path. This also wins over AndroidX ProtectionLayout /
            // ColorProtection scrims that can otherwise remain white.
            updateBottomProtectionOverlay(window, state, desired);
            state.lastApplied = desired;
        } catch (Throwable t) {
            log(Log.DEBUG, TAG, "Unable to apply nav colour", t);
        }
    }

    @SuppressWarnings("deprecation")
    private void updateNavigationIconAppearance(Window window, int background) {
        final int mask = WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
        final int appearance = isWhiteBackground(background) ? mask : 0;
        try {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) controller.setSystemBarsAppearance(appearance, mask);
        } catch (Throwable ignored) { }

        // Legacy flag fallback for ROM components that still consume decor flags.
        try {
            View decor = window.getDecorView();
            int flags = decor.getSystemUiVisibility();
            if (appearance != 0) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            else flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            decor.setSystemUiVisibility(flags);
        } catch (Throwable ignored) { }
    }

    private boolean isWhiteBackground(int color) {
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));
        return min >= 250 && max - min <= 5;
    }

    private void updateBottomProtectionOverlay(Window window, ActivityState state, int color) {
        try {
            View decor = window.getDecorView();
            if (!(decor instanceof ViewGroup)) return;

            WindowInsets insets = decor.getRootWindowInsets();
            if (insets == null) return;

            // Android documentation recommends tappableElement to identify the
            // 3-button navigation region. In gesture navigation this bottom inset
            // is normally zero, so do not inject an overlay there.
            int navHeight = insets.getInsets(WindowInsets.Type.tappableElement()).bottom;
            if (navHeight <= 0) {
                if (state.navOverlay != null) {
                    ViewGroup parent = (ViewGroup) state.navOverlay.getParent();
                    if (parent != null) parent.removeView(state.navOverlay);
                    state.navOverlay = null;
                }
                return;
            }

            ViewGroup root = (ViewGroup) decor;
            View overlay = state.navOverlay;
            if (overlay == null || overlay.getParent() != root) {
                if (overlay != null) {
                    try {
                        ViewGroup oldParent = (ViewGroup) overlay.getParent();
                        if (oldParent != null) oldParent.removeView(overlay);
                    } catch (Throwable ignored) { }
                }
                overlay = new View(decor.getContext());
                overlay.setClickable(false);
                overlay.setFocusable(false);
                overlay.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                overlay.setTag("NavBarStatusMatchOverlay");
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, navHeight, Gravity.BOTTOM);
                root.addView(overlay, lp);
                state.navOverlay = overlay;
            } else {
                ViewGroup.LayoutParams current = overlay.getLayoutParams();
                if (current.height != navHeight) {
                    current.height = navHeight;
                    overlay.setLayoutParams(current);
                }
            }

            overlay.setBackgroundColor(color);
            // Edge-to-edge libraries may add their own protection view after ours.
            // Repeated sampling/rechecks call this method again; bring ours to front
            // each time so the visible 3-button background is the matched colour.
            overlay.bringToFront();
            overlay.setVisibility(View.VISIBLE);
        } catch (Throwable t) {
            log(Log.DEBUG, TAG, "Unable to draw bottom navigation protection", t);
        }
    }
}
