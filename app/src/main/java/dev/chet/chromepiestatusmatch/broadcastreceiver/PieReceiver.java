package dev.chet.chromepiestatusmatch.broadcastreceiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.lang.reflect.Method;

public class PieReceiver extends BroadcastReceiver {

    public static final String FULLSCREEN_UPDATED_INTENT = "dev.chet.chromepiestatusmatch.intent.FULLSCREEN_UPDATED";
    public static final String EXPAND_NOTIFICATIONS_INTENT = "dev.chet.chromepiestatusmatch.intent.EXPAND_NOTIFICATIONS";

    // StatusBarManager is a hidden platform service accessed by the original pie action.
    @SuppressLint("WrongConstant")
    @SuppressWarnings("deprecation")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!context.getSharedPreferences("features_v2", 0).getBoolean("pie", false) || intent.getAction() == null) return;
        if (intent.getAction().equals(FULLSCREEN_UPDATED_INTENT)) {
            SharedPreferences prefs = context.getSharedPreferences(
                    "chromepie_v1", dev.chet.chromepiestatusmatch.PreferenceAccess.mode(context));
            boolean isFullscreen = intent.getBooleanExtra("IS_FULLSCREEN", false);
            prefs.edit().putBoolean("launch_in_fullscreen", isFullscreen).apply();
        } else if (intent.getAction().equals(EXPAND_NOTIFICATIONS_INTENT)) {
            try {
                Object statusBarService = context.getSystemService("statusbar");
                Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
                Method expand;
                if (Build.VERSION.SDK_INT >= 17) {
                    expand = statusBarManager.getMethod("expandNotificationsPanel");
                } else {
                    expand = statusBarManager.getMethod("expand");
                }
                expand.invoke(statusBarService);
            } catch (Throwable t) {

            }
        }
    }

}
