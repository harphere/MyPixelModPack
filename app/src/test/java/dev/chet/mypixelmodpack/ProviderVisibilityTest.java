package dev.chet.mypixelmodpack;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35, manifest=Config.NONE)
public class ProviderVisibilityTest {
    @Test public void grantsKnownHostsReadOnlyVisibilityWithoutChangingSettings() {
        Context app=RuntimeEnvironment.getApplication();
        app.getSharedPreferences("features_v2",0).edit().putBoolean("match",true).putBoolean("wake",false).commit();
        RecordingContext ctx=new RecordingContext(app);
        var result=ProviderVisibility.grantToPackages(ctx,List.of("com.eero.android","org.kman.AquaMail","com.costco.app.android"));
        assertEquals(3,result.granted()); assertTrue(result.failures().isEmpty());
        for(int flags:ctx.flags) assertEquals(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,flags);
        for(Uri uri:ctx.uris) assertEquals("content://dev.chet.mypixelmodpack.settings.v2/visibility",uri.toString());
        assertTrue(app.getSharedPreferences("features_v2",0).getBoolean("match",false));
        assertFalse(app.getSharedPreferences("features_v2",0).getBoolean("wake",true));
    }
    @Test public void oneRejectedGrantDoesNotPreventOtherHosts() {
        RecordingContext ctx=new RecordingContext(RuntimeEnvironment.getApplication()); ctx.rejected="blocked.app";
        var result=ProviderVisibility.grantToPackages(ctx,List.of("com.eero.android","blocked.app","com.whatsapp"));
        assertEquals(2,result.granted()); assertEquals(1,result.failures().size());
        assertTrue(result.failures().get(0).contains("blocked.app")); assertTrue(ctx.packages.contains("com.whatsapp"));
    }
    @Test public void skipsSelfAndDuplicatePackages() {
        RecordingContext ctx=new RecordingContext(RuntimeEnvironment.getApplication());
        var result=ProviderVisibility.grantToPackages(ctx,List.of(ctx.getPackageName(),"com.eero.android","com.eero.android"));
        assertEquals(1,result.granted()); assertEquals(List.of("com.eero.android"),ctx.packages);
    }
    private static final class RecordingContext extends ContextWrapper {
        final List<String> packages=new ArrayList<>(); final List<Uri> uris=new ArrayList<>(); final List<Integer> flags=new ArrayList<>(); String rejected;
        RecordingContext(Context base) { super(base); }
        @Override public void grantUriPermission(String pkg,Uri uri,int mode) {
            if(pkg.equals(rejected)) throw new SecurityException("test rejection");
            packages.add(pkg); uris.add(uri); flags.add(mode);
        }
    }
}
