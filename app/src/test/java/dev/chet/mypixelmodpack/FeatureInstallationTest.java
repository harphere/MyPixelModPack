package dev.chet.mypixelmodpack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class FeatureInstallationTest {
    @Test public void offFeatureNeverConstructsOrInvokesItsModule() {
        AtomicInteger calls = new AtomicInteger();
        List<String> messages = new ArrayList<>();
        FeatureInstallation.run("wake", false, calls::incrementAndGet,
            (feature, status, error) -> messages.add(feature + ":" + status));
        assertEquals(0, calls.get());
        assertEquals(List.of("wake:OFF"), messages);
    }
    @Test public void failedModuleCannotPreventTheNextFeatureFromStarting() {
        AtomicInteger calls = new AtomicInteger();
        List<Throwable> errors = new ArrayList<>();
        FeatureInstallation.Reporter reporter = (feature, status, error) -> { if (error != null) errors.add(error); };
        FeatureInstallation.run("wake", true, () -> { throw new ExceptionInInitializerError("early initialization"); }, reporter);
        FeatureInstallation.run("battery", true, calls::incrementAndGet, reporter);
        assertEquals(1, calls.get());
        assertEquals(1, errors.size());
        assertTrue(errors.get(0) instanceof ExceptionInInitializerError);
    }
}
