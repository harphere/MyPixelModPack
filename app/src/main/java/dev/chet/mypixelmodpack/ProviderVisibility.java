package dev.chet.mypixelmodpack;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/** Run only in the pack app. URI grants establish provider visibility for host apps. */
public final class ProviderVisibility {
    private ProviderVisibility() {}
    public static final Uri VISIBILITY_URI = Uri.withAppendedPath(PackSettingsProvider.URI, "visibility");
    public record Result(int granted, List<String> failures) {
        public String summary() {
            return "App access refreshed: " + granted + " grants; " + failures.size() + " failures."
                + (failures.isEmpty() ? "" : "\n" + String.join("\n", failures));
        }
    }
    public static Result refresh(Context context) {
        PackageManager pm = context.getPackageManager();
        List<String> packages = new ArrayList<>();
        for (ApplicationInfo app : pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))) {
            // Third-party hosts plus launchable system apps. Scope still controls injection.
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0 || pm.getLaunchIntentForPackage(app.packageName) != null)
                packages.add(app.packageName);
        }
        return grantToPackages(context, packages);
    }
    static Result grantToPackages(Context context, List<String> packages) {
        int granted = 0; List<String> failures = new ArrayList<>();
        for (String pkg : new LinkedHashSet<>(packages)) {
            if (pkg == null || pkg.equals(context.getPackageName())) continue;
            try {
                // Read-only, persistable grant on a non-data URI. The existing exported
                // provider's store allowlist and caller checks continue to govern calls.
                context.grantUriPermission(pkg, VISIBILITY_URI, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                granted++;
            } catch (RuntimeException error) { failures.add(pkg + ": " + error); }
        }
        return new Result(granted, List.copyOf(failures));
    }
}
