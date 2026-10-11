package dev.chet.mobiletypeframe;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/** Original, monochrome network-type artwork. Draws within the SystemUI icon view. */
public final class FramedTypeDrawable extends Drawable {
    private final String label;
    private final float density;
    private final String shape;
    private final int fontPercent;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private ColorStateList tint;
    private int color = Color.WHITE;
    private int alpha = 255;
    private ColorFilter filter;

    public FramedTypeDrawable(String label, float density, String shape, int fontPercent) {
        this.label = label;
        this.density = density;
        this.shape = shape;
        this.fontPercent = ShapeProvider.validFontPercent(fontPercent);
        int weight = ShapeProvider.ITALIC.equals(shape) ? Typeface.ITALIC
                : ShapeProvider.BOLD_ITALIC.equals(shape) ? Typeface.BOLD_ITALIC
                : Typeface.BOLD;
        paint.setTypeface(Typeface.create("sans-serif-condensed", weight));
    }

    @Override public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.isEmpty()) return;
        float width = b.width(), height = b.height();
        float inset = Math.max(0.7f * density, height * 0.07f);
        float stroke = Math.max(0.8f * density, height * 0.085f);
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setColorFilter(filter);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        if (ShapeProvider.ITALIC.equals(shape)
                || ShapeProvider.BOLD_ITALIC.equals(shape)) {
            // Typography-only styles deliberately have no surrounding artwork.
        } else if (ShapeProvider.WAVES.equals(shape)) {
            // Two short radio waves above the upper-right edge of the glyph.
            float right = b.right - inset;
            float top = b.top + inset;
            canvas.drawArc(right - height * .56f, top,
                    right + height * .18f, top + height * .59f,
                    205, 84, false, paint);
            canvas.drawArc(right - height * .39f, top + height * .16f,
                    right + height * .01f, top + height * .49f,
                    205, 83, false, paint);
        } else if (ShapeProvider.SIDE_WAVES.equals(shape)) {
            // Three concentric arcs to the right, as in the supplied reference.
            float cx = b.left + width * .60f;
            float cy = b.exactCenterY();
            paint.setStrokeWidth(Math.max(.68f * density, height * .055f));
            for (int i = 0; i < 3; i++) {
                float radius = height * (.24f + .15f * i);
                canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius,
                        -66f, 132f, false, paint);
            }
        } else if (ShapeProvider.OPEN_CORNERS.equals(shape)) {
            float l = b.left + inset, t = b.top + inset;
            float r = b.right - inset, bottom = b.bottom - inset;
            float length = Math.min(width, height) * .23f;
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(l, t, l + length, t, paint);
            canvas.drawLine(l, t, l, t + length, paint);
            canvas.drawLine(r - length, t, r, t, paint);
            canvas.drawLine(r, t, r, t + length, paint);
            canvas.drawLine(l, bottom - length, l, bottom, paint);
            canvas.drawLine(l, bottom, l + length, bottom, paint);
            canvas.drawLine(r, bottom - length, r, bottom, paint);
            canvas.drawLine(r - length, bottom, r, bottom, paint);
        } else if (ShapeProvider.FILLED.equals(shape)) {
            // Render the lettering as transparent cutouts so any status bar background
            // shows through the badge, including light and tinted backgrounds.
            canvas.saveLayer(b.left, b.top, b.right, b.bottom, null);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(b.left + inset, b.top + inset,
                    b.right - inset, b.bottom - inset,
                    height * .14f, height * .14f, paint);
        } else {
            canvas.drawRoundRect(b.left + inset, b.top + inset,
                    b.right - inset, b.bottom - inset,
                    height * 0.14f, height * 0.14f, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        boolean unboxed = ShapeProvider.WAVES.equals(shape)
                || ShapeProvider.SIDE_WAVES.equals(shape)
                || ShapeProvider.ITALIC.equals(shape)
                || ShapeProvider.BOLD_ITALIC.equals(shape);
        float availableWidth = ShapeProvider.SIDE_WAVES.equals(shape)
                ? width * .60f : width - (unboxed ? 2f : 4.4f) * inset;
        float availableHeight = height - (unboxed ? 2.5f : 3.5f) * inset;
        paint.setTextSize(availableHeight * 0.94f * fontPercent / 100f);
        float measured = paint.measureText(label);
        if (measured > availableWidth) paint.setTextSize(paint.getTextSize() * availableWidth / measured);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float baseline = b.exactCenterY() - (fm.ascent + fm.descent) / 2f
                + (ShapeProvider.WAVES.equals(shape) ? height * .10f : 0f);
        float textCenter = ShapeProvider.SIDE_WAVES.equals(shape)
                ? b.left + width * .30f : b.exactCenterX();
        if (ShapeProvider.FILLED.equals(shape)) {
            paint.setColorFilter(null);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawText(label, textCenter, baseline, paint);
            paint.setXfermode(null);
            canvas.restore();
        } else {
            canvas.drawText(label, textCenter, baseline, paint);
        }
    }

    @Override public int getIntrinsicWidth() {
        float em = label.length() > 2 ? 26f : 21f;
        if (ShapeProvider.SIDE_WAVES.equals(shape)) em = label.length() > 2 ? 38f : 31f;
        if (ShapeProvider.OPEN_CORNERS.equals(shape)) em += 1f;
        // Keep the intrinsic height at 16dp: SystemUI constrains the icon view's height.
        // Widen the artwork to let larger letters retain their requested text size.
        float inset = Math.max(.7f, 16f * .07f);
        boolean sideWaves = ShapeProvider.SIDE_WAVES.equals(shape);
        boolean unboxed = sideWaves || ShapeProvider.WAVES.equals(shape)
                || ShapeProvider.ITALIC.equals(shape)
                || ShapeProvider.BOLD_ITALIC.equals(shape);
        paint.setTextSize((16f - (unboxed ? 2.5f : 3.5f) * inset)
                * .94f * fontPercent / 100f * density);
        float textWidth = paint.measureText(label);
        float minimumWidth = sideWaves
                ? (textWidth + 2f * density) / .60f
                : textWidth + (unboxed ? 2f : 4.4f) * inset * density;
        return Math.round(Math.max(em * density, minimumWidth));
    }
    @Override public int getIntrinsicHeight() { return Math.round(16f * density); }
    @Override public void setAlpha(int value) { alpha = value; invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter value) { filter = value; invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public void setTint(int value) { color = value; invalidateSelf(); }
    @Override public void setTintList(ColorStateList value) {
        tint = value;
        updateTint();
    }
    @Override public boolean isStateful() { return tint != null && tint.isStateful(); }
    @Override protected boolean onStateChange(int[] state) {
        if (tint == null) return false;
        int next = tint.getColorForState(state, tint.getDefaultColor());
        if (next == color) return false;
        color = next;
        invalidateSelf();
        return true;
    }
    private void updateTint() {
        if (tint != null) color = tint.getColorForState(getState(), tint.getDefaultColor());
        invalidateSelf();
    }
}
