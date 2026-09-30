package dev.chet.mypixelmodpack;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;

/** The only LSPosed entry. Constructors and settings reads run with a real context. */
public final class PackDispatcher implements IXposedHookLoadPackage, IXposedHookInitPackageResources {
    private static final String PACK = "dev.chet.mypixelmodpack";
    private static final String UI = "com.android.systemui";
    private static final Set<String> LAUNCHERS = Set.of("com.google.android.apps.nexuslauncher", "com.android.launcher3");
    private static final String GBOARD = "com.google.android.inputmethod.latin";
    private final Set<String> started = new HashSet<>();
    private final List<XC_InitPackageResources.InitPackageResourcesParam> pendingResources = new ArrayList<>();
    private dev.chet.mobiletypeframe.MobileTypeFrame mobile;
    private boolean configurationReady;

    @Override public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam load) {
        if (PACK.equals(load.packageName) || "android".equals(load.packageName)) return;
        XposedBridge.log("MyPixelModPack 2.0.0: dispatcher reached " + load.packageName + " process=" + load.processName);
        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                Context ctx = (Context) param.args[0];
                if (!load.packageName.equals(ctx.getPackageName())) return;
                synchronized (PackDispatcher.this) {
                    String key = load.packageName + ":" + load.processName;
                    if (!started.add(key)) return;
                }
                try { start(ctx, load); }
                catch (Throwable error) {
                    XposedBridge.log("MyPixelModPack: dispatcher failed; stock behavior retained");
                    XposedBridge.log(error);
                }
            }
        });
    }
    private void start(Context ctx, XC_LoadPackage.LoadPackageParam load) {
        PackRuntime.attach(ctx, load.packageName, load.processName);
        final SharedPreferences switches;
        try { switches = PackRuntime.preferences("features_v2"); }
        catch (Throwable error) {
            PackRuntime.report("startup", "SETTINGS UNAVAILABLE; no pack hooks installed: " + error);
            return;
        }
        PackRuntime.report("startup", "settings snapshot read; context ready");
        String pkg = load.packageName;
        if (UI.equals(pkg)) {
            install("battery", switches, () -> new dev.chet.batterygradient.BatteryModule().handleLoadPackage(load));
            install("mobile", switches, () -> {
                mobile = new dev.chet.mobiletypeframe.MobileTypeFrame();
                mobile.handleLoadPackage(load);
            });
            install("vo", switches, () -> new com.chet.voserviceframe.VoServiceFrameModule().handleLoadPackage(load));
            install("network", switches, () -> new com.chet.networkactivity.NetworkActivityModule().handleLoadPackage(load));
            install("wake", switches, () -> new com.pixeldt2w.module.DoubleTapWakeHook().handleLoadPackage(load));
            synchronized (this) {
                configurationReady = true;
                for (XC_InitPackageResources.InitPackageResourcesParam resource : pendingResources) applyResources(resource);
                pendingResources.clear();
            }
        }
        if (LAUNCHERS.contains(pkg)) {
            install("cursor", switches, () -> new dev.chet.gboardcursorkeys.CursorModule().handleLoadPackage(load));
            install("opa", switches, () -> new com.chet.pixelopahome.OpaModule().handleLoadPackage(load));
            install("dots", switches, () -> new com.chet.navdotstyle.NavDotModule().handleLoadPackage(load));
        }
        if (GBOARD.equals(pkg)) install("cursor", switches, () -> new dev.chet.gboardcursorkeys.CursorModule().handleLoadPackage(load));
        if (!UI.equals(pkg))
            install("match", switches, () -> new com.chet.navbarmatch.NavBarModule().handleLoadPackage(load));
    }
    private void install(String feature, SharedPreferences switches, FeatureInstallation.Action action) {
        FeatureInstallation.run(feature, switches.getBoolean(feature, false), action, (key, status, error) -> {
            PackRuntime.report(key, status);
            if (error != null) XposedBridge.log(error);
        });
    }
    @Override public synchronized void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam param) {
        if (!UI.equals(param.packageName)) return;
        if (!configurationReady) pendingResources.add(param);
        else applyResources(param);
    }
    private void applyResources(XC_InitPackageResources.InitPackageResourcesParam param) {
        if (mobile == null) return;
        try { mobile.handleInitPackageResources(param); }
        catch (Throwable error) { PackRuntime.report("mobile resources", "FAILED: " + error); XposedBridge.log(error); }
    }
}
