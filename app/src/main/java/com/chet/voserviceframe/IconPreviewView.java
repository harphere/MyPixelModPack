package com.chet.voserviceframe;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class IconPreviewView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private BadgeRenderer.Kind kind = BadgeRenderer.Kind.VOWIFI;
    private int style = IconStyle.SQUARE;

    public IconPreviewView(Context context, AttributeSet attrs) { super(context, attrs); }
    public IconPreviewView(Context context) { super(context); }

    public void setPreview(BadgeRenderer.Kind kind, int style) {
        this.kind = kind;
        this.style = style;
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float s = Math.min(getWidth(), getHeight()) * 0.82f;
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        BadgeRenderer.draw(canvas, new RectF(cx-s/2f, cy-s/2f, cx+s/2f, cy+s/2f), paint, kind, style, 0xFF111111);
    }
}
