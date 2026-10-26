package dev.chet.batterygradient;
import android.content.Context;
import android.content.Intent;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedBridge;
final class BatterySettingsShortcut {
 static void open(Context context) {
  Intent intent=new Intent("android.intent.action.POWER_USAGE_SUMMARY").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  try {
   ClassLoader loader=context.getClassLoader();
   Class<?> type=XposedHelpers.findClass("com.android.systemui.plugins.ActivityStarter",loader);
   Class<?> dependency=XposedHelpers.findClass("com.android.systemui.Dependency",loader);
   Object starter=XposedHelpers.callStaticMethod(dependency,"get",type);
   XposedHelpers.callMethod(starter,"postStartActivityDismissingKeyguard",intent,0);
   return;
  } catch(Throwable unavailable) { /* Some ROMs don't publish ActivityStarter through Dependency. */ }
  try { context.startActivity(intent); }
  catch(RuntimeException error) { XposedBridge.log("BatteryGradient: Battery settings unavailable: "+error); }
 }
}
