# Validation — 2.0.1

Completed September 30, 2026:

- Twelve tests passed, zero failures/errors: five settings-provider tests, two component failure-isolation tests, three process initialization tests, and two user-unlock retry tests.
- Full release APK build and Android release lint passed (zero Error/Fatal lint issues; inherited warnings remain).
- Built APK entry checks passed: one legacy dispatcher, no modern metadata, nine component sources, independent settings providers/stores.
- APK signature verification passed. The included signing key is byte-for-byte identical to 2.0.0.
- Battery Gradient, Gboard arrows, OPA and NavDotStyle component source files are unchanged from 2.0.0. The changes are to dispatcher startup boundaries, a retry on user unlock, the process initialization guard, diagnostics display, and regression tests.

The tests exercise startup deduplication, retries after a no-hooks-installed settings failure, preventing duplicate hooks after an unexpected partial failure, and the unlock broadcast retry. They do not simulate Vector injection or confirm the device's Launcher3 hooks. The missed-attach/locked-user scenarios are plausible explanations for the absent component logs; device behavior remains unverified.
