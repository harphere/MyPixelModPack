package dev.chet.mypixelmodpack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Map;
import java.util.TreeMap;

public final class HomeActivity extends Activity {
    private static final String[][] FEATURES = {
        {"battery", "Battery gradient", "dev.chet.batterygradient.SettingsActivity"},
        {"cursor", "Gboard cursor arrows", "dev.chet.gboardcursorkeys.MainActivity"},
        {"mobile", "4G/5G icon styles", "dev.chet.mobiletypeframe.SettingsActivity"},
        {"vo", "VoLTE / VoWiFi icons", "com.chet.voserviceframe.MainActivity"},
        {"opa", "OPA Home animation", ""},
        {"dots", "Navigation icons", "com.chet.navdotstyle.MainActivity"},
        {"match", "Nav Bar Status Match", "com.chet.navbarmatch.MainActivity"},
        {"network", "Network activity indicator", "com.chet.networkactivity.SettingsActivity"},
        {"wake", "Double Tap 2 Wake", ""}
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SharedPreferences prefs = getSharedPreferences("features_v2", MODE_PRIVATE);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        list.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(this); scroll.addView(list);
        TextView intro = new TextView(this);
        intro.setText("My Pixel Mod Pack 2.0.0\n\nChoose features, disable their matching standalone modules in Vector / LSPosed, and reboot. Every switch starts OFF in this refreshed version. Earlier pack switches are ignored. Changes take effect after reboot.");
        intro.setTextSize(17); list.addView(intro);
        for (String[] feature : FEATURES) {
            CheckBox toggle = new CheckBox(this);
            toggle.setText(feature[1]); toggle.setChecked(prefs.getBoolean(feature[0], false));
            toggle.setOnCheckedChangeListener((v, checked) -> {
                if (!prefs.edit().putBoolean(feature[0], checked).commit())
                    Toast.makeText(this, "Could not save switch; retry before rebooting", Toast.LENGTH_LONG).show();
            });
            list.addView(toggle);
            if (!feature[2].isEmpty()) {
                Button settings = new Button(this); settings.setText(feature[1] + " settings");
                settings.setOnClickListener(v -> startActivity(new Intent().setClassName(this, feature[2])));
                list.addView(settings);
            }
        }
        Button diagnostics = new Button(this); diagnostics.setText("Copy pack startup diagnostics");
        diagnostics.setOnClickListener(v -> {
            StringBuilder text = new StringBuilder("My Pixel Mod Pack 2.0.0\nSaved switches (reboot required):\n");
            for (String[] feature : FEATURES) text.append(feature[0]).append('=').append(prefs.getBoolean(feature[0], false)).append('\n');
            text.append("\nLatest process reports (epoch milliseconds):\n");
            Map<String, ?> reports = new TreeMap<>(getSharedPreferences("diagnostics_v2", 0).getAll());
            if (reports.isEmpty()) text.append("No process reports. Confirm module enabled, scope and reboot.\n");
            for (Map.Entry<String, ?> report : reports.entrySet()) text.append(report.getKey()).append(": ").append(report.getValue()).append('\n');
            ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Pack diagnostics", text));
            Toast.makeText(this, "Diagnostics copied. Also capture Vector logs for detailed hook errors.", Toast.LENGTH_LONG).show();
        });
        list.addView(diagnostics);
        TextView scope = new TextView(this);
        scope.setText("Scope: System UI for status features and DT2W; Gboard plus Pixel Launcher/Launcher3 for arrows; launcher for navigation icons and OPA. Add each target app for Nav Bar Status Match. System Framework is unnecessary. If a test fails, disable the pack and reboot, then restore the matching standalone module.");
        scope.setPadding(0, pad, 0, pad); list.addView(scope); setContentView(scroll);
    }
}
