package dev.chet.batterygradient;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ChargingAnimationTest {
    static final class Callback implements Drawable.Callback {
        Runnable pending; int schedules;
        public void invalidateDrawable(Drawable d) {}
        public void scheduleDrawable(Drawable d,Runnable r,long when) { pending=r; schedules++; }
        public void unscheduleDrawable(Drawable d,Runnable r) { if(pending==r) pending=null; }
    }
    @Test public void framesStopOnHiddenDisabledFullOrUnpluggedAndResume() {
        GradientBatteryDrawable d=new GradientBatteryDrawable(); Callback c=new Callback();d.setCallback(c);
        d.setCharging(true);assertNotNull(c.pending);
        d.setVisible(false,false);assertNull(c.pending);
        d.setVisible(true,false);assertNotNull(c.pending);
        d.setAnimationEnabled(false);assertNull(c.pending);
        d.setAnimationEnabled(true);assertNotNull(c.pending);
        d.setLevelPercent(100);assertNull(c.pending);
        d.setLevelPercent(60);assertNotNull(c.pending);
        d.setActivelyCharging(false);assertNull(c.pending);
        d.setCharging(false);assertNull(c.pending);
        d.setCharging(true);assertNotNull(c.pending);
        d.stopAnimation();assertNull(c.pending);
    }
    @Test public void visibleRestartRestoresLostFrameCallback() {
        GradientBatteryDrawable d = new GradientBatteryDrawable(); Callback c = new Callback();
        d.setCallback(c); d.setCharging(true); assertNotNull(c.pending);
        c.pending = null; // ImageView callback was reset while moving the icon.
        d.setVisible(true, true); assertNotNull(c.pending);
        c.pending.run(); assertTrue(d.animationStatus().contains("frames=1"));
        d.setVisible(false, false); assertNull(c.pending);
    }
    Bitmap render(String style,float phase,boolean enabled,int level) {
        GradientBatteryDrawable d=new GradientBatteryDrawable();d.setStyle(style);d.setLevelPercent(level);
        d.setCharging(true);d.setAnimationEnabled(enabled);d.setAnimationPhaseForTest(phase);d.setBounds(0,0,320,240);
        Bitmap b=Bitmap.createBitmap(320,240,Bitmap.Config.ARGB_8888);d.draw(new Canvas(b));return b;
    }
    @Test public void eachStyleAnimatesWithoutPaintingEmptyAreaOrBoltLane() {
        for(String style:new String[]{SettingsProvider.FILLED,SettingsProvider.PORTRAIT,SettingsProvider.CIRCLE,SettingsProvider.DASHED}) {
            Bitmap base=render(style,.25f,false,60),pulse=render(style,.25f,true,60);
            assertFalse(style,base.sameAs(pulse));
            for(int y=0;y<240;y++)for(int x=0;x<320;x++) {
                if(x>=240 || Color.alpha(base.getPixel(x,y))==0) assertEquals(style,base.getPixel(x,y),pulse.getPixel(x,y));
            }
            assertTrue(render(style,.25f,true,100).sameAs(render(style,.25f,false,100)));
        }
    }
    @Test public void attachedViewStopsForScreenOffAndDetachThenRestartsOnScreenOn() {
        var controller=Robolectric.buildActivity(android.app.Activity.class).setup().visible();
        var activity=controller.get();
        var power=(android.os.PowerManager)activity.getSystemService(android.content.Context.POWER_SERVICE);
        Shadows.shadowOf(power).setIsInteractive(true);
        var root=new android.widget.FrameLayout(activity);activity.setContentView(root);
        var icon=new AnimatedBatteryView(activity);var drawable=new GradientBatteryDrawable();
        icon.setImageDrawable(drawable);root.addView(icon);drawable.setCharging(true);
        assertTrue(drawable.shouldAnimate());
        Shadows.shadowOf(power).setIsInteractive(false);
        activity.sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_SCREEN_OFF));
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertFalse(drawable.shouldAnimate());
        Shadows.shadowOf(power).setIsInteractive(true);
        activity.sendBroadcast(new android.content.Intent(android.content.Intent.ACTION_SCREEN_ON));
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertTrue(drawable.shouldAnimate());
        root.removeView(icon);assertFalse(drawable.shouldAnimate());
        root.addView(icon);assertTrue(drawable.shouldAnimate());
        controller.pause().stop().destroy();assertFalse(drawable.shouldAnimate());
    }
    @Test public void exportRenderedChargingPreview() throws Exception {
        java.io.File directory = new java.io.File("build/charging-preview"); directory.mkdirs();
        String[] styles={SettingsProvider.FILLED,SettingsProvider.PORTRAIT,SettingsProvider.DASHED,SettingsProvider.CIRCLE};
        for(int frame=0;frame<24;frame++) {
            Bitmap sheet=Bitmap.createBitmap(640,540,Bitmap.Config.ARGB_8888);
            Canvas canvas=new Canvas(sheet);canvas.drawColor(Color.rgb(24,27,32));
            Paint label=new Paint(Paint.ANTI_ALIAS_FLAG);label.setColor(Color.WHITE);label.setTextSize(20);
            for(int i=0;i<4;i++) {
                canvas.drawBitmap(render(styles[i],frame/24f,true,75),(i%2)*320,(i/2)*270,null);
                canvas.drawText((SettingsProvider.PORTRAIT.equals(styles[i]) ? "Landscape J" : styles[i])+" • 75%",(i%2)*320+70,(i/2)*270+250,label);
            }
            try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(directory,String.format("frame-%02d.png",frame)))) {
                sheet.compress(Bitmap.CompressFormat.PNG,100,out);
            }
        }
    }
    @Test public void animationPreferenceRoundTripPreservesOtherBatteryOptions() {
        SettingsProvider p=Robolectric.buildContentProvider(SettingsProvider.class).create().get();
        assertTrue(p.call("get",null,null).getBoolean("charging_animation"));
        p.call("set",SettingsProvider.PORTRAIT,null);p.call("set_percentage","true",null);
        p.call("set_animation","false",null);
        var b=p.call("get",null,null);assertFalse(b.getBoolean("charging_animation"));
        assertTrue(b.getBoolean("show_percentage"));assertEquals(SettingsProvider.PORTRAIT,b.getString("style"));
    }
}
