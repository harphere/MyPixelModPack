package dev.chet.mobiletypeframe;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public final class SettingsActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(32), dp(24), dp(24));
        page.setBackgroundColor(Color.rgb(20, 24, 33));

        TextView title = new TextView(this);
        title.setText("MobileTypeFrame");
        title.setTextSize(25);
        title.setTextColor(Color.WHITE);
        page.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Choose your network icon style");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setPadding(0, dp(10), 0, dp(18));
        page.addView(subtitle);

        ImageView preview = new ImageView(this);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(dp(130), dp(100));
        previewParams.gravity = Gravity.CENTER_HORIZONTAL;
        page.addView(preview, previewParams);

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);
        final String[] shapes = { ShapeProvider.SQUARE, ShapeProvider.FILLED,
                ShapeProvider.WAVES, ShapeProvider.OPEN_CORNERS, ShapeProvider.SIDE_WAVES,
                ShapeProvider.ITALIC, ShapeProvider.BOLD_ITALIC };
        final String[] names = { "Square", "Filled", "Waves", "Open Corners",
                "Side Waves", "Italic", "Bold Italic" };
        String current = getSharedPreferences(ShapeProvider.PREFS, 0)
                .getString("shape", ShapeProvider.SQUARE);
        // Migrate the removed Circle choice so the picker always has a selection.
        if (!ShapeProvider.SQUARE.equals(current) && !ShapeProvider.FILLED.equals(current)
                && !ShapeProvider.ITALIC.equals(current)
                && !ShapeProvider.BOLD_ITALIC.equals(current)
                && !ShapeProvider.WAVES.equals(current)
                && !ShapeProvider.OPEN_CORNERS.equals(current)
                && !ShapeProvider.SIDE_WAVES.equals(current)) {
            current = ShapeProvider.SQUARE;
            getSharedPreferences(ShapeProvider.PREFS, 0).edit()
                    .putString("shape", current).apply();
        }
        for (int i = 0; i < shapes.length; i++) {
            RadioButton option = new RadioButton(this);
            option.setId(View.generateViewId());
            option.setText(names[i]);
            option.setTextSize(18);
            option.setTextColor(Color.WHITE);
            option.setTag(shapes[i]);
            option.setPadding(0, dp(10), 0, dp(10));
            group.addView(option);
            if (shapes[i].equals(current)) option.setChecked(true);
        }
        page.addView(group);

        TextView sizeTitle = new TextView(this);
        sizeTitle.setText("5G / 4G / LTE font size");
        sizeTitle.setTextSize(18);
        sizeTitle.setTextColor(Color.WHITE);
        sizeTitle.setPadding(0, dp(18), 0, dp(4));
        page.addView(sizeTitle);

        RadioGroup sizeGroup = new RadioGroup(this);
        int[] sizes = { 100, 125, 150 };
        String[] sizeNames = { "Normal (100%)", "Large (125%)", "Extra large (150%)" };
        int selectedSize = ShapeProvider.validFontPercent(getSharedPreferences(
                ShapeProvider.PREFS, 0).getInt("fontPercent", ShapeProvider.DEFAULT_FONT_PERCENT));
        for (int i = 0; i < sizes.length; i++) {
            RadioButton option = new RadioButton(this);
            option.setId(View.generateViewId());
            option.setText(sizeNames[i]);
            option.setTextSize(18);
            option.setTextColor(Color.WHITE);
            option.setTag(sizes[i]);
            option.setPadding(0, dp(7), 0, dp(7));
            sizeGroup.addView(option);
            if (sizes[i] == selectedSize) option.setChecked(true);
        }
        page.addView(sizeGroup);
        final String[] previewShape = { current };
        final int[] previewSize = { selectedSize };
        showPreview(preview, current, selectedSize);
        group.setOnCheckedChangeListener((buttons, checkedId) -> {
            RadioButton selected = buttons.findViewById(checkedId);
            if (selected == null) return;
            String shape = (String) selected.getTag();
            getSharedPreferences(ShapeProvider.PREFS, 0).edit().putString("shape", shape).apply();
            previewShape[0] = shape;
            showPreview(preview, shape, previewSize[0]);
        });
        sizeGroup.setOnCheckedChangeListener((buttons, checkedId) -> {
            RadioButton selected = buttons.findViewById(checkedId);
            if (selected == null) return;
            int percent = (Integer) selected.getTag();
            getSharedPreferences(ShapeProvider.PREFS, 0).edit()
                    .putInt("fontPercent", percent).apply();
            previewSize[0] = percent;
            showPreview(preview, previewShape[0], percent);
        });

        TextView hint = new TextView(this);
        hint.setText("Reboot after changing the style or font size to refresh the status bar.");
        hint.setTextColor(Color.LTGRAY);
        hint.setTextSize(14);
        hint.setPadding(0, dp(24), 0, 0);
        page.addView(hint);
        ScrollView scroll = new ScrollView(this);
        scroll.addView(page);
        setContentView(scroll);
    }

    private void showPreview(ImageView preview, String shape, int fontPercent) {
        Drawable drawable = new FramedTypeDrawable("5G", getResources().getDisplayMetrics().density,
                shape, fontPercent);
        preview.setImageDrawable(drawable);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
