# Replace files when updating 3.0.1 → 3.1.0

Copy each file from this ZIP to exactly the same repository path. Commit the changes together. The complete ZIP can also replace the repository source in one upload.

## Replace existing build files

- .github/workflows/build.yml
- app/build.gradle
- scripts/check_pack.py
- scripts/check_apk.py
- release-source-sha256.json

## Replace existing Java and tests

- app/src/main/java/dev/chet/batterygradient/GradientBatteryDrawable.java
- app/src/main/java/dev/chet/batterygradient/SettingsActivity.java
- app/src/main/java/dev/chet/batterygradient/SettingsProvider.java
- app/src/main/java/dev/chet/chromepiestatusmatch/view/PieMenu.java
- app/src/main/java/dev/chet/mypixelmodpack/HomeActivity.java
- app/src/test/java/dev/chet/batterygradient/BatteryRenderingTest.java
- app/src/test/java/dev/chet/batterygradient/ChargingAnimationTest.java

## Add new files

- app/src/main/java/dev/chet/batterygradient/BatteryJPaths.java
- app/src/test/java/dev/chet/chromepiestatusmatch/view/PieContrastTest.java
- LICENSE
- THIRD_PARTY_NOTICES.md
- REPLACE_FILES.md

Also copy README.md, SOURCE_AUDIT.md, VALIDATION.md, docs/battery-preview.png and docs/charging-preview.gif for updated documentation/previews. Keep your existing included signing key. No manifest or scope change is required for this update.

Run **Build MyPixelModPack 3.1.0**, download its artifact, install **MyPixelModPack-3.1.0.apk** as an update and reboot. The old portrait selection becomes **Landscape (Battery J)** automatically. Circle styles and saved appearance options are retained.
