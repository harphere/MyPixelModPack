# Validation — 2.0.2

Completed October 1, 2026:

- Sixteen tests passed, zero failures/errors: five provider snapshot tests, two failure-isolation tests, three startup guard tests, two unlock retry tests, three visibility-grant tests and one Nav Bar Match settings UI test.
- Full release build and release lint passed. Zero Error/Fatal lint issues; inherited warnings remain.
- Grant tests cover read-only plus persistable mode on the dedicated non-data URI, unchanged feature switches, continued grants after one rejection, self exclusion and deduplication.
- Settings UI test confirms saved OFF is displayed as OFF, toggling preserves the blacklist, and returning to the page reads the current saved state.
- All original Java files compared against 2.0.1. Changes are limited to PackRuntime, dispatcher version labels, HomeActivity access/diagnostics UI and the Nav Bar Match settings checkbox. New helper/tests added separately. All nine feature entry classes and signing key are identical to baseline.
- APK signature and single legacy entry checks passed. Packaging excludes build output, local SDK paths and temporary dependency setup.

Limitations: Robolectric tests record URI-grant calls; they do not exercise Android's real package visibility service, persistent permission lifecycle across device reboot, or Vector injection. The supplied eero log confirms provider lookup failure before hooks install. The workaround and AquaMail/Costco behavior require testing on the user's device. No guaranteed colour-matching result is inferred from compilation or unit tests.
