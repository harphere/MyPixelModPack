package dev.chet.mypixelmodpack;

import android.app.Activity;
import android.widget.Switch;
import android.view.View;
import android.view.ViewGroup;
import dev.chet.chromepiestatusmatch.settings.PieSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class CombinedSettingsTest {
    @Test public void chromeSettingsUseIsolatedPrivateStoreAndProviderSnapshot() {
        var controller=Robolectric.buildActivity(PieSettings.class).setup();
        Activity activity=controller.get();
        var fragment=(android.preference.PreferenceFragment)activity.getFragmentManager().findFragmentById(android.R.id.content);
        assertEquals("chromepie_v1",fragment.getPreferenceManager().getSharedPreferencesName());
        assertEquals(0,fragment.getPreferenceManager().getSharedPreferencesMode());
        activity.getSharedPreferences("chromepie_v1",0).edit().putString("pie_icon_color_mode","white").commit();
        PackSettingsProvider provider=Robolectric.buildContentProvider(PackSettingsProvider.class).create().get();
        assertEquals("white",provider.call("snapshot","chromepie_v1",null).getString("pie_icon_color_mode"));
        assertFalse(activity.getSharedPreferences("features_v2",0).contains("pie_icon_color_mode"));
        assertFalse(activity.getSharedPreferences("features_v2",0).getBoolean("speaker",false));
        assertFalse(activity.getSharedPreferences("features_v2",0).getBoolean("pie",false));
        controller.pause().stop().destroy();
    }
    @Test public void batteryUiHasWorkingAnimationSwitch() {
        var controller=Robolectric.buildActivity(dev.chet.batterygradient.SettingsActivity.class).setup();
        Switch animation=find(controller.get().findViewById(android.R.id.content),"Charging animation");
        assertNotNull(animation);animation.setChecked(false);
        assertFalse(controller.get().getSharedPreferences("battery",0).getBoolean("charging_animation",true));
        controller.pause().stop().destroy();
    }
    Switch find(View v,String title) {
        if(v instanceof Switch && title.contentEquals(((Switch)v).getText())) return (Switch)v;
        if(v instanceof ViewGroup) for(int i=0;i<((ViewGroup)v).getChildCount();i++) {
            Switch found=find(((ViewGroup)v).getChildAt(i),title);if(found!=null)return found;
        }
        return null;
    }
}
