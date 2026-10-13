package dev.chet.batterygradient;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Switch;

public final class SettingsActivity extends Activity {
    private final GradientBatteryDrawable preview = new GradientBatteryDrawable();
    private final Handler main = new Handler(Looper.getMainLooper());
    private TextView statusView;
    private ImageView previewView;
    private String previewStyle;
    private SeekBar sizeSlider;
    private TextView sizeReading;
    private RadioGroup orientationGroup;
    private boolean refreshingControls;
    private final Runnable refreshStatus = new Runnable() {
        @Override public void run() {
            if (statusView != null) statusView.setText("System UI: "
                    + SettingsProvider.getStatus(SettingsActivity.this));
            main.postDelayed(this, 2000);
        }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(Color.rgb(248, 248, 247));
        setContentView(root);

        TextView title = text("Battery Gradient", 24);
        root.addView(title);
        TextView detail = text("Filled circle drains from the outside in: green outer edge, yellow, amber and red centre. "
                + "Rectangular battery drains from the top in portrait mode or from the right in landscape mode.", 15);
        detail.setPadding(0, dp(10), 0, dp(16));
        root.addView(detail);

        ImageView icon = new AnimatedBatteryView(this);
        icon.setImageDrawable(preview);
        previewView = icon;
        LinearLayout.LayoutParams iconLayout = new LinearLayout.LayoutParams(dp(144), dp(108));
        iconLayout.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(icon, iconLayout);

        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = battery == null ? 75 : Math.round(100f *
                battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 75) /
                Math.max(1, battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100)));
        preview.setLevelPercent(level);
        int status = battery == null ? 0 : battery.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        preview.setCharging(status == BatteryManager.BATTERY_STATUS_CHARGING
                || (status == BatteryManager.BATTERY_STATUS_FULL && battery != null
                && battery.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0));
        preview.setActivelyCharging(status == BatteryManager.BATTERY_STATUS_CHARGING);
        preview.setShowPercentage(SettingsProvider.getDisplayOptions(this)
                .getBoolean("show_percentage", false));

        TextView reading = text("Preview: " + level + "%", 15);
        reading.setGravity(Gravity.CENTER);
        root.addView(reading);
        SeekBar slider = new SeekBar(this);
        slider.setMax(100);
        slider.setProgress(level);
        root.addView(slider);
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean user) {
                preview.setLevelPercent(value);
                reading.setText("Preview: " + value + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });

        Switch percentage = new Switch(this);
        percentage.setText("Show battery percentage");
        percentage.setChecked(SettingsProvider.getDisplayOptions(this)
                .getBoolean("show_percentage", false));
        percentage.setOnCheckedChangeListener((button, checked) -> {
            getContentResolver().call(SettingsProvider.URI, "set_percentage",
                    String.valueOf(checked), null);
            preview.setShowPercentage(checked);
        });
        root.addView(percentage);
        Switch animation = new Switch(this); animation.setText("Charging animation");
        animation.setChecked(SettingsProvider.getDisplayOptions(this).getBoolean("charging_animation", true));
        preview.setAnimationEnabled(animation.isChecked());
        animation.setOnCheckedChangeListener((button, checked) -> {
            getContentResolver().call(SettingsProvider.URI, "set_animation", String.valueOf(checked), null);
            preview.setAnimationEnabled(checked);
        }); root.addView(animation);
        Switch chargingPreview = new Switch(this); chargingPreview.setText("Preview charging");
        chargingPreview.setChecked(status == BatteryManager.BATTERY_STATUS_CHARGING);
        chargingPreview.setOnCheckedChangeListener((button, checked) -> preview.setCharging(checked));
        root.addView(chargingPreview);

        TextView selector = text("Status bar style", 18);
        selector.setPadding(0, dp(20), 0, dp(8));
        root.addView(selector);
        RadioGroup styles = new RadioGroup(this);
        styles.setOrientation(RadioGroup.VERTICAL);
        String[] names = {"Filled circle (outside in)", "Dashed circle", "Circle", "Rectangular (portrait / landscape)"};
        String[] values = {SettingsProvider.FILLED, SettingsProvider.DASHED,
                SettingsProvider.CIRCLE, SettingsProvider.PORTRAIT};
        String selected = SettingsProvider.getSelectedStyle(this);
        previewStyle = selected;
        for (int i = 0; i < names.length; i++) {
            RadioButton button = new RadioButton(this);
            button.setId(View.generateViewId());
            button.setText(names[i]);
            button.setTextSize(17);
            button.setPadding(0, dp(5), 0, dp(5));
            styles.addView(button);
            if (values[i].equals(selected)) button.setChecked(true);
            final String value = values[i];
            button.setOnClickListener(v -> {
                previewStyle = value;
                preview.setStyle(value);
                refreshSizeControls();
                getContentResolver().call(SettingsProvider.URI, "set", value, null);
            });
        }
        preview.setStyle(selected);
        root.addView(styles);
        sizeReading = text("", 16); root.addView(sizeReading);
        sizeSlider = new SeekBar(this); sizeSlider.setMax(75); root.addView(sizeSlider);
        sizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean user) {
                if (refreshingControls) return;
                int percent = value + 25;
                sizeReading.setText("Size for this style: " + percent + "%");
                preview.setScalePercent(percent);
                if (user) {
                    Bundle options = new Bundle(); options.putInt("percent", percent);
                    getContentResolver().call(SettingsProvider.URI, "set_size", previewStyle, options);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) {
                Bundle options = new Bundle(); options.putInt("percent", bar.getProgress() + 25);
                getContentResolver().call(SettingsProvider.URI, "set_size", previewStyle, options);
            }
        });
        TextView orientationLabel = text("Rectangular orientation", 16); root.addView(orientationLabel);
        orientationGroup = new RadioGroup(this); orientationGroup.setOrientation(RadioGroup.HORIZONTAL);
        for (String orientation : new String[]{"portrait", "landscape"}) {
            RadioButton button = new RadioButton(this); button.setId(View.generateViewId());
            button.setText("portrait".equals(orientation) ? "Portrait" : "Landscape");
            button.setTag(orientation); orientationGroup.addView(button);
            button.setOnClickListener(v -> {
                getContentResolver().call(SettingsProvider.URI, "set_orientation", orientation, null);
                preview.setOrientation(orientation);
            });
        }
        root.addView(orientationGroup);
        refreshSizeControls();
        TextView note = text("Enable Battery gradient in My Pixel Mod Pack and scope the Pack to System UI. "
                + "Restart System UI once after enabling the module. Style changes apply live.", 14);
        note.setPadding(0, dp(24), 0, 0);
        root.addView(note);
        statusView = text("System UI: Checking module status…", 14);
        statusView.setPadding(0, dp(20), 0, 0);
        root.addView(statusView);
    }

    private void refreshSizeControls() {
        if (sizeSlider == null) return;
        Bundle options = SettingsProvider.getDisplayOptions(this);
        int size = options.getInt("size_" + previewStyle, SettingsProvider.defaultSize(previewStyle));
        refreshingControls = true; sizeSlider.setProgress(size - 25); refreshingControls = false;
        sizeReading.setText("Size for this style: " + size + "%");
        preview.setScalePercent(size);
        String orientation = options.getString("orientation", "portrait"); preview.setOrientation(orientation);
        for (int i = 0; i < orientationGroup.getChildCount(); i++) {
            RadioButton button = (RadioButton)orientationGroup.getChildAt(i);
            button.setChecked(orientation.equals(button.getTag()));
            button.setEnabled(SettingsProvider.PORTRAIT.equals(previewStyle));
        }
    }
    @Override protected void onResume() {
        super.onResume();
        main.removeCallbacks(refreshStatus);
        preview.setVisible(true, false);
        main.post(refreshStatus);
    }

    @Override protected void onPause() {
        main.removeCallbacks(refreshStatus);
        preview.setVisible(false, false);
        super.onPause();
    }

    private TextView text(String value, int size) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(Color.rgb(32, 38, 42));
        return view;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
