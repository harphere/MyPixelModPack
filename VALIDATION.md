# Validation completed for refreshed 2.0.0

Validation date: September 29, 2026.

- All Java sources compile against Android SDK 36 and legacy Xposed API 82.
- Full Gradle 8.11.1 / Java 17 release APK build passed.
- Android release lint passed with zero Error/Fatal issues. Warnings remain in inherited module code (private API usage, UI strings/accessibility, drawable allocation) and in the intentionally exported settings providers.
- Seven automated tests passed: five Android/Robolectric settings tests and two component installation isolation tests. No failures or errors.
- Tested old switches being ignored, independent navigation stores, string-set transfer, a stable startup snapshot, missing-provider errors, preferences allowlisting, disabled-feature non-invocation, and continuing after an initialization error.
- Source/manifest checks passed: unique provider authorities, all nine component sources present, matching settings activities, separate navigation stores, and no XSharedPreferences/FeatureGate/modern API/original-method interceptor.
- Built APK verified to contain exactly one legacy dispatcher entry and no modern Xposed entry metadata.
- APK signature verification passed using APK Signature Scheme v2 with the included stable development key.

These checks establish build correctness and settings/installation behavior under tests. They do not validate Vector's injection timing, resource forwarding, ROM-specific hook availability, Iconify placement or the nine features on the physical Pixel. Device verification remains required. The local test runner used an environment proxy for dependencies; that proxy is not included in the project or GitHub workflow.
