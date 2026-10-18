package dev.chet.mypixelmodpack;
import android.content.Context;
import android.graphics.*;
import android.view.View;
/** Original category vectors, with pastel circular backgrounds. */
public final class CategoryIconView extends View {
 private final int category;private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 public CategoryIconView(Context c,int category){super(c);this.category=category;}
 @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);int save=canvas.save();canvas.scale(getWidth()/44f,getHeight()/44f);
  int[] colors={0xffa5c9ff,0xfff2b4df,0xff93d99c,0xffc9b8f5,0xffffc28c};p.setStyle(Paint.Style.FILL);p.setColor(colors[category]);canvas.drawCircle(22,22,22,p);
  p.setColor(0xff273345);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setStrokeJoin(Paint.Join.ROUND);p.setStrokeCap(Paint.Cap.ROUND);
  switch(category){
   case 0:for(int i=0;i<3;i++)canvas.drawRect(12+i*7,26-i*5,16+i*7,32,p);break;
   case 1:canvas.drawCircle(17,15,3,p);canvas.drawRect(10,26,16,32,p);Path triangle=new Path();triangle.moveTo(28,24);triangle.lineTo(23,32);triangle.lineTo(33,32);triangle.close();canvas.drawPath(triangle,p);break;
   case 2:Path phone=new Path();phone.moveTo(13,11);phone.cubicTo(9,25,22,36,33,30);phone.lineTo(29,24);phone.lineTo(25,27);phone.quadTo(18,24,17,18);phone.lineTo(20,15);phone.lineTo(17,10);phone.close();canvas.drawPath(phone,p);break;
   case 3:canvas.drawRoundRect(13,9,31,35,3,3,p);canvas.drawLine(20,31,24,31,p);break;
   default:canvas.drawCircle(22,22,10,p);canvas.drawCircle(22,22,4,p);for(int i=0;i<8;i++){double a=i*Math.PI/4;canvas.drawLine(22+(float)Math.cos(a)*11,22+(float)Math.sin(a)*11,22+(float)Math.cos(a)*14,22+(float)Math.sin(a)*14,p);}
  }canvas.restoreToCount(save);
 }
}
