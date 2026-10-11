package dev.chet.mypixelmodpack;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=35, manifest=Config.NONE)
public class StartupRetryTest {
    @Test public void transientSettingsFailureRetriesAndStopsAfterSuccess() {
        StartupRetry retry = new StartupRetry(); AtomicInteger count = new AtomicInteger();
        retry.schedule("host", () -> count.incrementAndGet() >= 2);
        retry.schedule("host", () -> { fail("duplicate schedule"); return true; });
        var looper = Shadows.shadowOf(android.os.Looper.getMainLooper());
        looper.idleFor(Duration.ofMillis(250)); assertEquals(1, count.get());
        looper.idleFor(Duration.ofMillis(1000)); assertEquals(2, count.get());
        looper.idleFor(Duration.ofSeconds(30)); assertEquals(2, count.get());
    }
    @Test public void inaccessibleProviderStopsAfterFourAttempts() {
        StartupRetry retry = new StartupRetry(); AtomicInteger count = new AtomicInteger();
        retry.schedule("host", () -> { count.incrementAndGet(); return false; });
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMinutes(1));
        assertEquals(4, count.get());
    }
}
