package dev.chet.mobiletypeframe;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/** Exposes only icon appearance settings; no private user data. */
public final class ShapeProvider extends ContentProvider {
    public static final String SQUARE = "square";
    public static final String FILLED = "filled";
    public static final String ITALIC = "italic";
    public static final String BOLD_ITALIC = "bold_italic";
    public static final String WAVES = "waves";
    public static final String OPEN_CORNERS = "open_corners";
    public static final String SIDE_WAVES = "side_waves";
    static final String PREFS = "mobile_type_frame";
    static final int DEFAULT_FONT_PERCENT = 150;

    static int validFontPercent(int percent) {
        return percent == 100 || percent == 125 || percent == 150
                ? percent : DEFAULT_FONT_PERCENT;
    }

    @Override public boolean onCreate() { return true; }

    @Override public Bundle call(String method, String arg, Bundle extras) {
        if (!"getShape".equals(method)) return null;
        Bundle result = new Bundle();
        String shape = getContext().getSharedPreferences(PREFS, 0).getString("shape", SQUARE);
        if (!SQUARE.equals(shape) && !FILLED.equals(shape)
                && !ITALIC.equals(shape) && !BOLD_ITALIC.equals(shape)
                && !WAVES.equals(shape)
                && !OPEN_CORNERS.equals(shape) && !SIDE_WAVES.equals(shape)) {
            shape = SQUARE;
        }
        result.putString("shape", shape);
        result.putInt("fontPercent", validFontPercent(getContext()
                .getSharedPreferences(PREFS, 0)
                .getInt("fontPercent", DEFAULT_FONT_PERCENT)));
        return result;
    }

    @Override public Cursor query(Uri uri, String[] projection, String selection,
                                   String[] selectionArgs, String sortOrder) { return null; }
    @Override public String getType(Uri uri) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection,
                                String[] selectionArgs) { return 0; }
}
