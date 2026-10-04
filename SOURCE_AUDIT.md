# Source audit — 2.1.3

Baseline: MyPixelModPack-source-v2.1.2-battery.zip.

- GradientBatteryDrawable adds an optional percentage, default OFF, and replaces the charging dot with a right-side vector lightning bolt. The canvas expands to 32 × 24 logical units; the battery remains in its original 24 × 24 area. All four style geometries and colour gradients are retained.
- Battery SettingsProvider returns and persists show_percentage through a same-UID, validated set_percentage operation; changes notify the existing observer. Style values and override behaviour are retained.
- SettingsActivity adds a live percentage switch, reads saved state, and widens its preview with matching charging state.
- BatteryModule reads style and percentage from one provider snapshot and uses a 32 × 24 dp slot at both attachment paths. Existing row placement logic and native icon suppression are retained. FULL status shows the bolt only when plugged in; accessibility descriptions retain level and charging state.
- All production Java outside Battery Gradient matches the baseline except two version log labels. Signing key, package ID, all nine feature switches, provider visibility grants and startup isolation are retained.
- Version 2.1.3, versionCode 33; GH Actions artifact version updated.
