# 3.9.0 validation

Completed locally:
- Recovered 3.8.2 source ZIP and verified all original locked SHA-256 digests.
- Compiled changed battery placement, battery hook, roaming hook and Home UI, including their referenced source dependencies, against Android/Xposed APIs with Java 17 source/target settings. Only deprecation/toolchain warnings.
- Eight battery hierarchy checks using a lightweight View fixture passed. These validate branch selection and anchor lookup, not Android measurement or rendering.
- Source-integrity, manifest/XML and release ZIP checks passed.

Added six Robolectric BatteryPlacementTest cases and a UI regression test for the roaming toggle's default/off/independent state. These, full existing tests, lint, APK compilation and signature/version checks run in GitHub Actions and were not executed locally. No device or Iconify debug-3054 binary testing is claimed.

Device validation: dual-row placement, swap rows, rotation and lock/unlock; roaming R hidden/restored after restart; existing battery animation/appearance and other components preserved.
