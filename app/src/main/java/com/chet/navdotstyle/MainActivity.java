package com.chet.navdotstyle;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;


public final class MainActivity extends Activity {
    private SharedPreferences prefs;
    private TextView status;
    private RadioGroup choices;
    private PreviewView preview;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(buildUi());
    }

    @Override protected void onStart() {
        super.onStart();
        prefs = getSharedPreferences("nav_icons_v2", MODE_PRIVATE);
        status.setText("Settings saved • restart Launcher3 or reboot to apply");
        select(prefs.getString("style", "mixed"));
    }

    private View buildUi() {
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(0xFFF7F7FA);

        TextView title = new TextView(this);
        title.setText("Nav Dot Style");
        title.setTextSize(28);
        title.setTextColor(0xFF15151A);
        title.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        root.addView(title);

        TextView description = new TextView(this);
        description.setText("Replace Android's three navigation icons with a clean, minimal set.");
        description.setTextSize(16);
        description.setTextColor(0xFF55555F);
        description.setPadding(0, dp(8), 0, dp(18));
        root.addView(description);

        preview = new PreviewView();
        root.addView(preview, new LinearLayout.LayoutParams(-1, dp(94)));

        choices = new RadioGroup(this);
        choices.setOrientation(RadioGroup.VERTICAL);
        addChoice("Three small dots", "dots", 1001);
        addChoice("Three short hashmarks", "marks", 1002);
        addChoice("Small • large • small dots", "mixed", 1003);
        choices.setOnCheckedChangeListener((group, id) -> {
            String style = id == 1001 ? "dots" : id == 1002 ? "marks" : "mixed";
            preview.style = style;
            preview.invalidate();
            if (prefs != null) prefs.edit().putString("style", style).apply();
        });
        root.addView(choices);

        Button scopes = new Button(this);
        scopes.setText("Grant required LSPosed scopes");
        scopes.setOnClickListener(v -> requestScopes());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, -2);
        buttonParams.topMargin = dp(18);
        root.addView(scopes, buttonParams);

        status = new TextView(this);
        status.setText("Connecting to LSPosed…");
        status.setTextColor(0xFF55555F);
        status.setPadding(0, dp(14), 0, 0);
        root.addView(status);

        TextView note = new TextView(this);
        note.setText("Designed for Android 16 three-button navigation. If the icons do not change immediately after first setup, restart Launcher3 or reboot once.");
        note.setTextColor(0xFF777781);
        note.setPadding(0, dp(12), 0, 0);
        root.addView(note);

        select("mixed");
        return root;
    }

    private void addChoice(String label, String value, int id) {
        RadioButton button = new RadioButton(this);
        button.setId(id);
        button.setText(label);
        button.setTextSize(17);
        button.setTag(value);
        button.setPadding(0, dp(7), 0, dp(7));
        choices.addView(button);
    }

    private void select(String style) {
        int id = "dots".equals(style) ? 1001 : "marks".equals(style) ? 1002 : 1003;
        choices.check(id);
        preview.style = style;
        preview.invalidate();
    }

    private void requestScopes() {
        Toast.makeText(this, "In LSPosed, scope this pack to Pixel Launcher (or Launcher3) and SystemUI.", Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private final class PreviewView extends View {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        String style = "mixed";

        PreviewView() {
            super(MainActivity.this);
            paint.setColor(0xFF25252C);
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRoundRect(new RectF(0, dp(8), getWidth(), getHeight() - dp(8)),
                    dp(18), dp(18), backgroundPaint());
            float cy = getHeight() / 2f;
            float[] x = {getWidth() * .22f, getWidth() * .5f, getWidth() * .78f};
            for (int i = 0; i < 3; i++) {
                if ("marks".equals(style)) {
                    float w = dp(14), h = dp(3);
                    canvas.drawRoundRect(x[i] - w / 2, cy - h / 2, x[i] + w / 2,
                            cy + h / 2, h / 2, h / 2, paint);
                } else {
                    float radius = "mixed".equals(style) && i == 1 ? dp(5) : dp(2.5f);
                    canvas.drawCircle(x[i], cy, radius, paint);
                }
            }
        }

        private Paint backgroundPaint() {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(Color.WHITE);
            return p;
        }
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
