package dev.chet.batterygradient;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/** Original vector drawing; level is mapped continuously from red to amber to green. */
public final class GradientBatteryDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int level = 75;
    private boolean charging;
    private String style = SettingsProvider.FILLED;

    public void setLevelPercent(int value) {
        level = Math.max(0, Math.min(100, value));
        invalidateSelf();
    }
    public void setCharging(boolean value) { charging = value; invalidateSelf(); }
    public void setStyle(String value) { style = value; invalidateSelf(); }

    public static int levelColor(int level) {
        int red = Color.rgb(229, 59, 59);
        int amber = Color.rgb(246, 176, 35);
        int green = Color.rgb(47, 192, 96);
        if (level <= 50) return mix(red, amber, Math.max(0, level) / 50f);
        return mix(amber, green, Math.min(100, level) / 50f - 1f);
    }

    private static int mix(int a, int b, float t) {
        return Color.rgb(Math.round(Color.red(a) + (Color.red(b) - Color.red(a)) * t),
                Math.round(Color.green(a) + (Color.green(b) - Color.green(a)) * t),
                Math.round(Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t));
    }

    @Override public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) return;
        int saved = canvas.save();
        canvas.translate(bounds.left, bounds.top);
        float scale = Math.min(bounds.width(), bounds.height()) / 24f;
        canvas.translate((bounds.width() - 24f * scale) / 2f,
                (bounds.height() - 24f * scale) / 2f);
        canvas.scale(scale, scale);
        int color = levelColor(level);
        RectF circle = new RectF(2.5f, 2.5f, 21.5f, 21.5f);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(SettingsProvider.DASHED.equals(style) ? 2.9f : 2.6f);
        paint.setColor(Color.argb(110, 120, 120, 120));
        if (SettingsProvider.DASHED.equals(style)) {
            for (int i = 0; i < 24; i++) canvas.drawArc(circle, -90f + i * 15f, 10.5f, false, paint);
            paint.setColor(color);
            paint.setShader(new LinearGradient(0, 2, 0, 22,
                    mix(color, Color.WHITE, .22f), color, Shader.TileMode.CLAMP));
            float sections = level * 24f / 100f;
            for (int i = 0; i < 24; i++) {
                float fraction = Math.max(0, Math.min(1, sections - i));
                if (fraction > 0) canvas.drawArc(circle, -90f + i * 15f,
                        fraction * 10.5f, false, paint);
            }
        } else if (SettingsProvider.CIRCLE.equals(style)) {
            canvas.drawArc(circle, 0, 360, false, paint);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setColor(color);
            paint.setShader(new LinearGradient(0, 2, 0, 22,
                    mix(color, Color.WHITE, .22f), color, Shader.TileMode.CLAMP));
            if (level > 0) canvas.drawArc(circle, -90, level * 3.6f, false, paint);
        } else if (SettingsProvider.PORTRAIT.equals(style)) {
            RectF body = new RectF(5.5f, 3.5f, 18.5f, 22f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(70, 120, 120, 120));
            canvas.drawRoundRect(body, 2f, 2f, paint);
            paint.setColor(Color.LTGRAY);
            canvas.drawRoundRect(new RectF(9f, 1f, 15f, 3.5f), .7f, .7f, paint);
            int clip = canvas.save();
            Path mask = new Path();
            mask.addRoundRect(body, 2f, 2f, Path.Direction.CW);
            canvas.clipPath(mask);
            paint.setShader(new LinearGradient(0, body.top, 0, body.bottom,
                    gradientColors(false), gradientStops(), Shader.TileMode.CLAMP));
            if (level > 0) canvas.drawRect(body.left, body.bottom - body.height() * level / 100f,
                    body.right, body.bottom, paint);
            canvas.restoreToCount(clip);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1f);
            paint.setColor(Color.LTGRAY);
            canvas.drawRoundRect(body, 2f, 2f, paint);
        } else {
            // Keep a fixed full-size track; remove the outer coloured rings first.
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(70, 120, 120, 120));
            canvas.drawCircle(12, 12, 9.5f, paint);
            paint.setColor(Color.WHITE);
            paint.setShader(new RadialGradient(12, 12, 9.5f,
                    gradientColors(true), gradientStops(), Shader.TileMode.CLAMP));
            if (level > 0) canvas.drawCircle(12, 12, 9.5f * level / 100f, paint);
            paint.setShader(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(.8f);
            paint.setColor(Color.argb(140, 160, 160, 160));
            canvas.drawCircle(12, 12, 9.5f, paint);
        }
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(level == 100 ? 8.3f : 9.6f);
        // A dark backing preserves legibility over both pale and saturated fills.
        String label = String.valueOf(level);
        paint.setColor(Color.argb(205, 18, 27, 25));
        canvas.drawText(label, 12.25f, 15.4f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText(label, 11.85f, 15f, paint);
        if (charging) {
            paint.setColor(Color.WHITE);
            canvas.drawCircle(19.6f, 4.4f, 1.4f, paint);
        }
        canvas.restoreToCount(saved);
    }

    private static int[] gradientColors(boolean radial) {
        int red = Color.rgb(229, 59, 59), amber = Color.rgb(246, 176, 35);
        int yellow = Color.rgb(245, 220, 48), green = Color.rgb(47, 192, 96);
        return radial ? new int[]{red, amber, yellow, green}
                : new int[]{green, yellow, amber, red};
    }
    private static float[] gradientStops() { return new float[]{0f, .35f, .65f, 1f}; }

    @Override public void setAlpha(int alpha) { /* Ignore SystemUI tint/alpha updates. */ }
    @Override public void setColorFilter(ColorFilter filter) { /* Keep the level colour. */ }
    @Override public void setTint(int tint) { /* Keep the level colour. */ }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public int getIntrinsicWidth() { return 24; }
    @Override public int getIntrinsicHeight() { return 24; }
}
