package dev.chet.mypixelmodpack;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Optional Settings homepage row. Uses the host's preference classes and layout. */
public final class SettingsShortcutModule {
    private static final String KEY="my_pixel_mod_pack_shortcut";
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam load) throws NoSuchMethodException {
        Class<?> home=XposedHelpers.findClass("com.android.settings.homepage.TopLevelSettings",load.classLoader);
        XC_MethodHook callback=new XC_MethodHook() {
            @Override protected void afterHookedMethod(MethodHookParam param) {
                try { if(home.isInstance(param.thisObject)) update(param.thisObject,load.classLoader); }
                catch(Throwable error) {
                    PackRuntime.report("settings_entry","SHORTCUT UNAVAILABLE: "+error);
                    XposedBridge.log("MyPixelModPack: Settings shortcut failed: "+error);
                }
            }
        };
        // Hook the resolved implementation even if a ROM moves it into a superclass.
        XposedBridge.hookMethod(resolve(home,"onCreatePreferences",Bundle.class,String.class),callback);
        XposedBridge.hookMethod(resolve(home,"onStart"),callback);
    }
    private static java.lang.reflect.Method resolve(Class<?> type,String name,Class<?>... args) throws NoSuchMethodException {
        for(Class<?> c=type;c!=null;c=c.getSuperclass()) {
            try { java.lang.reflect.Method m=c.getDeclaredMethod(name,args); m.setAccessible(true); return m; }
            catch(NoSuchMethodException ignored) {}
        }
        throw new NoSuchMethodException(name);
    }
    private void update(Object fragment,ClassLoader loader) throws Exception {
        Context ctx=(Context)XposedHelpers.callMethod(fragment,"getContext");
        Object screen=XposedHelpers.callMethod(fragment,"getPreferenceScreen");
        if(ctx==null || screen==null) return;
        Object existing=XposedHelpers.callMethod(screen,"findPreference",KEY);
        boolean enabled=PackRuntime.preferences("features_v2").getBoolean("settings_entry",false);
        if(!enabled) { if(existing!=null) XposedHelpers.callMethod(screen,"removePreference",existing); return; }
        if(existing!=null) return;
        Class<?> pref=XposedHelpers.findClassIfExists("com.android.settings.widget.HomepagePreference",loader);
        if(pref==null) pref=XposedHelpers.findClass("androidx.preference.Preference",loader);
        Object row=XposedHelpers.newInstance(pref,ctx);
        XposedHelpers.callMethod(row,"setKey",KEY);
        XposedHelpers.callMethod(row,"setTitle","My Pixel Mod Pack");
        XposedHelpers.callMethod(row,"setSummary","Status bar, navigation and wake customizations");
        XposedHelpers.callMethod(row,"setPersistent",false);
        XposedHelpers.callMethod(row,"setOrder",1000);
        Intent launch=new Intent().setClassName("dev.chet.mypixelmodpack","dev.chet.mypixelmodpack.HomeActivity");
        XposedHelpers.callMethod(row,"setIntent",launch);
        try {
            Drawable icon=ctx.getPackageManager().getApplicationIcon("dev.chet.mypixelmodpack");
            XposedHelpers.callMethod(row,"setIcon",icon);
        } catch(Exception ignored) { /* An unavailable icon must not block the shortcut. */ }
        boolean added=(Boolean)XposedHelpers.callMethod(screen,"addPreference",row);
        PackRuntime.report("settings_entry",added ? "Settings home shortcut added" : "Settings rejected shortcut row");
    }
}
