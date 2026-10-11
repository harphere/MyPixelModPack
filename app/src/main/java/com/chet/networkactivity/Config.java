package com.chet.networkactivity;

import android.graphics.Color;

final class Config {
    static final String PREFS = "network_activity_v2";
    static final String AUTHORITY = "dev.chet.mypixelmodpack.network.settings";
    static final String URI = "content://" + AUTHORITY + "/settings";

    static final boolean DEFAULT_ENABLED = true;
    static final boolean DEFAULT_COLORIZE = true;

    // PixelXpert NetworkTraffic.java defaults: upload RED, download GREEN.
    static final String DEFAULT_UPLOAD = "#FF0000";
    static final String DEFAULT_DOWNLOAD = "#00FF00";
    static final String LEGACY_UPLOAD = "#66BB6A";
    static final String LEGACY_DOWNLOAD = "#42A5F5";

    static final float DEFAULT_TEXT_SIZE_SP = 15.0f;
    static final float LEGACY_TEXT_SIZE_SP = 6.5f;
    static final boolean DEFAULT_HIDE_IDLE = false;
    static final long DEFAULT_IDLE_THRESHOLD = 1024L;

    static final boolean DEFAULT_ONE_LINE = false;
    static final float DEFAULT_LINE_SPACING = 0.85f;
    static final int DEFAULT_ARROW_GAP = 1;
    static final int DEFAULT_ENTRY_GAP = 3;
    static final float DEFAULT_VERTICAL_OFFSET_DP = 0f;

    static int safeColor(String value, int fallback) {
        try {
            return Color.parseColor(value);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private Config() {}
}
