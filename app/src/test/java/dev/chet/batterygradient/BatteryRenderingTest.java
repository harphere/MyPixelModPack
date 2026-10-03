package dev.chet.batterygradient;

import android.graphics.*;
import android.os.Bundle;
import java.io.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class BatteryRenderingTest {
    private Bitmap render(String style,int level) {
        GradientBatteryDrawable d=new GradientBatteryDrawable();
        d.setStyle(style); d.setLevelPercent(level); d.setBounds(0,0,240,240);
        Bitmap b=Bitmap.createBitmap(240,240,Bitmap.Config.ARGB_8888);
        d.draw(new Canvas(b)); return b;
    }
    @Test public void circleRemovesOuterColourWhileKeepingWarmCentre() {
        Bitmap full=render(SettingsProvider.FILLED,100), half=render(SettingsProvider.FILLED,50), empty=render(SettingsProvider.FILLED,0);
        int outer=full.getPixel(195,120);
        assertTrue(Color.green(outer)>Color.red(outer));
        assertEquals(255,Color.alpha(outer));
        assertTrue(Color.alpha(half.getPixel(195,120))<100);
        assertTrue(Color.red(half.getPixel(120,160))>Color.green(half.getPixel(120,160)));
        assertTrue(Color.alpha(empty.getPixel(120,160))<100);
    }
    @Test public void portraitRemovesTopGreenAndRetainsBottomRed() {
        Bitmap full=render(SettingsProvider.PORTRAIT,100), half=render(SettingsProvider.PORTRAIT,50);
        int top=full.getPixel(75,55), bottom=half.getPixel(75,200);
        assertTrue(Color.green(top)>Color.red(top));
        assertTrue(Color.alpha(half.getPixel(75,55))<100);
        assertTrue(Color.red(bottom)>Color.green(bottom));
    }
    @Test public void portraitSelectionAndOverrideRoundTrip() {
        var c=RuntimeEnvironment.getApplication();
        SettingsProvider p=Robolectric.buildContentProvider(SettingsProvider.class).create().get();
        p.call("set",SettingsProvider.PORTRAIT,null);
        assertEquals(SettingsProvider.PORTRAIT,p.call("selected",null,null).getString("style"));
        p.call("override",SettingsProvider.CIRCLE,null);
        assertEquals(SettingsProvider.CIRCLE,p.call("get",null,null).getString("style"));
        p.call("override",SettingsProvider.DEFAULT,null);
        assertEquals(SettingsProvider.PORTRAIT,p.call("get",null,null).getString("style"));
    }
    @Test public void generatePreviewSheet() throws Exception {
        String dir=System.getProperty("pack.preview.dir"); if(dir==null)return;
        Bitmap sheet=Bitmap.createBitmap(1200,620,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(sheet);canvas.drawColor(Color.rgb(24,27,32));
        Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);text.setColor(Color.WHITE);text.setTextSize(24);
        int[] levels={100,75,50,25,10};
        for(int row=0;row<2;row++) for(int col=0;col<levels.length;col++) {
            Bitmap icon=render(row==0?SettingsProvider.FILLED:SettingsProvider.PORTRAIT,levels[col]);
            canvas.drawBitmap(icon,col*240,row*310+30,null);
            canvas.drawText((row==0?"Circle ":"Portrait ")+levels[col]+"%",col*240+35,row*310+290,text);
        }
        File f=new File(dir,"battery-preview.png");f.getParentFile().mkdirs();
        try(FileOutputStream out=new FileOutputStream(f)){sheet.compress(Bitmap.CompressFormat.PNG,100,out);}
    }
}
