package dev.chet.mypixelmodpack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35, manifest = Config.NONE)
public class PackSettingsTest {
    private Context context;
    private PackSettingsProvider provider;
    @Before public void setup() {
        context = RuntimeEnvironment.getApplication();
        provider = Robolectric.buildContentProvider(PackSettingsProvider.class).create().get();
        ShadowContentResolver.registerProviderInternal(PackSettingsProvider.URI.getAuthority(), provider);
        PackRuntime.attach(context, context.getPackageName(), context.getPackageName());
    }
    @Test public void oldSwitchesCannotEnableRefreshedHooks() {
        context.getSharedPreferences("features", 0).edit().putBoolean("wake", true).putBoolean("battery", true).commit();
        SharedPreferences fresh = PackRuntime.preferences("features_v2");
        assertFalse(fresh.getBoolean("wake", false));
        assertFalse(fresh.getBoolean("battery", false));
    }
    @Test public void navigationSettingsRemainIndependent() {
        context.getSharedPreferences("nav_icons_v2", 0).edit().putString("style", "marks").commit();
        context.getSharedPreferences("nav_match_v2", 0).edit().putBoolean("enabled", false)
            .putStringSet("blacklist", Set.of("example.app")).commit();
        SharedPreferences icons = PackRuntime.preferences("nav_icons_v2");
        SharedPreferences match = PackRuntime.preferences("nav_match_v2");
        assertEquals("marks", icons.getString("style", "mixed"));
        assertFalse(icons.contains("enabled"));
        assertFalse(match.getBoolean("enabled", true));
        assertFalse(match.contains("style"));
        assertEquals(Set.of("example.app"), match.getStringSet("blacklist", Set.of()));
    }
    @Test public void startupReadsOneStableSwitchSnapshot() {
        SharedPreferences local = context.getSharedPreferences("features_v2", 0);
        local.edit().putBoolean("battery", true).commit();
        SharedPreferences snapshot = PackRuntime.preferences("features_v2");
        local.edit().putBoolean("battery", false).commit();
        assertTrue(snapshot.getBoolean("battery", false));
        assertFalse(PackRuntime.preferences("features_v2").getBoolean("battery", true));
    }
    @Test public void missingProviderIsNotMisreportedAsAllSwitchesOff() {
        ShadowContentResolver.registerProviderInternal(PackSettingsProvider.URI.getAuthority(), null);
        try {
            PackRuntime.preferences("features_v2");
            fail("Missing provider must be reported");
        } catch (IllegalStateException expected) { }
    }
    @Test public void arbitraryPrivatePreferencesAreNotExposed() {
        try { provider.call("snapshot", "battery", Bundle.EMPTY); fail("Store must be allowlisted"); }
        catch (IllegalArgumentException expected) { }
    }
}
