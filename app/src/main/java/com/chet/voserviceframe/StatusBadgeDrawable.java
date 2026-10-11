package com.chet.voserviceframe;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class StatusBadgeDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final BadgeRenderer.Kind kind;
    private final int style;
    private int tintColor = 0xFFFFFFFF;

    public StatusBadgeDrawable(BadgeRenderer.Kind kind, int style) {
        this.kind = kind;
        this.style = style;
    }

    @Override public void draw(Canvas canvas) {
        Rect b = getBounds();
        float pad = Math.max(0f, Math.min(b.width(), b.height()) * 0.04f);
        BadgeRenderer.draw(canvas, new RectF(b.left + pad, b.top + pad, b.right - pad, b.bottom - pad), paint, kind, style, tintColor);
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); invalidateSelf(); }
    @Override public void setColorFilter(ColorFilter colorFilter) { paint.setColorFilter(colorFilter); invalidateSelf(); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    @Override public int getIntrinsicWidth() { return 64; }
    @Override public int getIntrinsicHeight() { return 64; }

    @Override public void setTint(int tintColor) {
        this.tintColor = tintColor;
        invalidateSelf();
    }
}
