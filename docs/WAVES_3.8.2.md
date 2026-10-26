# Larger Waves artwork — 3.8.2

The outer wave radius increases from 31% to 64% of the drawable height (about 2.06×), and the stroke from 6.5% to 10.5% (about 1.62×). At a 16dp drawable height this gives an outer arc radius of 10.24dp and a 1.68dp stroke. The two arcs now occupy a comparable height to the letters. A separate gap prevents overlap with G.

Intrinsic width grows by another 6.4dp, while glyph scale compensates for this extra width so the accepted text size/font is retained. Android/SystemUI can still constrain the ImageView; confirm appearance on the device. No status-bar layout hooks or other components change.

YAATA notification icon taps were reported working in 3.8.1; Phone and AquaMail remain awaiting user testing. The tap implementation is unchanged in this release.

Build the complete ZIP through GitHub Actions, install the APK, then reboot/restart System UI. Keep Waves selected under Status bar → 4G/5G icon styles. Prior settings are retained.

The separate charging lightning bolt is removed from every battery style. Charging animation, percentage, sizing, orientation and tap shortcut are retained. Battery layout and size are preserved. The existing rendering test now verifies charging adds no overlay when animation is disabled; animation tests remain unchanged.
