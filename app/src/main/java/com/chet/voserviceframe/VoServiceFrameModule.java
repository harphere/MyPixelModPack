package com.chet.voserviceframe;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.Locale;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import android.content.SharedPreferences;
import dev.chet.mypixelmodpack.PackRuntime;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class VoServiceFrameModule implements IXposedHookLoadPackage {
    private static final String TAG = "VoServiceFrame";
    private static final String MODULE_PKG = "dev.chet.mypixelmodpack";

    private static final String[] VOWIFI_HINTS = {
            "vowifi", "vo_wifi", "wifi_calling", "wificalling", "wfc", "ims_wifi"
    };
    private static final String[] VOLTE_HINTS = {
            "volte", "vo_lte", "ims_lte", "hd_voice", "hdvoice"
    };

    @Override public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!"com.android.systemui".equals(lpparam.packageName)) return;
        XposedBridge.log(TAG + ": loaded SystemUI; installing VoWiFi/VoLTE ImageView resource hook");

        try {
            XposedHelpers.findAndHookMethod(ImageView.class, "setImageResource", int.class, new XC_MethodHook() {
                @Override protected void beforeHookedMethod(MethodHookParam param) {
                    int resId = (Integer) param.args[0];
                    if (resId == 0) return;

                    ImageView view = (ImageView) param.thisObject;
                    Resources res = view.getResources();
                    String entry;
                    try {
                        entry = res.getResourceEntryName(resId).toLowerCase(Locale.ROOT);
                    } catch (Throwable ignored) {
                        return;
                    }

                    BadgeRenderer.Kind kind = classify(entry);
                    if (kind == null) return;

                    int style = readStyle(kind);
                    Drawable d = new StatusBadgeDrawable(kind, style);
                    view.setImageDrawable(d);
                    param.setResult(null);
                    XposedBridge.log(TAG + ": replaced " + entry + " with " + kind + " style " + style);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": setImageResource hook failed: " + t);
        }
    }

    private static BadgeRenderer.Kind classify(String entry) {
        for (String s : VOWIFI_HINTS) if (entry.contains(s)) return BadgeRenderer.Kind.VOWIFI;
        for (String s : VOLTE_HINTS) if (entry.contains(s)) return BadgeRenderer.Kind.VOLTE;
        return null;
    }

    private static int readStyle(BadgeRenderer.Kind kind) {
        try {
            SharedPreferences p = PackRuntime.preferences(MainActivity.PREFS);
            String key = kind == BadgeRenderer.Kind.VOWIFI ? MainActivity.KEY_VOWIFI : MainActivity.KEY_VOLTE;
            int style = p.getInt(key, IconStyle.SQUARE);
            if (style < 1 || style > 4) return IconStyle.SQUARE;
            return style;
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": prefs read failed: " + t);
            return IconStyle.SQUARE;
        }
    }
}
