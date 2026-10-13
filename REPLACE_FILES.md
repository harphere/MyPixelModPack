# Updating 3.1.0 → 3.2.0

Copy files from this ZIP to the same repository paths and commit them together.

## Replace build files

- .github/workflows/build.yml
- app/build.gradle
- scripts/check_pack.py
- scripts/check_apk.py
- release-source-sha256.json

## Replace existing Java/tests

- app/src/main/java/dev/chet/batterygradient/GradientBatteryDrawable.java
- app/src/main/java/dev/chet/batterygradient/BatteryModule.java
- app/src/main/java/dev/chet/batterygradient/SettingsProvider.java
- app/src/main/java/dev/chet/batterygradient/SettingsActivity.java
- app/src/main/java/dev/chet/mypixelmodpack/HomeActivity.java
- app/src/test/java/dev/chet/batterygradient/BatteryRenderingTest.java

## Add

- app/src/test/java/dev/chet/batterygradient/BatterySizingTest.java
- app/src/test/java/dev/chet/batterygradient/BatterySizingUiTest.java

## Remove obsolete file

- app/src/main/java/dev/chet/batterygradient/BatteryJPaths.java

The supplied workflow also removes this obsolete file before checking/building, so a stale copy will not block the Actions build.

Copy the updated README.md, SOURCE_AUDIT.md, VALIDATION.md, THIRD_PARTY_NOTICES.md, REPLACE_FILES.md and docs folder for instructions and previews. Keep the existing signing key and manifest.

Run **Build MyPixelModPack 3.2.0**, install **MyPixelModPack-3.2.0.apk** as an update and reboot. In Battery Customize select Rectangular, choose Portrait or Landscape, and adjust Size for this style. Defaults are 50% for rectangle and 100% for circle styles. All style sizes are independent.

Tower XML/SVG/PNG files under docs are design previews. They do not enable a new status-icon theme or new hooks.
