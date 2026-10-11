package dev.chet.mypixelmodpack;

import android.os.Handler;
import android.os.Looper;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

/** Retry only the explicit no-hooks-installed case; never poll forever. */
final class StartupRetry {
    private static final long[] DELAYS = {250, 1000, 3000, 10000};
    private final Map<String, Runnable> pending = new HashMap<>();
    private Handler handler;
    synchronized void schedule(String key, BooleanSupplier attempt) {
        if (pending.containsKey(key)) return;
        if (handler == null) handler = new Handler(Looper.getMainLooper());
        Runnable task = new Runnable() {
            int index;
            @Override public void run() {
                boolean complete = attempt.getAsBoolean();
                synchronized (StartupRetry.this) {
                    if (complete || ++index == DELAYS.length) pending.remove(key);
                    else handler.postDelayed(this, DELAYS[index]);
                }
            }
        };
        pending.put(key, task);
        handler.postDelayed(task, DELAYS[0]);
    }
}
