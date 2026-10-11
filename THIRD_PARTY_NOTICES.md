# Third-party notices

## Iconify Battery J geometry

BatteryJPaths.java adapts the perimeter and four-segment fill coordinates from Mahmud0808/Iconify's `app/src/main/res/values/battery_configs.xml`, commit `5515543241d906af78ad44751ea96933a7ad7cc7` (reviewed October 11, 2026). Upstream repository: https://github.com/Mahmud0808/Iconify . Upstream references: `LandscapeBatteryJ.kt` and the resource paths named `config_landscapeBatteryPerimeterPathJ` / `config_landscapeBatteryFillMaskJ`.

The resource coordinates are from the GPL-3.0 Iconify repository; LICENSE contains its GPL v3 license. The combined source incorporating this adaptation is provided under GPL-3.0, with prior component copyright and license notices preserved. Modified for the Pack: Java Path commands, centring, fixed level gradient, measured horizontal fill clipping, optional percentage and a separate charging bolt/animation. No Iconify hooks or services are imported.

Existing AOSP/ChromePie Apache source notices remain in their files. Apache Commons Lang is used under Apache-2.0; its dependency is declared in app/build.gradle.
