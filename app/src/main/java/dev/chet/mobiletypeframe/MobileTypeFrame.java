package dev.chet.mobiletypeframe;

import android.content.res.XResources;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Classic LSPosed resource hook. Only resources present in this ROM are replaced. */
public final class MobileTypeFrame implements IXposedHookInitPackageResources, IXposedHookLoadPackage {
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final Uri SETTINGS = Uri.parse("content://dev.chet.mypixelmodpack.mobile.settings");
    private static final String[][] ICONS = {
        {"ic_4g_mobiledata", "4G"},
        {"ic_4g_plus_mobiledata", "4G+"},
        {"ic_lte_mobiledata", "LTE"},
        {"ic_lte_plus_mobiledata", "LTE+"},
        {"ic_5g_mobiledata", "5G"},
        {"ic_5g_plus_mobiledata", "5G+"},
        {"ic_5g_uwb_mobiledata", "5G+"},
        {"ic_5g_uc_mobiledata", "5G+"}
    };

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam param) {
        if (!SYSTEM_UI.equals(param.packageName)) return;
        int replaced = 0;
        for (String[] entry : ICONS) {
            final String resourceName = entry[0];
            final String label = entry[1];
            try {
                if (param.res.getIdentifier(resourceName, "drawable", SYSTEM_UI) == 0) continue;
                param.res.setReplacement(SYSTEM_UI, "drawable", resourceName,
                    new XResources.DrawableLoader() {
                        @Override
                        public Drawable newDrawable(XResources res, int id) {
                            String shape = ShapeProvider.SQUARE;
                            int fontPercent = ShapeProvider.DEFAULT_FONT_PERCENT;
                            try {
                                Object app = XposedHelpers.callStaticMethod(
                                        XposedHelpers.findClass("android.app.ActivityThread", null),
                                        "currentApplication");
                                if (app instanceof Context) {
                                    Bundle settings = selectedSettings((Context) app);
                                    shape = settings.getString("shape", ShapeProvider.SQUARE);
                                    fontPercent = settings.getInt("fontPercent",
                                            ShapeProvider.DEFAULT_FONT_PERCENT);
                                }
                            } catch (Throwable ignored) { }
                            return new FramedTypeDrawable(label, res.getDisplayMetrics().density,
                                    shape, fontPercent);
                        }
                    });
                replaced++;
                XposedBridge.log("MobileTypeFrame: replaced " + resourceName + " with " + label);
            } catch (Throwable error) {
                XposedBridge.log("MobileTypeFrame: skipped " + resourceName + ": " + error);
            }
        }
        XposedBridge.log("MobileTypeFrame: registered " + replaced + " SystemUI resources");
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam param) {
        if (!SYSTEM_UI.equals(param.packageName)) return;
        try {
            Class<?> binder = XposedHelpers.findClass(
                    "com.android.systemui.common.ui.binder.IconViewBinder", param.classLoader);
            XposedBridge.hookAllMethods(binder, "bind", new XC_MethodHook() {
                @Override protected void afterHookedMethod(MethodHookParam hook) {
                    if (hook.args.length < 2 || !(hook.args[1] instanceof ImageView)) return;
                    ImageView view = (ImageView) hook.args[1];
                    if (!isMobileType(view)) return;
                    String label = labelForIcon(hook.args[0], view);
                    if (label == null) return;
                    Bundle settings = selectedSettings(view.getContext());
                    String shape = settings.getString("shape", ShapeProvider.SQUARE);
                    int fontPercent = settings.getInt("fontPercent",
                            ShapeProvider.DEFAULT_FONT_PERCENT);
                    view.setImageDrawable(new FramedTypeDrawable(label,
                            view.getResources().getDisplayMetrics().density, shape, fontPercent));
                    XposedBridge.log("MobileTypeFrame: bound mobile_type " + label + " as " + shape);
                }
            });
            XposedBridge.log("MobileTypeFrame: hooked IconViewBinder.bind");
        } catch (Throwable error) {
            XposedBridge.log("MobileTypeFrame: view hook unavailable: " + error);
        }
    }

    private static boolean isMobileType(ImageView view) {
        try {
            return "mobile_type".equals(view.getResources().getResourceEntryName(view.getId()));
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static String labelForIcon(Object icon, ImageView view) {
        try {
            Object value = XposedHelpers.callMethod(icon, "getRes");
            if (value instanceof Integer) {
                String name = view.getResources().getResourceEntryName((Integer) value);
                for (String[] entry : ICONS) if (entry[0].equals(name)) return entry[1];
                XposedBridge.log("MobileTypeFrame: mobile_type uses drawable " + name);
                if (name.contains("5g")) return name.contains("plus") || name.contains("uw")
                        || name.contains("uc") ? "5G+" : "5G";
                if (name.contains("4g")) return name.contains("plus") ? "4G+" : "4G";
                if (name.contains("lte")) return name.contains("plus") ? "LTE+" : "LTE";
            }
        } catch (Throwable ignored) {
            // Some ROMs use a loaded drawable instead of Icon.Resource.
        }
        CharSequence description = view.getContentDescription();
        if (description == null) return null;
        String name = description.toString().toUpperCase(java.util.Locale.ROOT);
        if (name.contains("5G UW") || name.contains("5G UC")) return "5G+";
        if (name.contains("5G+")) return "5G+";
        if (name.contains("5G")) return "5G";
        if (name.contains("4G+")) return "4G+";
        if (name.contains("4G")) return "4G";
        if (name.contains("LTE+")) return "LTE+";
        if (name.contains("LTE")) return "LTE";
        return null;
    }

    private static Bundle selectedSettings(Context context) {
        try {
            Bundle data = context.getContentResolver().call(SETTINGS, "getShape", null, null);
            if (data != null) return data;
        } catch (Throwable error) {
            XposedBridge.log("MobileTypeFrame: settings unavailable; using square: " + error);
        }
        Bundle defaults = new Bundle();
        defaults.putString("shape", ShapeProvider.SQUARE);
        defaults.putInt("fontPercent", ShapeProvider.DEFAULT_FONT_PERCENT);
        return defaults;
    }
}
