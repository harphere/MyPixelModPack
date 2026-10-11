# Source audit — 3.1.0

Baseline: delivered MyPixelModPack-source-v3.0.1.zip. Version 3.1.0 / code 38. Existing package, signing key, saved switches, blacklist and providers are preserved.

- Battery: legacy portrait style key retained, display/geometry replaced with slim horizontal Battery J perimeter and four segments adapted from Iconify commit 5515543241d906af78ad44751ea96933a7ad7cc7. Pack gradient, continuous level clipping, optional percentage, right bolt and charging lifecycle retained. Landscape pulse moves left to right.
- ChromePie: near-white test now requires minimum RGB channel 232 and spread at most 16. Wikipedia #EAECF0 consequently uses the existing opaque grey/black-icon treatment. Sampling, retries, attachment hooks and non-light icon preferences are unchanged.
- Version/CI diagnostics and source fingerprints updated. Exact signing key unchanged. .gitkeep tolerance retained.
- Gboard, OPA, navigation icons, Nav Match, network activity, mobile/service icons, DT2W and SpeakerControl sources match 3.0.1; no new system hooks or services.

Upstream geometry sources and licensing: THIRD_PARTY_NOTICES.md and LICENSE. Local checks do not replace physical-device verification.

The Xposed API is added to testRuntimeOnly so the new real PieMenu colour tests can load its utility exception types; it remains compileOnly for the released APK.
