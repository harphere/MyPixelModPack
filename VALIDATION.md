# Validation — MyPixelModPack 3.2.0

October 12, 2026. Baseline: delivered 3.1.0; rectangular drawable restored from exact delivered 3.0.1. Version 3.2.0 / code 39.

- All 58 release tests passed, zero failures/errors/skips.
- Native rendering checks cover half-size scaling for all styles, preserved portrait geometry/gradient, landscape rotation/depletion, optional percentage, bolt lane and charging animation clipping/lifecycle.
- Provider tests cover independent saved sizes, 50% rectangle default, active style override, clamped ranges and rejection of invalid style/orientation values. UI test verifies orientation controls apply only to rectangle and persist the chosen value.
- Previous ChromePie/Wikipedia and navigation contrast tests remain passing.
- Release assembled; lint completed with zero Error/Fatal findings. Existing warnings remain.
- APK verified as dev.chet.mypixelmodpack 3.2.0 / 39, one v2 signer. Signing key matches 3.1.0 byte-for-byte. Complete-source and single-dispatcher APK checks pass.
- Inspected the generated native sizing/orientation sheet and SVG-rendered Tower concepts. Source previews are included in docs. Concept vectors are not compiled/activated by the APK.

Tests do not inject Vector into a physical System UI. Confirm actual layout and charging behaviour on the phone. Portrait/landscape and size use the existing live observer; initial updated code requires a reboot. Tower theme linking and ROM status-slot/state integration remain separate work.
