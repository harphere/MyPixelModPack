package dev.chet.mypixelmodpack;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.os.UserManager;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import static org.robolectric.Shadows.shadowOf;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 35, manifest = Config.NONE)
public class UnlockRetryTest {
    @Test public void lockedStartupRetriesOnceOnUnlock() {
        Context context = RuntimeEnvironment.getApplication();
        shadowOf(context.getSystemService(UserManager.class)).setUserUnlocked(false);
        AtomicInteger calls = new AtomicInteger();
        assertTrue(UnlockRetry.waitForUnlock(context, calls::incrementAndGet));
        assertEquals(0, calls.get());
        shadowOf(context.getSystemService(UserManager.class)).setUserUnlocked(true);
        context.sendBroadcast(new Intent(Intent.ACTION_USER_UNLOCKED));
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(1, calls.get());
        context.sendBroadcast(new Intent(Intent.ACTION_USER_UNLOCKED));
        shadowOf(Looper.getMainLooper()).idle();
        assertEquals(1, calls.get());
    }
    @Test public void unlockedStartupDoesNotWaitOrScheduleExtraInstallation() {
        Context context = RuntimeEnvironment.getApplication();
        shadowOf(context.getSystemService(UserManager.class)).setUserUnlocked(true);
        AtomicInteger calls = new AtomicInteger();
        assertFalse(UnlockRetry.waitForUnlock(context, calls::incrementAndGet));
        assertEquals(0, calls.get());
    }
}
