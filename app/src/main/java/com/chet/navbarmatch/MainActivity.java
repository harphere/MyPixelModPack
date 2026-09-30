package com.chet.navbarmatch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public final class MainActivity extends Activity {
    private LinearLayout appList;
    private TextView serviceStatus;
    private EditText search;
    private SharedPreferences prefs;
    private List<AppItem> apps = new ArrayList<>();

    private record AppItem(String label, String packageName) {}

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(buildUi());
        loadApps();
    }

    @Override protected void onStart() {
        super.onStart();
        prefs = getSharedPreferences("nav_match_v2", MODE_PRIVATE);
        serviceStatus.setText("Settings saved • restart affected apps to apply");
        renderApps();
    }

    private View buildUi() {
        int p = dp(16);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(p, p, p, p);

        TextView title = new TextView(this);
        title.setText("Nav Bar Status Match");
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title);

        TextView intro = new TextView(this);
        intro.setText("Android 16 • 3-button navigation\nMirrors the colour drawn under the foreground app's status bar onto its navigation bar. Apps are enabled by default; tick only apps you want blacklisted.");
        intro.setPadding(0, dp(8), 0, dp(12));
        root.addView(intro);

        serviceStatus = new TextView(this);
        serviceStatus.setText("LSPosed service: connecting…");
        root.addView(serviceStatus);

        Button scope = new Button(this);
        scope.setText("Scope apps manually in Vector / LSPosed");
        scope.setOnClickListener(v -> requestAllScopes());
        root.addView(scope);

        CheckBox enabled = new CheckBox(this);
        enabled.setText("Module enabled");
        enabled.setChecked(true);
        enabled.setOnCheckedChangeListener((b, checked) -> {
            if (prefs != null) prefs.edit().putBoolean("enabled", checked).apply();
        });
        root.addView(enabled);

        search = new EditText(this);
        search.setHint("Search blacklist…");
        search.setSingleLine(true);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) { renderApps(); }
            public void afterTextChanged(Editable e) {}
        });
        root.addView(search);

        ScrollView scroll = new ScrollView(this);
        appList = new LinearLayout(this);
        appList.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(appList);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));
        return root;
    }

    private void loadApps() {
        new Thread(() -> {
            PackageManager pm = getPackageManager();
            List<AppItem> out = new ArrayList<>();
            for (PackageInfo pi : pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))) {
                ApplicationInfo ai = pi.applicationInfo;
                if (ai == null || pi.packageName.equals(getPackageName())) continue;
                if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && pm.getLaunchIntentForPackage(pi.packageName) == null) continue;
                CharSequence labelCs = pm.getApplicationLabel(ai);
                out.add(new AppItem(labelCs == null ? pi.packageName : labelCs.toString(), pi.packageName));
            }
            out.sort(Comparator.comparing(a -> a.label.toLowerCase(Locale.ROOT)));
            apps = out;
            runOnUiThread(this::renderApps);
        }).start();
    }

    private void renderApps() {
        if (appList == null) return;
        appList.removeAllViews();
        Set<String> blacklist = prefs == null ? Collections.emptySet() : prefs.getStringSet("blacklist", Collections.emptySet());
        if (blacklist == null) blacklist = Collections.emptySet();
        String q = search == null ? "" : search.getText().toString().trim().toLowerCase(Locale.ROOT);
        final Set<String> initial = blacklist;
        for (AppItem app : apps) {
            if (!q.isEmpty() && !app.label.toLowerCase(Locale.ROOT).contains(q) && !app.packageName.toLowerCase(Locale.ROOT).contains(q)) continue;
            CheckBox box = new CheckBox(this);
            box.setText(app.label + "\n" + app.packageName);
            box.setChecked(initial.contains(app.packageName));
            box.setOnCheckedChangeListener((b, checked) -> updateBlacklist(app.packageName, checked));
            appList.addView(box);
        }
    }

    private void updateBlacklist(String packageName, boolean blacklisted) {
        if (prefs == null) return;
        Set<String> set = new HashSet<>(prefs.getStringSet("blacklist", Collections.emptySet()));
        if (blacklisted) set.add(packageName); else set.remove(packageName);
        prefs.edit().putStringSet("blacklist", set).apply();
    }

    private void requestAllScopes() {
        Toast.makeText(this, "Grant this pack scope to your apps in LSPosed, then restart them.", Toast.LENGTH_LONG).show();
    }

    private int dp(int v) { return Math.round(v * getResources().getDisplayMetrics().density); }
}
