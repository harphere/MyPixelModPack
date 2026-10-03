package dev.chet.batterygradient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Tasker entry point; accepts only five predefined cosmetic style names. */
public final class StyleIntentReceiver extends BroadcastReceiver {
    public static final String ACTION = "dev.chet.batterygradient.SET_STYLE";

    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null || !ACTION.equals(intent.getAction())) return;
        String style = intent.getStringExtra("style");
        if (!SettingsProvider.FILLED.equals(style)
                && !SettingsProvider.DASHED.equals(style)
                && !SettingsProvider.CIRCLE.equals(style)
                && !SettingsProvider.PORTRAIT.equals(style)
                && !SettingsProvider.DEFAULT.equals(style)) return;
        context.getContentResolver().call(SettingsProvider.URI, "override", style, null);
    }
}
