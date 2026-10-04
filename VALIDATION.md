# Validation — 2.1.3

October 4, 2026 UTC (October 3 in America/Regina).

- All 32 release unit tests passed with zero failures/errors. Added rendering tests verify percentage visibility for all four styles, exact preservation of the entire battery drawing when the charging bolt appears, the separate bolt lane, and persistence/default/override independence of the percentage preference.
- The existing radial shrink, portrait gradient, provider isolation, startup, saved-setting and UI regression tests passed.
- Release assembly and lint passed with no Error/Fatal findings; existing lint warnings remain. APK signature and single legacy dispatcher checks passed.
- Native Robolectric preview rendering shows circle and portrait at five levels with percentage hidden, percentage enabled, and charging bolts. Visually inspected; see docs/battery-preview.png. This is the actual drawable rendered off-device, not a device screenshot.
- Signing key and production Java outside Battery Gradient are unchanged from 2.1.2 except version labels. The working 2.0.2 provider-access fix is preserved.
- Build used Java 17, Gradle 8.11.1, SDK 36 and build tools 35.0.0. Temporary local proxy/preview configuration is excluded; GH Actions performs the normal checks.

Physical-device appearance and the widened 32 × 24 dp slot with Iconify have not been tested here. Row discovery and movement logic are retained, with an 8 dp lane reserved for the bolt. Percentage switches use the existing live provider observer. Optional Settings shortcut compatibility is unchanged.
