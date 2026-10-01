# Source audit — 2.0.2

Baseline: delivered MyPixelModPack-source-refreshed-v2.0.1.zip.

All nine feature entry classes, their helper classes, settings providers, package ID, signing key and appearance stores are unchanged. Dispatcher logic is unchanged; only its version log labels change.

Changes:
- ProviderVisibility runs in the pack app to grant a dedicated provider-visibility URI to installed third-party and launchable system packages in the same Android user/profile. It deduplicates packages, excludes the pack itself, isolates rejected grants and returns a report.
- PackRuntime attempts to retain a granted read-only URI permission before querying saved preferences. A missing grant is tolerated for already-visible hosts; an unavailable settings snapshot still aborts initialization rather than enabling defaults.
- The pack settings provider declares grantUriPermissions. Its existing snapshot store allowlist, read-only configuration API and diagnostic caller validation are retained.
- HomeActivity automatically refreshes app access in a worker, exposes a manual refresh button and adds the result/internal Nav Bar Match settings to diagnostics.
- Nav Bar Match settings UI now reflects its saved enable flag onStart and preserves the blacklist.
- New regression tests cover URI/mode selection, unchanged feature settings, partial grant failures, package deduplication/self exclusion and persisted checkbox state.

The URI-grant mechanism follows Android's documented package visibility rules. The supplied eero log establishes provider lookup failure, but the device-specific source of that restriction has not been reproduced locally. The test build remains subject to real-device confirmation; AquaMail and Costco have no corresponding startup logs yet.
