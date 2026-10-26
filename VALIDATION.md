# 3.8.0 validation

Local validation: ten pure-Java gesture assertions passed (tap, drag cancellation, returning after drag, multi-touch, long press, explicit cancellation, fresh gesture and invalid timestamp). source integrity, manifest/XML checks, and Java compilation of changed runtime classes against Android/Xposed APIs. Full Android unit tests, lint and APK packaging run in the included GitHub Actions workflow. No physical-device testing is claimed.

Device checks: enable Tap notification icons, reboot, test each app separately and with all three present. Each tap should open exactly one app; swipe down, long press and unsupported icons should retain normal behaviour. Check locked-screen behaviour. Inspect Waves at each font size and LTE/4G/5G labels for clipping. Existing battery tap is independently configured under Battery gradient → Customize.
