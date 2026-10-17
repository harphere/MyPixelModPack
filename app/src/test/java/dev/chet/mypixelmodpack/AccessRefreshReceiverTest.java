package dev.chet.mypixelmodpack;
import android.content.*;
import android.net.Uri;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35,manifest=Config.NONE)
public class AccessRefreshReceiverTest {
    @Test public void installAndUpdateGrantOnlyChangedPackageWithoutChangingSwitches() {
        var app=RuntimeEnvironment.getApplication();
        app.getSharedPreferences("features_v2",0).edit().putBoolean("match",true).commit();
        var ctx=new RecordingContext(app);var receiver=new AccessRefreshReceiver();
        for(String action:new String[]{Intent.ACTION_PACKAGE_ADDED,Intent.ACTION_PACKAGE_REPLACED})
            receiver.onReceive(ctx,new Intent(action,Uri.parse("package:org.telegram.messenger")));
        assertEquals(List.of("org.telegram.messenger","org.telegram.messenger"),ctx.packages);
        assertTrue(app.getSharedPreferences("features_v2",0).getBoolean("match",false));
        receiver.onReceive(ctx,new Intent(Intent.ACTION_PACKAGE_ADDED));
        receiver.onReceive(ctx,new Intent("unrelated",Uri.parse("package:other.app")));
        receiver.onReceive(ctx,new Intent(Intent.ACTION_PACKAGE_ADDED,Uri.parse("package:"+ctx.getPackageName())));
        assertEquals(2,ctx.packages.size());
    }
    private static final class RecordingContext extends ContextWrapper {
        final List<String> packages=new ArrayList<>();
        RecordingContext(Context context){super(context);}
        @Override public void grantUriPermission(String pkg,Uri uri,int flags){
            assertEquals(ProviderVisibility.VISIBILITY_URI,uri);
            assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,flags);
            packages.add(pkg);
        }
    }
}
