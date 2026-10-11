package com.chet.voserviceframe;
import dev.chet.mypixelmodpack.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {
    public static final String PREFS = "vo_icons_v2";
    public static final String KEY_VOWIFI = "vowifi_style";
    public static final String KEY_VOLTE = "volte_style";

    private SharedPreferences prefs;
    private IconPreviewView vowifiPreview;
    private IconPreviewView voltePreview;
    private TextView statusText;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        vowifiPreview = findViewById(R.id.vowifiPreview);
        voltePreview = findViewById(R.id.voltePreview);
        statusText = findViewById(R.id.statusText);

        RadioGroup vowifiGroup = findViewById(R.id.vowifiGroup);
        RadioGroup volteGroup = findViewById(R.id.volteGroup);

        int vowifi = prefs.getInt(KEY_VOWIFI, IconStyle.SQUARE);
        int volte = prefs.getInt(KEY_VOLTE, IconStyle.SQUARE);
        checkForStyle(vowifiGroup, vowifi, true);
        checkForStyle(volteGroup, volte, false);
        vowifiPreview.setPreview(BadgeRenderer.Kind.VOWIFI, vowifi);
        voltePreview.setPreview(BadgeRenderer.Kind.VOLTE, volte);

        vowifiGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int style = styleFromId(checkedId);
            prefs.edit().putInt(KEY_VOWIFI, style).apply();
            vowifiPreview.setPreview(BadgeRenderer.Kind.VOWIFI, style);
            statusText.setText("VoWiFi style saved. Restart SystemUI to apply.");
        });

        volteGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int style = styleFromId(checkedId);
            prefs.edit().putInt(KEY_VOLTE, style).apply();
            voltePreview.setPreview(BadgeRenderer.Kind.VOLTE, style);
            statusText.setText("VoLTE style saved. Restart SystemUI to apply.");
        });

        Button restart = findViewById(R.id.restartButton);
        restart.setOnClickListener(v -> restartSystemUi());
    }

    private int styleFromId(int id) {
        if (id == R.id.vowifiV2 || id == R.id.volteV2) return IconStyle.ROUNDED;
        if (id == R.id.vowifiV3 || id == R.id.volteV3) return IconStyle.OPEN_CORNERS;
        if (id == R.id.vowifiV4 || id == R.id.volteV4) return IconStyle.SIGNAL;
        return IconStyle.SQUARE;
    }

    private void checkForStyle(RadioGroup group, int style, boolean wifi) {
        int id;
        if (wifi) {
            id = style == 2 ? R.id.vowifiV2 : style == 3 ? R.id.vowifiV3 : style == 4 ? R.id.vowifiV4 : R.id.vowifiV1;
        } else {
            id = style == 2 ? R.id.volteV2 : style == 3 ? R.id.volteV3 : style == 4 ? R.id.volteV4 : R.id.volteV1;
        }
        group.check(id);
    }

    private void restartSystemUi() {
        try {
            Process p = new ProcessBuilder("su", "-c", "pkill -TERM com.android.systemui").start();
            int rc = p.waitFor();
            statusText.setText(rc == 0 ? "SystemUI restart requested." : "Root command returned " + rc + ". You can reboot instead.");
        } catch (Throwable t) {
            statusText.setText("Could not restart SystemUI: " + t.getClass().getSimpleName() + ". Reboot instead.");
        }
    }
}
