package dev.chet.mypixelmodpack;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35, application=UiApplication.class)
public class FeatureUiTest {
    @Test public void wrapperPreservesNavigationStyleControlsAndDoesNotNestOnResume() {
        Context c=RuntimeEnvironment.getApplication();
        c.getSharedPreferences("nav_icons_v2",0).edit().putString("style","dots").commit();
        try(var controller=Robolectric.buildActivity(com.chet.navdotstyle.MainActivity.class).setup()) {
            var a=controller.get(); ViewGroup content=a.findViewById(android.R.id.content);
            assertEquals("pack-ui-wrapper",content.getChildAt(0).getTag());
            RadioButton marks=a.findViewById(1002); marks.performClick();
            assertEquals("marks",c.getSharedPreferences("nav_icons_v2",0).getString("style",""));
            controller.pause().resume();
            assertEquals(1,countWrappers(content)); assertNotNull(a.findViewById(1002));
        }
    }
    @Test @Config(qualifiers="night") public void featureTextUsesDarkThemePalette() {
        try(var controller=Robolectric.buildActivity(com.chet.navdotstyle.MainActivity.class).setup()) {
            var a=controller.get(); assertTrue(PackUi.dark(a));
            RadioButton marks=a.findViewById(1002);
            assertEquals(PackUi.text(a),marks.getCurrentTextColor());
        }
    }
    @Test public void blacklistKeepsUsableScrollAreaUnderTheHeader() {
        try(var controller=Robolectric.buildActivity(com.chet.navbarmatch.MainActivity.class).setup()) {
            ViewGroup content=controller.get().findViewById(android.R.id.content);
            content.measure(View.MeasureSpec.makeMeasureSpec(1080,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(2200,View.MeasureSpec.EXACTLY));
            content.layout(0,0,1080,2200);
            android.widget.ScrollView list=findScroll(content);
            assertNotNull(list); assertTrue("Blacklist should retain space to scroll",list.getHeight()>500);
            assertEquals(1,countWrappers(content));
        }
    }
    private static android.widget.ScrollView findScroll(View v) {
        if(v instanceof android.widget.ScrollView) return (android.widget.ScrollView)v;
        if(v instanceof ViewGroup) { var g=(ViewGroup)v; for(int i=0;i<g.getChildCount();i++) { var found=findScroll(g.getChildAt(i)); if(found!=null) return found; } }
        return null;
    }
    private static int countWrappers(View v) {
        int count="pack-ui-wrapper".equals(v.getTag()) ? 1:0;
        if(v instanceof ViewGroup) { var group=(ViewGroup)v; for(int i=0;i<group.getChildCount();i++) count+=countWrappers(group.getChildAt(i)); }
        return count;
    }
}
