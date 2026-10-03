# Validation — 2.1.2

October 3, 2026 UTC (October 2 in America/Regina).

- All 29 release unit tests passed with no failures/errors. The four added battery tests verify actual rendered pixels for radial shrink, full opacity, removal of top portrait fill, retention of lower warm colours, and persisted portrait selection/override/reset.
- Native Robolectric rendering produced a two-row preview at 100%, 75%, 50%, 25% and 10%; visually inspected. This is the actual drawable rendered off-device, not an illustration or physical-device screenshot. See docs/battery-preview.png.
- Clean release assembly, release lint and APK signature verification passed. Lint has zero Error/Fatal findings; pre-existing warnings remain.
- Source and APK checks confirm a single legacy dispatcher and the preserved 2.0.2 provider-access fix.
- Byte comparison against 2.1.1 confirms only four battery drawing/selection files and two infrastructure version labels changed in production Java. BatteryModule, placement/row helpers, other components and signing key are unchanged.
- Local environment uses Java 17, Gradle 8.11.1, SDK 36, build tools 35.0.0; temporary proxy/preview configuration is external and excluded from the ZIP. GH Actions runs the regular release checks.

Physical-device status bar appearance and Iconify placement with these new drawings have not been tested here. The existing hook and placement code is preserved. No change to optional Settings shortcut compatibility in this release.
