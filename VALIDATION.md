# Validation — 3.6.0

Baseline: complete 3.5.0; versionCode 45. Adds Auto SpeakerControl routing and refreshes the home UI with a reference-inspired dark palette, pastel vector category icons, rounded rows, functional feature search, refresh/overflow controls, and Home/Diagnostics/Settings navigation. Light theme remains available through the system setting. Existing feature pages, switches and appearance settings are retained.

## Local checks
All SpeakerControl Java classes, PackUi, CategoryIconView, HomeActivity and battery SettingsProvider compiled against Android/Xposed API jars, using generated BuildConfig/provider API stubs as needed. Complete-source, component and XML checks pass. Added Android tests for one-target dispatch, duplicate/stale status reports, ambiguity, missing responses and invalid-token/unknown-package reports. These tests and the full Android suite, release compilation, lint and native UI preview renders were NOT run locally; the included Actions workflow runs the Android checks. This is not an on-device compatibility claim.

## Limits
WhatsApp detection/routing remains device-test dependent. Auto chooses one positive active backend from replies received during a one-second window, not an OS-wide authoritative inventory of calls. Full dynamic receiver/manifest delivery, Tasker operation, UI appearance and call routing require phone testing. No fabricated Update or download feature was added to the new navigation bar.
