# Double-tap wake reliability — 3.7.0

Baseline: complete 3.6.0-buildfix; versionCode 47. All other runtime features preserved.

The supplied USB log showed accepted 167 ms and 185 ms sensor-event pairs. It did not establish why physical triple taps were sometimes needed. User reports DT2W works when plugged in, AOD disabled. This revision targets a plausible CPU-suspend/re-arm race; it is not a confirmed fix for the specific phone.

- Adds a non-reference-counted PARTIAL_WAKE_LOCK on the first suppressed sensor event, with a hard 650 ms timeout. It keeps CPU processing available briefly, not the screen. Releases after second native sensor handling, window expiry, sensor replacement or failure. A failed/unavailable wake-lock setup cannot permanently hold the CPU.
- Uses elapsedRealtime for the 500 ms pair window. Callback delays and wall-clock changes cannot pair a stale tap. Ignores sub-40 ms duplicate events without shifting the first timestamp.
- Captures tap sensors during updateListening as well as construction. Checks mRegistered after re-arm. This is SystemUI registration bookkeeping, NOT proof of hardware readiness; if unavailable/false, stock behaviour is retained for the event.
- Logs the native A16 pulsing double-tap result without changing it. Android's wake, proximity and falsing checks remain in charge. Does not force wake or bypass pocket checks.
- Slightly widens the window from 400 ms to 500 ms for tolerance; prior captured pairs were already within 400 ms.

Build and install as an update, reboot, then test unplugged with AOD still off after short and longer idle periods. Compare wake rate and idle battery use. Extra CPU hold is bounded but may add a small cost if the sensor fires frequently. If sensor hardware misses the first physical tap or the ROM rejects a wake, software pairing alone may not resolve it. Disable the Pack DT2W switch and reboot to revert to ROM wake handling.
