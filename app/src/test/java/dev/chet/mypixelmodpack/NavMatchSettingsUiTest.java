package dev.chet.mypixelmodpack;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35, manifest=Config.NONE)
public class NavMatchSettingsUiTest {
    @Test public void checkboxReflectsSavedStateAndDoesNotResetBlacklist() {
        Context c=RuntimeEnvironment.getApplication();
        var prefs=c.getSharedPreferences("nav_match_v2",0);
        prefs.edit().putBoolean("enabled",false).putStringSet("blacklist",Set.of("excluded.app")).commit();
        try(var controller=Robolectric.buildActivity(com.chet.navbarmatch.MainActivity.class).setup()) {
            var activity=controller.get(); CheckBox toggle=find(activity.findViewById(android.R.id.content));
            assertNotNull(toggle); assertFalse(toggle.isChecked());
            toggle.setChecked(true); assertTrue(prefs.getBoolean("enabled",false));
            assertEquals(Set.of("excluded.app"),prefs.getStringSet("blacklist",Set.of()));
            controller.pause().stop(); prefs.edit().putBoolean("enabled",false).commit(); controller.start().resume();
            assertFalse(toggle.isChecked());
        }
    }
    private static CheckBox find(View v) {
        if(v instanceof CheckBox && "Module enabled".contentEquals(((CheckBox)v).getText())) return (CheckBox)v;
        if(v instanceof ViewGroup) { var group=(ViewGroup)v; for(int i=0;i<group.getChildCount();i++) { CheckBox found=find(group.getChildAt(i)); if(found!=null)return found; } }
        return null;
    }
}
