# Charging visibility update 3.6.0

- 1.4-second cycle instead of 1.8 seconds.
- Wider, brighter outward pulse for filled circles.
- Wider upward sweep for portrait batteries; orientation rotates the sweep too.
- Ring and dashed-ring highlights chase the entire track, including at low charge.
- Battery level fill, colour scheme, size controls, percentage option and bolt remain.
- Stops when full, unplugged, hidden or screen off, as before.

Build with the included GitHub Actions workflow. Source validation was run locally;
Android unit tests, release compilation and lint run in Actions. Visibility and
charging lifecycle still need testing on the phone.
