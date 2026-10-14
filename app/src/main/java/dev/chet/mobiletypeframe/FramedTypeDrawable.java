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
        this.shape = ShapeProvider.validShape(shape);
        this.fontPercent = ShapeProvider.validFontPercent(fontPercent);
        paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
    }

    @Override public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.isEmpty()) return;
        if (ShapeProvider.KARYL.equals(shape) || ShapeProvider.NISHIKI.equals(shape)) {
            drawRoundedBadge(canvas, b);
            return;
        }
        float width = b.width(), height = b.height();
        float inset = Math.max(0.7f * density, height * 0.07f);
        float stroke = Math.max(0.8f * density, height * 0.085f);
        paint.setColor(color);
        paint.setAlpha(alpha);
        paint.setColorFilter(filter);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        if (ShapeProvider.WAVES.equals(shape)) {
            // Two short radio waves above the upper-right edge of the glyph.
            float right = b.right - inset;
            float top = b.top + inset;
            canvas.drawArc(right - height * .56f, top,
                    right + height * .18f, top + height * .59f,
                    205, 84, false, paint);
            canvas.drawArc(right - height * .39f, top + height * .16f,
                    right + height * .01f, top + height * .49f,
                    205, 83, false, paint);
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
        boolean unboxed = ShapeProvider.WAVES.equals(shape);
        float availableWidth = width - (unboxed ? 2f : 4.4f) * inset;
        float availableHeight = height - (unboxed ? 2.5f : 3.5f) * inset;
        paint.setTextSize(availableHeight * 0.94f * fontPercent / 100f);
        float measured = paint.measureText(label);
        if (measured > availableWidth) paint.setTextSize(paint.getTextSize() * availableWidth / measured);
        Paint.FontMetrics fm = paint.getFontMetrics();
        float baseline = b.exactCenterY() - (fm.ascent + fm.descent) / 2f
                + (ShapeProvider.WAVES.equals(shape) ? height * .10f : 0f);
        float textCenter = b.exactCenterX();
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

    private void drawRoundedBadge(Canvas canvas, Rect b) {
        boolean filled = ShapeProvider.NISHIKI.equals(shape);
        float h = b.height(), w = b.width();
        float inset = h * .065f;
        paint.setColor(color); paint.setColorFilter(filter);
        paint.setStrokeJoin(Paint.Join.ROUND); paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
        paint.setStrokeWidth(h * .085f);
        paint.setAlpha(Math.round(alpha * (filled ? .30f : .72f)));
        canvas.drawRoundRect(b.left + inset, b.top + inset, b.right - inset,
                b.bottom - inset, h * .23f, h * .23f, paint);
        paint.setAlpha(alpha);
        paint.setStyle(Paint.Style.STROKE);
        // Original glyph paths: no external font or artwork dependency.
        float emWidth = label.length() * 7.8f - 1.8f;
        float scale = Math.min((w - h * .33f) / emWidth,
                h * .63f / 9f * fontPercent / 150f);
        int save = canvas.save();
        canvas.translate(b.exactCenterX() - emWidth * scale / 2f,
                b.exactCenterY() - 4.5f * scale);
        canvas.scale(scale, scale);
        paint.setStrokeWidth(filled ? 2.15f : 1.65f);
        for (int i = 0; i < label.length(); i++) {
            android.graphics.Path path = roundedGlyph(label.charAt(i));
            canvas.drawPath(path, paint);
            canvas.translate(7.8f, 0);
        }
        canvas.restoreToCount(save);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    static android.graphics.Path roundedGlyph(char c) {
        android.graphics.Path p = new android.graphics.Path();
        switch (c) {
            case '3':
                p.moveTo(0,0); p.lineTo(4,0); p.quadTo(6,0,6,2.2f);
                p.quadTo(6,4.5f,3,4.5f); p.quadTo(6,4.5f,6,6.8f);
                p.quadTo(6,9,4,9); p.lineTo(0,9); break;
            case '4':
                p.moveTo(1,0); p.lineTo(0,5); p.lineTo(6,5);
                p.moveTo(5,0); p.lineTo(5,9); break;
            case '5':
                p.moveTo(6,0); p.lineTo(0,0); p.lineTo(0,4.2f); p.lineTo(4,4.2f);
                p.quadTo(6,4.2f,6,6.5f); p.quadTo(6,9,3.5f,9); p.lineTo(0,9); break;
            case 'G':
                p.moveTo(6,1); p.quadTo(5.5f,0,3,0); p.quadTo(0,0,0,3);
                p.lineTo(0,6); p.quadTo(0,9,3,9); p.lineTo(6,9);
                p.lineTo(6,5); p.lineTo(3.5f,5); break;
            case 'L': p.moveTo(0,0); p.lineTo(0,9); p.lineTo(6,9); break;
            case 'T': p.moveTo(0,0); p.lineTo(6,0); p.moveTo(3,0); p.lineTo(3,9); break;
            case 'E':
                p.moveTo(6,0); p.lineTo(0,0); p.lineTo(0,9); p.lineTo(6,9);
                p.moveTo(0,4.5f); p.lineTo(5,4.5f); break;
            case '+': p.moveTo(0,4.5f); p.lineTo(6,4.5f); p.moveTo(3,1.5f); p.lineTo(3,7.5f); break;
            default: p.addRoundRect(0,0,6,9,1,1,android.graphics.Path.Direction.CW);
        }
        return p;
    }

    @Override public int getIntrinsicWidth() {
        float em = label.length() > 2 ? 26f : 21f;
        if (ShapeProvider.OPEN_CORNERS.equals(shape)) em += 1f;
        // Keep the intrinsic height at 16dp: SystemUI constrains the icon view's height.
        // Widen the artwork to let larger letters retain their requested text size.
        float inset = Math.max(.7f, 16f * .07f);
        if (ShapeProvider.KARYL.equals(shape) || ShapeProvider.NISHIKI.equals(shape)) {
            // Widen as the selected letter size increases; keep the badge 16dp tall.
            return Math.round(Math.max(em, 6f + label.length() * 6.8f * fontPercent / 100f) * density);
        }
        boolean unboxed = ShapeProvider.WAVES.equals(shape);
        paint.setTextSize((16f - (unboxed ? 2.5f : 3.5f) * inset)
                * .94f * fontPercent / 100f * density);
        float textWidth = paint.measureText(label);
        float minimumWidth = textWidth + (unboxed ? 2f : 4.4f) * inset * density;
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
