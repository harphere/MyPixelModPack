package dev.chet.mypixelmodpack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import de.robv.android.xposed.XposedBridge;

/** Access happens after Application.attach, with no world-readable file dependency. */
public final class PackRuntime {
    private static Context context;
    private static boolean visibilityRetained;
    private static String packageName, processName;
    private PackRuntime() {}
    static void attach(Context ctx, String pkg, String process) {
        context = ctx; packageName = pkg; processName = process;
    }
    public static SharedPreferences preferences(String store) {
        if (context == null) throw new IllegalStateException("Pack context not ready");
        if (!visibilityRetained) {
            try {
                context.getContentResolver().takePersistableUriPermission(
                    ProviderVisibility.VISIBILITY_URI, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
                visibilityRetained = true;
            } catch (SecurityException ignored) {
                // Existing visible hosts need no grant. Missing grants never replace saved switches.
            }
        }
        final Bundle data;
        try { data = context.getContentResolver().call(PackSettingsProvider.URI, "snapshot", store, null); }
        catch (RuntimeException error) { throw new IllegalStateException("Settings provider unavailable for " + store, error); }
        if (data == null) throw new IllegalStateException("No settings snapshot for " + store);
        return new Snapshot(data);
    }
    static void report(String feature, String status) {
        XposedBridge.log("MyPixelModPack 2.1.4: " + processName + " / " + feature + " / " + status);
        try {
            Bundle extras = new Bundle();
            extras.putString("package", packageName); extras.putString("process", processName);
            extras.putString("feature", feature); extras.putString("status", status);
            context.getContentResolver().call(PackSettingsProvider.URI, "report", null, extras);
        } catch (Throwable error) {
            XposedBridge.log("MyPixelModPack: diagnostic provider unavailable: " + error);
        }
    }
    private static final class Snapshot implements SharedPreferences {
        private final Bundle data;
        Snapshot(Bundle data) { this.data = data; }
        @Override public Map<String, ?> getAll() {
            Map<String, Object> result = new HashMap<>();
            for (String key : data.keySet()) {
                Object value = data.get(key);
                if (value instanceof ArrayList) value = new HashSet<>((ArrayList<?>) value);
                result.put(key, value);
            }
            return result;
        }
        @Override public String getString(String key, String def) { return data.getString(key, def); }
        @Override public Set<String> getStringSet(String key, Set<String> def) {
            ArrayList<String> values = data.getStringArrayList(key);
            return values == null ? def : new HashSet<>(values);
        }
        @Override public boolean getBoolean(String key, boolean def) { return data.getBoolean(key, def); }
        @Override public int getInt(String key, int def) { return data.getInt(key, def); }
        @Override public long getLong(String key, long def) { return data.getLong(key, def); }
        @Override public float getFloat(String key, float def) { return data.getFloat(key, def); }
        @Override public boolean contains(String key) { return data.containsKey(key); }
        @Override public Editor edit() { throw new UnsupportedOperationException("Read-only snapshot"); }
        @Override public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) { }
        @Override public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener l) { }
    }
}
