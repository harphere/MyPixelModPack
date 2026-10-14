package dev.chet.mobiletypeframe;
import android.graphics.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MobileStylesTest {
    @Test public void removedAndUnknownStylesMigrateToSquare() {
        for(String old:new String[]{"side_waves","italic","bold_italic","circle","unknown"})
            assertEquals(ShapeProvider.SQUARE,ShapeProvider.validShape(old));
        assertEquals("karyl",ShapeProvider.validShape("karyl"));
        assertEquals("nishiki",ShapeProvider.validShape("nishiki"));
    }
    Bitmap render(String label,String shape,int tint,int alpha) {
        FramedTypeDrawable d=new FramedTypeDrawable(label,1,shape,150);
        d.setBounds(0,0,d.getIntrinsicWidth()*8,128);d.setTint(tint);d.setAlpha(alpha);
        Bitmap b=Bitmap.createBitmap(d.getBounds().width(),128,Bitmap.Config.ARGB_8888);
        d.draw(new Canvas(b));return b;
    }
    @Test public void newBadgesRenderAllLabelsAndRespectAlphaAndTint() {
        for(String shape:new String[]{"karyl","nishiki"}) for(String label:new String[]{"3G","4G","5G","LTE","4G+","LTE+","5G+"}) {
            Bitmap b=render(label,shape,Color.RED,255);int pixels=0;
            for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++) {
                int c=b.getPixel(x,y);if(Color.alpha(c)>0){pixels++;assertEquals(0,Color.green(c));assertEquals(0,Color.blue(c));}
            }
            assertTrue(shape+label,pixels>100);
            Bitmap transparent=render(label,shape,Color.RED,0);
            for(int y=0;y<transparent.getHeight();y++)for(int x=0;x<transparent.getWidth();x++)assertEquals(0,Color.alpha(transparent.getPixel(x,y)));
        }
        assertFalse(render("5G","karyl",Color.WHITE,255).sameAs(render("5G","nishiki",Color.WHITE,255)));
    }
    @Test public void customLetterSizeWidensBadge() {
        for(String shape:new String[]{"karyl","nishiki"}) {
            var normal=new FramedTypeDrawable("LTE+",1,shape,100);
            var large=new FramedTypeDrawable("LTE+",1,shape,150);
            assertTrue(large.getIntrinsicWidth()>normal.getIntrinsicWidth());
            assertEquals(normal.getIntrinsicHeight(),large.getIntrinsicHeight());
        }
    }
}
