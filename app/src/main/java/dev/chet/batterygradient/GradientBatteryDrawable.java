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
    private boolean charging, activelyCharging;
    private boolean showPercentage;
    private boolean animationEnabled = true, animationRunning;
    private long animationStart;
    private final Path landscapePerimeter = BatteryJPaths.perimeter();
    private final Path landscapeFill = BatteryJPaths.fillMask();
    private static final RectF LANDSCAPE_FILL = new RectF(5f, 9.602f, 16.77f, 14.398f);
    private float phase;
    private long frameCount;
    private final Runnable frame = new Runnable() {
        @Override public void run() {
            if (!shouldAnimate()) { stopAnimation(); return; }
            phase = ((android.os.SystemClock.uptimeMillis() - animationStart) % 1800L) / 1800f;
            frameCount++;
            invalidateSelf();
            scheduleSelf(this, android.os.SystemClock.uptimeMillis() + 32);
        }
    };
    public void setAnimationEnabled(boolean enabled) { animationEnabled = enabled; updateAnimation(); invalidateSelf(); }
    boolean shouldAnimate() { return animationEnabled && activelyCharging && level > 0 && level < 100 && isVisible() && getCallback() != null; }
    private void updateAnimation() {
        if (shouldAnimate() && !animationRunning) {
            animationRunning = true; animationStart = android.os.SystemClock.uptimeMillis();
            phase = 0; scheduleSelf(frame, animationStart + 32);
        } else if (!shouldAnimate()) stopAnimation();
    }
    public String animationStatus() {
        return "level=" + level + "; charging=" + activelyCharging + "; animation_enabled=" + animationEnabled
                + "; visible=" + isVisible() + "; running=" + animationRunning + "; frames=" + frameCount;
    }
    public void stopAnimation() { unscheduleSelf(frame); animationRunning = false; phase = 0; }
    @Override public boolean setVisible(boolean visible, boolean restart) {
        boolean changed = super.setVisible(visible, restart);
        if (restart) stopAnimation();
        updateAnimation(); return changed;
    }
    void setAnimationPhaseForTest(float value) { phase = value; }
    private String style = SettingsProvider.FILLED;

    public void setLevelPercent(int value) {
        level = Math.max(0, Math.min(100, value));
        updateAnimation();
        invalidateSelf();
    }
    public void setCharging(boolean value) { charging = value; activelyCharging = value; updateAnimation(); invalidateSelf(); }
    public void setActivelyCharging(boolean value) { activelyCharging = value; updateAnimation(); invalidateSelf(); }
    public void setShowPercentage(boolean value) { showPercentage = value; invalidateSelf(); }
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
        float scale = Math.min(bounds.width() / 32f, bounds.height() / 24f);
        canvas.translate((bounds.width() - 32f * scale) / 2f,
                (bounds.height() - 24f * scale) / 2f);
        canvas.scale(scale, scale);
        int bodyLayer = canvas.saveLayer(0, 0, 24, 24, null);
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
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.LTGRAY);
            canvas.drawPath(landscapePerimeter, paint);
            paint.setColor(Color.argb(70, 120, 120, 120));
            canvas.drawPath(landscapeFill, paint);
            int clip = canvas.save();
            canvas.clipPath(landscapeFill);
            paint.setColor(Color.WHITE);
            paint.setShader(new LinearGradient(LANDSCAPE_FILL.left, 0, LANDSCAPE_FILL.right, 0,
                    gradientColors(true), gradientStops(), Shader.TileMode.CLAMP));
            if (level > 0) canvas.drawRect(LANDSCAPE_FILL.left, LANDSCAPE_FILL.top,
                    LANDSCAPE_FILL.left + LANDSCAPE_FILL.width() * level / 100f,
                    LANDSCAPE_FILL.bottom, paint);
            canvas.restoreToCount(clip);
            paint.setShader(null);
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
        drawChargingPulse(canvas, circle);
        canvas.restoreToCount(bodyLayer);
        paint.setShader(null);
        paint.setAlpha(255);
        paint.setStyle(Paint.Style.FILL);
        if (showPercentage) {
            paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
            paint.setTextAlign(Paint.Align.CENTER);
            boolean landscape = SettingsProvider.PORTRAIT.equals(style);
            paint.setTextSize(landscape ? 6.6f : level == 100 ? 8.3f : 9.6f);
            String label = String.valueOf(level);
            paint.setColor(Color.argb(205, 18, 27, 25));
            canvas.drawText(label, landscape ? 11.1f : 12.25f, landscape ? 14.5f : 15.4f, paint);
            paint.setColor(Color.WHITE);
            canvas.drawText(label, landscape ? 10.7f : 11.85f, landscape ? 14.1f : 15f, paint);
        }
        if (charging) {
            // Dedicated right-hand lane: never cover the battery colour or label.
            Path bolt = new Path();
            bolt.moveTo(28.5f, 4f); bolt.lineTo(24.5f, 13f);
            bolt.lineTo(28f, 13f); bolt.lineTo(26f, 20f);
            bolt.lineTo(31.5f, 10f); bolt.lineTo(28f, 10f);
            bolt.lineTo(30f, 4f); bolt.close();
            if (SettingsProvider.PORTRAIT.equals(style)) {
                android.graphics.Matrix slim = new android.graphics.Matrix();
                slim.setScale(.65f, .65f, 28f, 12f); bolt.transform(slim);
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(.8f);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setColor(Color.argb(220, 18, 27, 25));
            canvas.drawPath(bolt, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            canvas.drawPath(bolt, paint);
        }
        canvas.restoreToCount(saved);
    }

    private void drawChargingPulse(Canvas canvas, RectF circle) {
        if (!animationEnabled || !activelyCharging || level <= 0 || level >= 100 || !isVisible()) return;
        int alpha = Math.round(150 * (float)Math.sin(Math.PI * phase));
        if (alpha <= 0) return;
        paint.setShader(null); paint.setColor(Color.argb(alpha, 255, 255, 255));
        paint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_ATOP));
        paint.setStyle(Paint.Style.STROKE); paint.setStrokeCap(Paint.Cap.BUTT);
        if (SettingsProvider.FILLED.equals(style)) {
            float radius = 9.5f * level / 100f;
            int save = canvas.save(); Path clip = new Path();
            clip.addCircle(12, 12, radius, Path.Direction.CW); canvas.clipPath(clip);
            paint.setStrokeWidth(1.8f); canvas.drawCircle(12, 12, radius * phase, paint);
            canvas.restoreToCount(save);
        } else if (SettingsProvider.PORTRAIT.equals(style)) {
            int save = canvas.save();
            canvas.clipPath(landscapeFill);
            float right = LANDSCAPE_FILL.left + LANDSCAPE_FILL.width() * level / 100f;
            canvas.clipRect(LANDSCAPE_FILL.left, LANDSCAPE_FILL.top, right, LANDSCAPE_FILL.bottom);
            float x = LANDSCAPE_FILL.left + (right - LANDSCAPE_FILL.left) * phase;
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(x - 1.1f, LANDSCAPE_FILL.top, x + 1.1f, LANDSCAPE_FILL.bottom, paint);
            canvas.restoreToCount(save);
        } else if (SettingsProvider.DASHED.equals(style)) {
            paint.setStrokeWidth(2.9f); float sections = level * 24f / 100f;
            int index = Math.min(23, (int)(phase * 24));
            float fraction = Math.max(0, Math.min(1, sections - index));
            if (fraction > 0) canvas.drawArc(circle, -90 + index * 15, fraction * 10.5f, false, paint);
        } else {
            paint.setStrokeWidth(2.6f); float start = phase * 360;
            float sweep = Math.min(35, level * 3.6f - start);
            if (sweep > 0) canvas.drawArc(circle, -90 + start, sweep, false, paint);
        }
        paint.setAlpha(255); paint.setXfermode(null);
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
    @Override public int getIntrinsicWidth() { return 32; }
    @Override public int getIntrinsicHeight() { return 24; }
}
