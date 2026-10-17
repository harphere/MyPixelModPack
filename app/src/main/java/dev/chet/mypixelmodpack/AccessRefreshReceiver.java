package dev.chet.mypixelmodpack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.List;

/** Refresh visibility grants on lifecycle changes; never change feature switches. */
public final class AccessRefreshReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action) || Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
            String pkg = intent.getData() == null ? null : intent.getData().getSchemeSpecificPart();
            if (pkg == null || pkg.equals(context.getPackageName())) return;
            ProviderVisibility.Result result = ProviderVisibility.grantToPackages(context, List.of(pkg));
            context.getSharedPreferences("access_diagnostics_v2",0).edit()
                    .putString("package_result", System.currentTimeMillis()+" | "+pkg+": "+result.summary()).apply();
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            PendingResult pending = goAsync();
            if (!AccessRefresh.start(context, pending::finish)) pending.finish();
        }
    }
}
