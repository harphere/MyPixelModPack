package com.chet.networkactivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(Config.PREFS, 0);
        migrateLegacyDefaults();

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(20);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("PX Network Activity");
        title.setTextSize(24);
        title.setGravity(Gravity.START);
        root.addView(title, matchWrap());

        TextView note = new TextView(this);
        note.setText("Infinity-X: disable the ROM's native Network Monitor. In LSPosed, include System UI in the pack scope. Changes are re-read automatically within a few seconds.");
        note.setTextSize(14);
        note.setPadding(0, dp(8), 0, dp(16));
        root.addView(note, matchWrap());

        CheckBox enabled = new CheckBox(this);
        enabled.setText("Show network activity indicator");
        enabled.setChecked(prefs.getBoolean("enabled", Config.DEFAULT_ENABLED));
        root.addView(enabled, matchWrap());

        CheckBox colorize = new CheckBox(this);
        colorize.setText("Colorize upload/download separately");
        colorize.setChecked(prefs.getBoolean("colorize", Config.DEFAULT_COLORIZE));
        root.addView(colorize, matchWrap());

        EditText up = field("Upload color (#RRGGBB)", prefs.getString("upload_color", Config.DEFAULT_UPLOAD));
        root.addView(up, matchWrap());

        EditText down = field("Download color (#RRGGBB)", prefs.getString("download_color", Config.DEFAULT_DOWNLOAD));
        root.addView(down, matchWrap());

        EditText size = numericField("Text size in sp (4–24)", Float.toString(prefs.getFloat("text_size_sp", Config.DEFAULT_TEXT_SIZE_SP)), true);
        root.addView(size, matchWrap());

        CheckBox oneLine = new CheckBox(this);
        oneLine.setText("Use one-line layout (↑ speed   ↓ speed)");
        oneLine.setChecked(prefs.getBoolean("one_line", Config.DEFAULT_ONE_LINE));
        root.addView(oneLine, matchWrap());

        EditText lineSpacing = numericField("Two-line spacing multiplier (0.60–1.50)", Float.toString(prefs.getFloat("line_spacing", Config.DEFAULT_LINE_SPACING)), true);
        root.addView(lineSpacing, matchWrap());

        EditText arrowGap = numericField("Arrow-to-speed gap in spaces (0–4)", Integer.toString(prefs.getInt("arrow_gap", Config.DEFAULT_ARROW_GAP)), false);
        root.addView(arrowGap, matchWrap());

        EditText entryGap = numericField("One-line upload/download gap in spaces (1–8)", Integer.toString(prefs.getInt("entry_gap", Config.DEFAULT_ENTRY_GAP)), false);
        root.addView(entryGap, matchWrap());

        EditText verticalOffset = signedDecimalField("Vertical offset in dp (-6 to 6; negative moves up)", Float.toString(prefs.getFloat("vertical_offset_dp", Config.DEFAULT_VERTICAL_OFFSET_DP)));
        root.addView(verticalOffset, matchWrap());

        CheckBox hideIdle = new CheckBox(this);
        hideIdle.setText("Hide when both directions are idle");
        hideIdle.setChecked(prefs.getBoolean("hide_idle", Config.DEFAULT_HIDE_IDLE));
        root.addView(hideIdle, matchWrap());

        Button save = new Button(this);
        save.setText("Save settings");
        save.setOnClickListener(v -> {
            String upHex = up.getText().toString().trim();
            String downHex = down.getText().toString().trim();
            try {
                Color.parseColor(upHex);
                Color.parseColor(downHex);
                float textSize = Float.parseFloat(size.getText().toString().trim());
                float spacing = Float.parseFloat(lineSpacing.getText().toString().trim());
                int arrowSpaces = Integer.parseInt(arrowGap.getText().toString().trim());
                int betweenEntries = Integer.parseInt(entryGap.getText().toString().trim());
                float yOffset = Float.parseFloat(verticalOffset.getText().toString().trim());

                if (textSize < 4f || textSize > 24f) throw new IllegalArgumentException();
                if (spacing < 0.60f || spacing > 1.50f) throw new IllegalArgumentException();
                if (arrowSpaces < 0 || arrowSpaces > 4) throw new IllegalArgumentException();
                if (betweenEntries < 1 || betweenEntries > 8) throw new IllegalArgumentException();
                if (yOffset < -6f || yOffset > 6f) throw new IllegalArgumentException();

                prefs.edit()
                        .putBoolean("enabled", enabled.isChecked())
                        .putBoolean("colorize", colorize.isChecked())
                        .putString("upload_color", upHex)
                        .putString("download_color", downHex)
                        .putFloat("text_size_sp", textSize)
                        .putBoolean("one_line", oneLine.isChecked())
                        .putFloat("line_spacing", spacing)
                        .putInt("arrow_gap", arrowSpaces)
                        .putInt("entry_gap", betweenEntries)
                        .putFloat("vertical_offset_dp", yOffset)
                        .putBoolean("hide_idle", hideIdle.isChecked())
                        .apply();
                getContentResolver().notifyChange(android.net.Uri.parse(Config.URI), null);
                Toast.makeText(this, "Saved. SystemUI should pick up the new display settings within about five seconds.", Toast.LENGTH_LONG).show();
            } catch (Throwable e) {
                Toast.makeText(this, "Check colors and numeric ranges shown in each field.", Toast.LENGTH_LONG).show();
            }
        });
        root.addView(save, matchWrap());

        setContentView(scroll);
    }

    private void migrateLegacyDefaults() {
        SharedPreferences.Editor edit = prefs.edit();
        boolean changed = false;

        if (prefs.contains("text_size_sp")
                && Math.abs(prefs.getFloat("text_size_sp", Config.LEGACY_TEXT_SIZE_SP) - Config.LEGACY_TEXT_SIZE_SP) < 0.01f) {
            edit.putFloat("text_size_sp", Config.DEFAULT_TEXT_SIZE_SP);
            changed = true;
        }

        // Alpha1/alpha2 shipped these exact colors. Change only those exact defaults;
        // never overwrite a color the user actually customized.
        String oldUpload = prefs.getString("upload_color", null);
        String oldDownload = prefs.getString("download_color", null);
        if (Config.LEGACY_UPLOAD.equalsIgnoreCase(oldUpload == null ? "" : oldUpload)
                && Config.LEGACY_DOWNLOAD.equalsIgnoreCase(oldDownload == null ? "" : oldDownload)) {
            edit.putString("upload_color", Config.DEFAULT_UPLOAD);
            edit.putString("download_color", Config.DEFAULT_DOWNLOAD);
            changed = true;
        }

        if (changed) edit.apply();
    }

    private EditText field(String hint, String value) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value);
        e.setSingleLine(true);
        e.setPadding(0, dp(10), 0, dp(10));
        return e;
    }

    private EditText numericField(String hint, String value, boolean decimal) {
        EditText e = field(hint, value);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | (decimal ? InputType.TYPE_NUMBER_FLAG_DECIMAL : 0));
        return e;
    }

    private EditText signedDecimalField(String hint, String value) {
        EditText e = field(hint, value);
        e.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        return e;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
