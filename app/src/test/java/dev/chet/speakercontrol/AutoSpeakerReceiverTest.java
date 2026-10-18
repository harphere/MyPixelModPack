package dev.chet.speakercontrol;
import android.content.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35,manifest=Config.NONE)
public class AutoSpeakerReceiverTest {
 Context context=RuntimeEnvironment.getApplication();
 AutoSpeakerReceiver.Session session(){var s=new AutoSpeakerReceiver.Session(context,null,Protocol.APP,"user-request","TOGGLE");s.targets.add("com.whatsapp");s.targets.add("phone.app");return s;}
 Intent status(AutoSpeakerReceiver.Session s,String pkg){return new Intent(Protocol.PREFIX+"RESULT").putExtra(Protocol.KEY,Protocol.TOKEN).putExtra("request_id",s.probe).putExtra("backend_package",pkg).putExtra("command","STATUS").putExtra("success",true).putExtra("call_active",true);}
 List<Intent> sent(){return Shadows.shadowOf((android.app.Application)context).getBroadcastIntents();}
 @Test public void duplicateReportsDispatchOnlyOneTargetedToggleAndStaleReportsAreIgnored(){
  var s=session();s.onReceive(context,status(s,"com.whatsapp"));s.onReceive(context,status(s,"com.whatsapp"));
  s.onReceive(context,status(s,"phone.app").putExtra("request_id","old"));
  int before=sent().size();s.choose();
  assertEquals(before+1,sent().size());Intent command=sent().get(before);
  assertEquals("com.whatsapp",command.getPackage());assertEquals(Protocol.PREFIX+"TOGGLE",command.getAction());
  s.onReceive(context,status(s,"phone.app"));assertEquals(before+1,sent().size());s.finish();
 }
 @Test public void ambiguityReturnsFailureWithoutAnyToggle(){
  var s=session();s.onReceive(context,status(s,"com.whatsapp"));s.onReceive(context,status(s,"phone.app"));
  int before=sent().size();s.choose();assertEquals(before+1,sent().size());
  Intent result=sent().get(before);assertEquals(Protocol.PREFIX+"RESULT",result.getAction());assertFalse(result.getBooleanExtra("success",true));assertEquals("user-request",result.getStringExtra("request_id"));
 }
 @Test public void missingOrUnauthenticatedReportsNeverDispatchToggle(){
  var s=session();s.onReceive(context,status(s,"com.whatsapp").putExtra(Protocol.KEY,"wrong"));
  s.onReceive(context,status(s,"unscoped.app"));int before=sent().size();s.choose();
  assertEquals(Protocol.PREFIX+"RESULT",sent().get(before).getAction());assertFalse(sent().get(before).getBooleanExtra("success",true));
 }
}
