package dev.chet.mypixelmodpack;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
public class HomeActivityTest {
    @Test public void upgradeRetainsFeatureAndAppearanceSettings() {
        Context c=RuntimeEnvironment.getApplication();
        c.getSharedPreferences("features_v2",0).edit().clear().putBoolean("cursor",true).putBoolean("battery",true).commit();
        c.getSharedPreferences("nav_icons_v2",0).edit().putString("style","marks").commit();
        try(var activity=Robolectric.buildActivity(HomeActivity.class).setup()) {
            assertTrue(c.getSharedPreferences("features_v2",0).getBoolean("cursor",false));
            assertTrue(c.getSharedPreferences("features_v2",0).getBoolean("battery",false));
            assertEquals("marks",c.getSharedPreferences("nav_icons_v2",0).getString("style",""));
            assertFalse(c.getSharedPreferences("features_v2",0).contains("settings_entry"));
        }
    }
    @Test public void navigationSwitchChangesOnlyItsOwnFeature() {
        Context c=RuntimeEnvironment.getApplication(); SharedPreferences p=c.getSharedPreferences("features_v2",0);
        p.edit().clear().putBoolean("battery",true).putBoolean("opa",true).commit();
        Intent intent=new Intent(c,HomeActivity.class).putExtra("section","Navigation");
        try(var controller=Robolectric.buildActivity(HomeActivity.class,intent).setup()) {
            Switch cursor=find(controller.get().findViewById(android.R.id.content),"Gboard cursor arrows");
            assertNotNull(cursor); cursor.setChecked(true);
            assertTrue(p.getBoolean("cursor",false)); assertTrue(p.getBoolean("opa",false)); assertTrue(p.getBoolean("battery",false));
            assertFalse(p.contains("wake"));
        }
    }
    @Test public void settingsShortcutIsOptInAndDoesNotChangeFeatureSwitches() {
        Context c=RuntimeEnvironment.getApplication(); SharedPreferences p=c.getSharedPreferences("features_v2",0);
        p.edit().clear().putBoolean("wake",true).commit();
        Intent intent=new Intent(c,HomeActivity.class).putExtra("section","App & diagnostics");
        try(var controller=Robolectric.buildActivity(HomeActivity.class,intent).setup()) {
            Switch shortcut=find(controller.get().findViewById(android.R.id.content),"Show in Android Settings");
            assertFalse(shortcut.isChecked()); shortcut.setChecked(true);
            assertTrue(p.getBoolean("settings_entry",false)); assertTrue(p.getBoolean("wake",false));
        }
    }
    @Test public void copiedDiagnosticsKeepProviderAccessAndMatchConfiguration() {
        Context c=RuntimeEnvironment.getApplication();
        c.getSharedPreferences("nav_match_v2",0).edit().putBoolean("enabled",false).putStringSet("blacklist",java.util.Set.of("excluded.app")).commit();
        c.getSharedPreferences("access_diagnostics_v2",0).edit().putString("result","App access refreshed: 12 grants; 0 failures.").commit();
        Intent intent=new Intent(c,HomeActivity.class).putExtra("section","App & diagnostics");
        try(var controller=Robolectric.buildActivity(HomeActivity.class,intent).setup()) {
            android.widget.TextView title=findLabel(controller.get().findViewById(android.R.id.content),"Startup diagnostics");
            assertNotNull(title); ((View)title.getParent()).performClick();
            var dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();
            org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
            var clipboard=(android.content.ClipboardManager)c.getSystemService(Context.CLIPBOARD_SERVICE);
            String report=clipboard.getPrimaryClip().getItemAt(0).getText().toString();
            assertTrue(report.contains("module_enabled=false")); assertTrue(report.contains("excluded.app"));
            assertTrue(report.contains("12 grants; 0 failures")); assertTrue(report.contains("settings_entry="));
        }
    }
    private static android.widget.TextView findLabel(View v,String prefix) {
        if(v instanceof android.widget.TextView && ((android.widget.TextView)v).getText().toString().startsWith(prefix)) return (android.widget.TextView)v;
        if(v instanceof ViewGroup) { var g=(ViewGroup)v; for(int i=0;i<g.getChildCount();i++) { var found=findLabel(g.getChildAt(i),prefix); if(found!=null) return found; } }
        return null;
    }
    private static Switch find(View v,String label) {
        if(v instanceof Switch && label.contentEquals(((Switch)v).getText())) return (Switch)v;
        if(v instanceof ViewGroup) { ViewGroup g=(ViewGroup)v; for(int i=0;i<g.getChildCount();i++) { Switch found=find(g.getChildAt(i),label); if(found!=null) return found; } }
        return null;
    }
}
