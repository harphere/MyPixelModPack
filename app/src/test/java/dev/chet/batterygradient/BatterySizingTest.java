package dev.chet.batterygradient;
import android.graphics.*;
import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class BatterySizingTest {
    private Bitmap render(String style,int size,String orientation,int level) {
        var d=new GradientBatteryDrawable();d.setStyle(style);d.setScalePercent(size);
        d.setOrientation(orientation);d.setLevelPercent(level);d.setBounds(0,0,320,240);
        Bitmap b=Bitmap.createBitmap(320,240,Bitmap.Config.ARGB_8888);d.draw(new Canvas(b));return b;
    }
    private int opaquePixels(Bitmap b) {
        int n=0;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(Color.alpha(b.getPixel(x,y))>100)n++;return n;
    }
    @Test public void everyStyleSupportsHalfSizeWithoutPaintingOutsideItsBounds() {
        for(String style:new String[]{SettingsProvider.FILLED,SettingsProvider.DASHED,SettingsProvider.CIRCLE,SettingsProvider.PORTRAIT}) {
            Bitmap full=render(style,100,"portrait",100),half=render(style,50,"portrait",100);
            assertTrue(opaquePixels(half)<opaquePixels(full)*.3);
            assertTrue(opaquePixels(half)>opaquePixels(full)*.2);
        }
    }
    @Test public void landscapeRotatesGradientAndEmptyingDirection() {
        Bitmap full=render(SettingsProvider.PORTRAIT,100,"landscape",100);
        Bitmap half=render(SettingsProvider.PORTRAIT,100,"landscape",50);
        int green=full.getPixel(190,120),red=half.getPixel(45,120);
        assertTrue(Color.green(green)>Color.red(green));assertTrue(Color.red(red)>Color.green(red));
        assertTrue(Color.alpha(half.getPixel(190,120))<100);
        assertEquals(0,Color.alpha(full.getPixel(120,30)));
    }
    @Test public void savedSizesAreIndependentAndDefaultRectangleIsHalfSize() {
        var p=Robolectric.buildContentProvider(SettingsProvider.class).create().get();
        p.call("set",SettingsProvider.PORTRAIT,null);assertEquals(50,p.call("get",null,null).getInt("size_percent"));
        Bundle extras=new Bundle();extras.putInt("percent",40);p.call("set_size",SettingsProvider.FILLED,extras);
        extras.putInt("percent",70);p.call("set_size",SettingsProvider.PORTRAIT,extras);
        p.call("set_orientation","landscape",null);
        p.call("set",SettingsProvider.FILLED,null);var options=p.call("get",null,null);
        assertEquals(40,options.getInt("size_percent"));assertEquals(70,options.getInt("size_portrait"));
        assertEquals("landscape",options.getString("orientation"));
        p.call("override",SettingsProvider.PORTRAIT,null);assertEquals(70,p.call("get",null,null).getInt("size_percent"));
    }
    @Test public void invalidSizesClampAndInvalidStyleCannotOverwriteAnotherStyle() {
        var p=Robolectric.buildContentProvider(SettingsProvider.class).create().get();Bundle extra=new Bundle();
        extra.putInt("percent",5);p.call("set_size",SettingsProvider.FILLED,extra);assertEquals(25,p.call("get",null,null).getInt("size_filled"));
        extra.putInt("percent",250);p.call("set_size",SettingsProvider.FILLED,extra);assertEquals(100,p.call("get",null,null).getInt("size_filled"));
        extra.putInt("percent",30);p.call("set_size","unknown",extra);assertEquals(100,p.call("get",null,null).getInt("size_filled"));
        p.call("set_orientation","bad",null);assertEquals("portrait",p.call("get",null,null).getString("orientation"));
    }
    @Test public void exportSizingAndOrientationPreview() throws Exception {
        Bitmap sheet=Bitmap.createBitmap(960,1350,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);
        canvas.drawColor(Color.rgb(24,27,32));Paint label=new Paint(Paint.ANTI_ALIAS_FLAG);label.setColor(Color.WHITE);label.setTextSize(18);
        String[] styles={SettingsProvider.FILLED,SettingsProvider.DASHED,SettingsProvider.CIRCLE,SettingsProvider.PORTRAIT,SettingsProvider.PORTRAIT};
        String[] names={"Filled circle","Dashed circle","Ring","Portrait","Landscape"};int[] sizes={100,75,50};
        for(int row=0;row<5;row++)for(int col=0;col<3;col++) {
            canvas.drawBitmap(render(styles[row],sizes[col],row==4?"landscape":"portrait",75),col*320,row*270,null);
            canvas.drawText(names[row]+" / "+sizes[col]+"% size",col*320+40,row*270+252,label);
        }
        java.io.File file=new java.io.File("build/battery-sizing-preview.png");file.getParentFile().mkdirs();
        try(var output=new java.io.FileOutputStream(file)){sheet.compress(Bitmap.CompressFormat.PNG,100,output);}
    }
}
