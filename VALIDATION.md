# Validation — MyPixelModPack 3.1.0

October 11, 2026. Baseline: delivered 3.0.1. Version 3.1.0 / code 38.

- All 53 release tests passed with zero failures/errors/skips.
- Native Android renders verify the slim horizontal J geometry, four fill segments/gaps, continuous right-to-left depletion, retained warm left/green right gradient, optional percentage, charging bolt lane and charging pulse clipping. Existing circle and animation lifecycle tests remain passing.
- New real PieMenu tests cover Wikipedia's #EAECF0, opaque grey normal/selected/submenu paints with black icons despite the White preference, distinct selected fill, and retained orange/black white-icon preference.
- Release assembled and lint completed: zero Error/Fatal findings; 122 existing warnings remain.
- APK verification confirms dev.chet.mypixelmodpack, versionName 3.1.0, versionCode 38, one v2 signer. Signing key byte-matches 3.0.1.
- Complete source/APK checks pass: one legacy dispatcher, eleven components, optional Settings entry and preserved provider access/configuration stores. .gitkeep tolerance remains.
- Rendered previews are included at docs/battery-preview.png and docs/charging-preview.gif. They are generated Android drawable renders, not device screenshots.

Vector injection, actual charging and real Titanium compositor behaviour require phone testing. This release fixes the light-neutral classification visible in the supplied Wikipedia screenshot; browser status-colour sampling and its retries are preserved.

Build with the supplied workflow, install as an update and reboot. Existing portrait selection automatically becomes Landscape (Battery J); saved percentage/animation settings are retained. See REPLACE_FILES.md for the exact changes and THIRD_PARTY_NOTICES.md for source attribution.
