# Validation — 3.3.0

October 14, 2026. Baseline: complete 3.2.1 source, including brighter charging animations. VersionCode 41.

- Changed mobile provider, settings activity and drawable compiled successfully against an Android API jar using JDK 17.
- Complete-source and dispatcher checks passed locally.
- Added Android rendering tests for Karyl/Nishiki labels, tint, transparency and sizing, plus removed-style migration tests. These tests have NOT been run locally; the supplied Actions workflow runs the full Android unit suite, release build and lint and checks APK identity/signature.
- Reference appearance is recreated with original rounded vector glyphs, not the original Weeabooify font/assets. Actual SystemUI sizing and tint need phone testing.
- Existing mobile hooks retained; 3G resource replacement added if that resource exists on the ROM.

## Build-test correction

User Actions log: 62 tests ran, 61 passed; only chargingBoltOccupiesOnlyRightLane failed. The static render helper now disables animation so the bolt can be tested independently. ChargingAnimationTest remains unchanged. Full corrected suite has not been rerun locally; rerun Actions. No runtime code, version or signing changes.
