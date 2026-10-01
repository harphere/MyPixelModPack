package dev.chet.mypixelmodpack;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessStartupTest {
    @Test public void alreadyAttachedAndOnCreateSignalsInstallOnlyOnce() throws Throwable {
        ProcessStartup startup = new ProcessStartup();
        AtomicInteger calls = new AtomicInteger();
        ProcessStartup.Attempt install = () -> { calls.incrementAndGet(); return true; };
        startup.run("launcher:main", install);
        startup.run("launcher:main", install);
        startup.run("gboard:main", install);
        assertEquals(2, calls.get());
    }
    @Test public void unavailableSettingsCanRetryAtTheNextLifecycleBoundary() throws Throwable {
        ProcessStartup startup = new ProcessStartup();
        AtomicInteger calls = new AtomicInteger();
        startup.run("launcher:main", () -> { calls.incrementAndGet(); return false; });
        startup.run("launcher:main", () -> { calls.incrementAndGet(); return true; });
        startup.run("launcher:main", () -> { calls.incrementAndGet(); return true; });
        assertEquals(2, calls.get());
    }
    @Test public void unexpectedPartialFailureCannotInstallDuplicateHooks() throws Throwable {
        ProcessStartup startup = new ProcessStartup();
        AtomicInteger calls = new AtomicInteger();
        try { startup.run("launcher:main", () -> { calls.incrementAndGet(); throw new LinkageError("partial installation"); }); }
        catch (LinkageError expected) { }
        startup.run("launcher:main", () -> { calls.incrementAndGet(); return true; });
        assertEquals(1, calls.get());
    }
}
