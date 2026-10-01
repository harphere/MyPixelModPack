package dev.chet.mypixelmodpack;

import java.util.HashSet;
import java.util.Set;

/** Multiple lifecycle boundaries may fire; never install the same hooks twice. */
final class ProcessStartup {
    interface Attempt { boolean run() throws Throwable; }
    private final Set<String> claimed = new HashSet<>();
    void run(String key, Attempt attempt) throws Throwable {
        synchronized (this) { if (!claimed.add(key)) return; }
        // Retry only an explicit no-hooks-installed result. An unexpected exception
        // could leave partial hooks installed, so retain the claim until reboot.
        boolean retry = false;
        try { retry = !attempt.run(); }
        finally { if (retry) synchronized (this) { claimed.remove(key); } }
    }
}
