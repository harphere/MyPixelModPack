package dev.chet.mypixelmodpack;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Binder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/** Read-only appearance settings; diagnostics are separate from hook configuration. */
public final class PackSettingsProvider extends ContentProvider {
    public static final Uri URI = Uri.parse("content://dev.chet.mypixelmodpack.settings.v2");
    private static final Set<String> STORES = Set.of("features_v2", "nav_icons_v2", "nav_match_v2", "vo_icons_v2", "chromepie_v1");
    @Override public boolean onCreate() { return true; }
    @Override public Bundle call(String method, String arg, Bundle extras) {
        if ("snapshot".equals(method)) {
            if (!STORES.contains(arg)) throw new IllegalArgumentException("Unknown settings store");
            Bundle result = new Bundle();
            for (Map.Entry<String, ?> entry : getContext().getSharedPreferences(arg, 0).getAll().entrySet()) {
                String key = entry.getKey(); Object value = entry.getValue();
                if (value instanceof Boolean) result.putBoolean(key, (Boolean) value);
                else if (value instanceof String) result.putString(key, (String) value);
                else if (value instanceof Integer) result.putInt(key, (Integer) value);
                else if (value instanceof Long) result.putLong(key, (Long) value);
                else if (value instanceof Float) result.putFloat(key, (Float) value);
                else if (value instanceof Set) {
                    ArrayList<String> strings = new ArrayList<>();
                    for (Object item : (Set<?>) value) if (item instanceof String) strings.add((String) item);
                    result.putStringArrayList(key, strings);
                }
            }
            return result;
        }
        if ("report".equals(method) && extras != null) {
            String pkg = extras.getString("package", "");
            String[] callers = getContext().getPackageManager().getPackagesForUid(Binder.getCallingUid());
            boolean valid = false;
            if (callers != null) for (String caller : callers) if (caller.equals(pkg)) valid = true;
            if (!valid) throw new SecurityException("Caller package mismatch");
            String process = extras.getString("process", pkg);
            String feature = extras.getString("feature", "startup");
            String status = extras.getString("status", "unknown");
            getContext().getSharedPreferences("diagnostics_v2", 0).edit()
                .putString(process + " / " + feature, System.currentTimeMillis() + " | " + status).apply();
            return Bundle.EMPTY;
        }
        throw new IllegalArgumentException("Unknown provider method");
    }
    @Override public Cursor query(Uri u, String[] p, String s, String[] a, String o) { return null; }
    @Override public String getType(Uri u) { return null; }
    @Override public Uri insert(Uri u, ContentValues v) { throw new UnsupportedOperationException(); }
    @Override public int delete(Uri u, String s, String[] a) { throw new UnsupportedOperationException(); }
    @Override public int update(Uri u, ContentValues v, String s, String[] a) { throw new UnsupportedOperationException(); }
}
