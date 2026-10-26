package dev.chet.batterygradient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35)
public class BatteryShortcutSettingsTest {
 @Test public void shortcutIsOptionalAndPreservesAppearance(){
  var p=Robolectric.buildContentProvider(SettingsProvider.class).create().get();
  assertFalse(p.call("get",null,null).getBoolean("tap_settings"));
  p.call("set",SettingsProvider.PORTRAIT,null);p.call("set_percentage","true",null);
  p.call("set_tap_settings","true",null);var enabled=p.call("get",null,null);
  assertTrue(enabled.getBoolean("tap_settings"));assertTrue(enabled.getBoolean("show_percentage"));assertEquals(SettingsProvider.PORTRAIT,enabled.getString("style"));
  p.call("set_tap_settings","invalid",null);assertTrue(p.call("get",null,null).getBoolean("tap_settings"));
  p.call("set_tap_settings","false",null);assertFalse(p.call("get",null,null).getBoolean("tap_settings"));
 }
}
