package dev.chet.mypixelmodpack;

import android.content.SharedPreferences;
import de.robv.android.xposed.IXposedHookLoadPackage;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** A small adapter for the two navigation features originally written for API 101. */
public abstract class LegacyModule implements IXposedHookLoadPackage {
    public static final class ModuleLoadedParam {}
    public static final class PackageReadyParam {
        private final XC_LoadPackage.LoadPackageParam source;
        PackageReadyParam(XC_LoadPackage.LoadPackageParam source) { this.source = source; }
        public String getPackageName() { return source.packageName; }
        public ClassLoader getClassLoader() { return source.classLoader; }
    }

    private volatile boolean initialized;
    protected void onModuleLoaded(ModuleLoadedParam param) {}
    protected abstract void onPackageReady(PackageReadyParam param);

    @Override public final void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) throws Throwable {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) { onModuleLoaded(new ModuleLoadedParam()); initialized = true; }
            }
        }
        onPackageReady(new PackageReadyParam(param));
    }

    protected SharedPreferences getRemotePreferences(String name) {
        return PackRuntime.preferences(name);
    }

    protected void log(int level, String tag, String message) { XposedBridge.log(tag + ": " + message); }
    protected void log(int level, String tag, String message, Throwable error) {
        XposedBridge.log(tag + ": " + message); XposedBridge.log(error);
    }

}
