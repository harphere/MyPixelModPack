package dev.chet.mypixelmodpack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserManager;

/** Credential-protected module settings may be unavailable during direct boot. */
final class UnlockRetry {
    static boolean waitForUnlock(Context context, Runnable retry) {
        UserManager user = context.getSystemService(UserManager.class);
        if (user == null || user.isUserUnlocked()) return false;
        BroadcastReceiver receiver = new BroadcastReceiver() {
            private boolean delivered;
            @Override public void onReceive(Context ctx, Intent intent) {
                if (!Intent.ACTION_USER_UNLOCKED.equals(intent.getAction()) || delivered) return;
                delivered = true;
                ctx.unregisterReceiver(this);
                retry.run();
            }
        };
        context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_USER_UNLOCKED), Context.RECEIVER_NOT_EXPORTED);
        // Cover unlocking between the first check and receiver registration.
        if (user.isUserUnlocked()) context.getMainExecutor().execute(() ->
            receiver.onReceive(context, new Intent(Intent.ACTION_USER_UNLOCKED)));
        return true;
    }
    private UnlockRetry() { }
}
