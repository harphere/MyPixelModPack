package com.chet.networkactivity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.TextView;

import java.util.Locale;

final class NetworkActivityView extends TextView {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastRx;
    private long lastTx;
    private long lastTime;

    private boolean enabled = Config.DEFAULT_ENABLED;
    private boolean colorize = Config.DEFAULT_COLORIZE;
    private int uploadColor = Color.parseColor(Config.DEFAULT_UPLOAD);
    private int downloadColor = Color.parseColor(Config.DEFAULT_DOWNLOAD);
    private boolean hideIdle = Config.DEFAULT_HIDE_IDLE;
    private long idleThreshold = Config.DEFAULT_IDLE_THRESHOLD;
    private boolean oneLine = Config.DEFAULT_ONE_LINE;
    private float lineSpacing = Config.DEFAULT_LINE_SPACING;
    private int arrowGap = Config.DEFAULT_ARROW_GAP;
    private int entryGap = Config.DEFAULT_ENTRY_GAP;
    private float verticalOffsetDp = Config.DEFAULT_VERTICAL_OFFSET_DP;
    private int systemTint = Color.WHITE;
    private int ticks;

    NetworkActivityView(Context context) {
        super(context);
        setGravity(Gravity.CENTER);
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
        setIncludeFontPadding(false);
        setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        // Tabular numerals reduce visible jitter as speeds change width/value.
        try { setFontFeatureSettings("tnum"); } catch (Throwable ignored) {}
        setTextSize(Config.DEFAULT_TEXT_SIZE_SP);
        setLineSpacing(0f, Config.DEFAULT_LINE_SPACING);
        setMaxLines(2);
        setMinLines(2);
        int h = dp(3);
        setPadding(h, 0, h, 0);
        readConfig();
    }

    void setSystemTint(int tint) {
        systemTint = tint;
        if (!colorize) setTextColor(systemTint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        lastRx = TrafficStats.getTotalRxBytes();
        lastTx = TrafficStats.getTotalTxBytes();
        lastTime = android.os.SystemClock.elapsedRealtime();
        handler.removeCallbacks(updateRunnable);
        handler.post(updateRunnable);
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(updateRunnable);
        super.onDetachedFromWindow();
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override public void run() {
            updateRates();
            handler.postDelayed(this, 1000L);
        }
    };

    private void updateRates() {
        if ((ticks++ % 5) == 0) readConfig();
        if (!enabled) {
            setVisibility(GONE);
            return;
        }

        long now = android.os.SystemClock.elapsedRealtime();
        long rx = TrafficStats.getTotalRxBytes();
        long tx = TrafficStats.getTotalTxBytes();
        long dt = Math.max(1L, now - lastTime);
        long down = Math.max(0L, (rx - lastRx) * 1000L / dt);
        long up = Math.max(0L, (tx - lastTx) * 1000L / dt);
        lastRx = rx;
        lastTx = tx;
        lastTime = now;

        if (hideIdle && up < idleThreshold && down < idleThreshold) {
            setVisibility(INVISIBLE);
            return;
        }
        setVisibility(VISIBLE);

        String arrowSpace = spaces(arrowGap);
        String upText = "↑" + arrowSpace + formatRate(up);
        String downText = "↓" + arrowSpace + formatRate(down);
        String separator = oneLine ? spaces(entryGap) : "\n";

        SpannableStringBuilder s = new SpannableStringBuilder();
        int upStart = 0;
        s.append(upText);
        int upEnd = s.length();
        s.append(separator);
        int downStart = s.length();
        s.append(downText);
        int downEnd = s.length();

        if (colorize) {
            s.setSpan(new ForegroundColorSpan(uploadColor), upStart, upEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(new ForegroundColorSpan(downloadColor), downStart, downEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            setTextColor(systemTint);
        }
        setText(s);
    }

    private void readConfig() {
        Cursor c = null;
        try {
            c = getContext().getContentResolver().query(Uri.parse(Config.URI), null, null, null, null);
            if (c != null && c.moveToFirst()) {
                enabled = c.getInt(c.getColumnIndexOrThrow("enabled")) != 0;
                colorize = c.getInt(c.getColumnIndexOrThrow("colorize")) != 0;
                uploadColor = Config.safeColor(c.getString(c.getColumnIndexOrThrow("upload_color")), uploadColor);
                downloadColor = Config.safeColor(c.getString(c.getColumnIndexOrThrow("download_color")), downloadColor);
                setTextSize(c.getFloat(c.getColumnIndexOrThrow("text_size_sp")));
                hideIdle = c.getInt(c.getColumnIndexOrThrow("hide_idle")) != 0;
                idleThreshold = c.getLong(c.getColumnIndexOrThrow("idle_threshold"));
                oneLine = c.getInt(c.getColumnIndexOrThrow("one_line")) != 0;
                lineSpacing = c.getFloat(c.getColumnIndexOrThrow("line_spacing"));
                arrowGap = c.getInt(c.getColumnIndexOrThrow("arrow_gap"));
                entryGap = c.getInt(c.getColumnIndexOrThrow("entry_gap"));
                verticalOffsetDp = c.getFloat(c.getColumnIndexOrThrow("vertical_offset_dp"));

                setSingleLine(oneLine);
                setMaxLines(oneLine ? 1 : 2);
                setMinLines(oneLine ? 1 : 2);
                setLineSpacing(0f, lineSpacing);
                setTranslationY(verticalOffsetDp * getResources().getDisplayMetrics().density);
                setGravity(Gravity.CENTER);
                setTextAlignment(TEXT_ALIGNMENT_CENTER);
            }
        } catch (Throwable ignored) {
            // Keep safe defaults if provider access is temporarily unavailable.
        } finally {
            if (c != null) c.close();
        }
    }

    private static String formatRate(long bytesPerSecond) {
        if (bytesPerSecond >= 1024L * 1024L) {
            return String.format(Locale.US, "%.1fM", bytesPerSecond / (1024f * 1024f));
        }
        if (bytesPerSecond >= 1024L) {
            float kb = bytesPerSecond / 1024f;
            return kb >= 100 ? String.format(Locale.US, "%.0fK", kb) : String.format(Locale.US, "%.1fK", kb);
        }
        return bytesPerSecond + "B";
    }

    private static String spaces(int count) {
        int safe = Math.max(0, Math.min(8, count));
        StringBuilder out = new StringBuilder(safe);
        for (int i = 0; i < safe; i++) out.append(' ');
        return out.toString();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
