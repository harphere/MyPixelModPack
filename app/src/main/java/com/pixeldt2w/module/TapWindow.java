package com.pixeldt2w.module;

/** Elapsed-realtime tap pairing; independent of Handler scheduling and wall clock. */
final class TapWindow {
    static final long WINDOW_MS=500L;
    static final long MIN_GAP_MS=40L;
    enum Result { FIRST, SECOND, DUPLICATE }
    private long first=-1;
    synchronized Result tap(long now) {
        if(first>=0 && now>=first) {
            long gap=now-first;
            if(gap<MIN_GAP_MS)return Result.DUPLICATE;
            if(gap<=WINDOW_MS){first=-1;return Result.SECOND;}
        }
        first=now;return Result.FIRST;
    }
    synchronized void reset(){first=-1;}
}
