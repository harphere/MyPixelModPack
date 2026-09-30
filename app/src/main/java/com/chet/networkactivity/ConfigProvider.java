package com.chet.networkactivity;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class ConfigProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SharedPreferences p = requireContext().getSharedPreferences(Config.PREFS, 0);
        MatrixCursor c = new MatrixCursor(new String[]{
                "enabled", "colorize", "upload_color", "download_color",
                "text_size_sp", "hide_idle", "idle_threshold",
                "one_line", "line_spacing", "arrow_gap", "entry_gap", "vertical_offset_dp"
        });
        c.addRow(new Object[]{
                p.getBoolean("enabled", Config.DEFAULT_ENABLED) ? 1 : 0,
                p.getBoolean("colorize", Config.DEFAULT_COLORIZE) ? 1 : 0,
                p.getString("upload_color", Config.DEFAULT_UPLOAD),
                p.getString("download_color", Config.DEFAULT_DOWNLOAD),
                p.getFloat("text_size_sp", Config.DEFAULT_TEXT_SIZE_SP),
                p.getBoolean("hide_idle", Config.DEFAULT_HIDE_IDLE) ? 1 : 0,
                p.getLong("idle_threshold", Config.DEFAULT_IDLE_THRESHOLD),
                p.getBoolean("one_line", Config.DEFAULT_ONE_LINE) ? 1 : 0,
                p.getFloat("line_spacing", Config.DEFAULT_LINE_SPACING),
                p.getInt("arrow_gap", Config.DEFAULT_ARROW_GAP),
                p.getInt("entry_gap", Config.DEFAULT_ENTRY_GAP),
                p.getFloat("vertical_offset_dp", Config.DEFAULT_VERTICAL_OFFSET_DP)
        });
        return c;
    }

    @Override public String getType(Uri uri) { return "vnd.android.cursor.item/network-activity-config"; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException("Read-only"); }
    @Override public int delete(Uri uri, String selection, String[] args) { throw new UnsupportedOperationException("Read-only"); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) { throw new UnsupportedOperationException("Read-only"); }
}
