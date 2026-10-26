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
        if (ShapeProvider.KARYL.equals(shape) || ShapeProvider.NISHIKI.equals(shape)
                || ShapeProvider.HADAUN.equals(shape)) {
            drawRoundedBadge(canvas, b);
            return;
        }
        if (ShapeProvider.WAVES.equals(shape)) {
            drawWaves(canvas, b);
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
        if (ShapeProvider.OPEN_CORNERS.equals(shape)) {
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

        float scale = glyphScale(width, height, false);
        if (ShapeProvider.FILLED.equals(shape)) {
            paint.setColorFilter(null);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            drawLetters(canvas,b.exactCenterX(),b.exactCenterY(),scale,1.65f);
            paint.setXfermode(null);canvas.restore();
        } else drawLetters(canvas,b.exactCenterX(),b.exactCenterY(),scale,1.65f);
    }

    private float glyphScale(float width,float height,boolean waves) {
        float emWidth=label.length()*7.8f-1.8f;
        return Math.min((width-height*(waves?.60f:.33f))/emWidth,
                height*.63f/9f*fontPercent/150f);
    }
    private void drawLetters(Canvas canvas,float cx,float cy,float scale,float weight) {
        float emWidth=label.length()*7.8f-1.8f;
        int save=canvas.save();canvas.translate(cx-emWidth*scale/2f,cy-4.5f*scale);canvas.scale(scale,scale);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);paint.setStrokeWidth(weight);
        for(int i=0;i<label.length();i++){canvas.drawPath(roundedGlyph(label.charAt(i)),paint);canvas.translate(7.8f,0);}
        canvas.restoreToCount(save);paint.setStrokeCap(Paint.Cap.BUTT);
    }
    private void drawWaves(Canvas canvas,Rect b) {
        float h=b.height(),scale=glyphScale(b.width()-h*.35f,h,false);
        float emWidth=label.length()*7.8f-1.8f;
        // Use a full-height wave beside the top of G instead of a tiny arc squeezed above it.
        float radius=h*.31f, stroke=h*.065f;
        float cx=b.left+(b.width()-radius-stroke)/2f;
        float cy=b.exactCenterY();
        paint.setColor(color);paint.setColorFilter(filter);
        drawLetters(canvas,cx,cy,scale,1.65f);
        float x=cx+emWidth*scale/2f+stroke;
        float y=b.top+stroke;
        android.graphics.Path waves=new android.graphics.Path();
        waves.moveTo(x,y);waves.quadTo(x+radius,y,x+radius,y+radius);
        waves.moveTo(x,y+radius*.55f);waves.quadTo(x+radius*.45f,y+radius*.55f,x+radius*.45f,y+radius);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeWidth(stroke);
        canvas.drawPath(waves,paint);paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawRoundedBadge(Canvas canvas, Rect b) {
        boolean filled = ShapeProvider.NISHIKI.equals(shape);
        boolean segmented = ShapeProvider.HADAUN.equals(shape);
        float h = b.height(), w = b.width();
        float inset = h * .065f;
        paint.setColor(color); paint.setColorFilter(filter);
        paint.setStrokeJoin(Paint.Join.ROUND); paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(filled ? Paint.Style.FILL : Paint.Style.STROKE);
        paint.setStrokeWidth(h * .085f);
        paint.setAlpha(Math.round(alpha * (filled ? .30f : .72f)));
        if (segmented) {
            // H: four strong rounded brackets with tiny breaks in the frame.
            paint.setAlpha(alpha); paint.setStrokeWidth(h * .10f);
            float l=b.left+inset, t=b.top+inset, right=b.right-inset, bottom=b.bottom-inset;
            float radius=h*.20f, gap=h*.085f, cx=b.exactCenterX(), cy=b.exactCenterY();
            android.graphics.Path frame = new android.graphics.Path();
            frame.moveTo(cx-gap,t); frame.lineTo(l+radius,t); frame.quadTo(l,t,l,t+radius); frame.lineTo(l,cy-gap);
            frame.moveTo(l,cy+gap); frame.lineTo(l,bottom-radius); frame.quadTo(l,bottom,l+radius,bottom); frame.lineTo(cx-gap,bottom);
            frame.moveTo(cx+gap,bottom); frame.lineTo(right-radius,bottom); frame.quadTo(right,bottom,right,bottom-radius); frame.lineTo(right,cy+gap);
            frame.moveTo(right,cy-gap); frame.lineTo(right,t+radius); frame.quadTo(right,t,right-radius,t); frame.lineTo(cx+gap,t);
            canvas.drawPath(frame,paint);
        } else {
            canvas.drawRoundRect(b.left + inset, b.top + inset, b.right - inset,
                    b.bottom - inset, h * .23f, h * .23f, paint);
        }
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
        paint.setStrokeWidth(filled ? 2.15f : segmented ? 1.9f : 1.65f);
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
        float width = Math.max(em,6f+label.length()*6.8f*fontPercent/100f);
        if(ShapeProvider.WAVES.equals(shape)) width+=16f*.55f;
        return Math.round(width*density);
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
