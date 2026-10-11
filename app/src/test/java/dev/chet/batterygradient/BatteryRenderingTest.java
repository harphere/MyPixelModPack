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
        return render(style,level,false,false);
    }
    private Bitmap render(String style,int level,boolean percentage,boolean charging) {
        GradientBatteryDrawable d=new GradientBatteryDrawable();
        d.setShowPercentage(percentage);d.setCharging(charging);
        d.setStyle(style); d.setLevelPercent(level); d.setBounds(0,0,320,240);
        Bitmap b=Bitmap.createBitmap(320,240,Bitmap.Config.ARGB_8888);
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
    @Test public void percentageCanBeHiddenInEveryStyle() {
        for(String style:new String[]{SettingsProvider.FILLED,SettingsProvider.PORTRAIT,
                SettingsProvider.DASHED,SettingsProvider.CIRCLE}) {
            Bitmap hidden=render(style,75,false,false),shown=render(style,75,true,false);
            assertFalse(hidden.sameAs(shown));
            assertEquals(hidden.getPixel(120,60),shown.getPixel(120,60));
        }
    }
    @Test public void chargingBoltOccupiesOnlyRightLane() {
        for(String style:new String[]{SettingsProvider.FILLED,SettingsProvider.PORTRAIT,
                SettingsProvider.DASHED,SettingsProvider.CIRCLE}) {
            Bitmap idle=render(style,75,false,false),charging=render(style,75,false,true);
            for(int y=0;y<240;y++)for(int x=0;x<240;x++)
                assertEquals(idle.getPixel(x,y),charging.getPixel(x,y));
            assertEquals(0,Color.alpha(idle.getPixel(270,110)));
            assertTrue(Color.alpha(charging.getPixel(270,110))>200);
        }
    }
    @Test public void percentagePreferenceDefaultsOffAndSurvivesStyleOverride() {
        SettingsProvider p=Robolectric.buildContentProvider(SettingsProvider.class).create().get();
        assertFalse(p.call("get",null,null).getBoolean("show_percentage"));
        p.call("set_percentage","true",null);
        p.call("set",SettingsProvider.PORTRAIT,null);
        p.call("override",SettingsProvider.CIRCLE,null);
        assertTrue(p.call("get",null,null).getBoolean("show_percentage"));
        p.call("set_percentage","false",null);
        assertFalse(p.call("get",null,null).getBoolean("show_percentage"));
        assertEquals(SettingsProvider.CIRCLE,p.call("get",null,null).getString("style"));
    }
    @Test public void generatePreviewSheet() throws Exception {
        String dir=System.getProperty("pack.preview.dir"); if(dir==null)return;
        Bitmap sheet=Bitmap.createBitmap(1600,940,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(sheet);canvas.drawColor(Color.rgb(24,27,32));
        Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);text.setColor(Color.WHITE);text.setTextSize(24);
        int[] levels={100,75,50,25,10};
        for(int row=0;row<3;row++) for(int col=0;col<levels.length;col++) {
            Bitmap icon=render(row==1?SettingsProvider.PORTRAIT:SettingsProvider.FILLED,levels[col],row==2,col==4);
            canvas.drawBitmap(icon,col*320,row*310+30,null);
            canvas.drawText((row==1?"Portrait ":row==2?"With percentage ":"Circle ")+levels[col]+"%",col*320+35,row*310+290,text);
        }
        File f=new File(dir,"battery-preview.png");f.getParentFile().mkdirs();
        try(FileOutputStream out=new FileOutputStream(f)){sheet.compress(Bitmap.CompressFormat.PNG,100,out);}
    }
}
