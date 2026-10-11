package com.chet.voserviceframe;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

public final class BadgeRenderer {
    public enum Kind { VOWIFI, VOLTE }

    private BadgeRenderer() {}

    public static void draw(Canvas canvas, RectF bounds, Paint paint, Kind kind, int style, int color) {
        float w = bounds.width();
        float h = bounds.height();
        float size = Math.min(w, h);
        float cx = bounds.centerX();
        float cy = bounds.centerY();

        paint.setAntiAlias(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(Math.max(1.6f, size * 0.075f));

        float badge = size * 0.82f;
        RectF r = new RectF(cx - badge/2f, cy - badge/2f, cx + badge/2f, cy + badge/2f);

        if (style == IconStyle.SQUARE) {
            canvas.drawRect(r, paint);
        } else if (style == IconStyle.ROUNDED) {
            float radius = badge * 0.16f;
            canvas.drawRoundRect(r, radius, radius, paint);
        } else if (style == IconStyle.OPEN_CORNERS) {
            float c = badge * 0.23f;
            canvas.drawLine(r.left, r.top, r.left + c, r.top, paint);
            canvas.drawLine(r.left, r.top, r.left, r.top + c, paint);
            canvas.drawLine(r.right - c, r.top, r.right, r.top, paint);
            canvas.drawLine(r.right, r.top, r.right, r.top + c, paint);
            canvas.drawLine(r.left, r.bottom - c, r.left, r.bottom, paint);
            canvas.drawLine(r.left, r.bottom, r.left + c, r.bottom, paint);
            canvas.drawLine(r.right, r.bottom - c, r.right, r.bottom, paint);
            canvas.drawLine(r.right - c, r.bottom, r.right, r.bottom, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);

        boolean signal = style == IconStyle.SIGNAL;
        float textCenterX = signal ? cx - badge * 0.10f : cx;
        float textSize = badge * 0.31f;
        paint.setTextSize(textSize);

        Paint.FontMetrics fm = paint.getFontMetrics();
        float line = textSize * 0.84f;
        float base1 = cy - line * 0.20f - (fm.ascent + fm.descent) / 2f - line/2f;
        float base2 = base1 + line;
        canvas.drawText("Vo", textCenterX, base1, paint);
        canvas.drawText(kind == Kind.VOWIFI ? "WiFi" : "LTE", textCenterX, base2, paint);

        if (signal) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1.4f, badge * 0.055f));
            if (kind == Kind.VOWIFI) {
                float sx = cx + badge * 0.22f;
                float sy = cy - badge * 0.16f;
                for (int i = 1; i <= 3; i++) {
                    float rr = badge * (0.10f + i * 0.085f);
                    RectF arc = new RectF(sx - rr, sy - rr, sx + rr, sy + rr);
                    canvas.drawArc(arc, 285f, 70f, false, paint);
                }
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(sx, sy, badge * 0.035f, paint);
            } else {
                paint.setStyle(Paint.Style.FILL);
                float left = cx + badge * 0.12f;
                float bottom = cy - badge * 0.02f;
                float bw = badge * 0.07f;
                float gap = badge * 0.045f;
                for (int i = 0; i < 4; i++) {
                    float bh = badge * (0.10f + i * 0.085f);
                    RectF bar = new RectF(left + i*(bw+gap), bottom - bh, left + i*(bw+gap) + bw, bottom);
                    canvas.drawRoundRect(bar, bw*0.35f, bw*0.35f, paint);
                }
            }
        }
    }
}
