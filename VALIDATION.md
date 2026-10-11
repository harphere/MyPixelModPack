# Validation — My Pixel Mod Pack 3.0

October 11, 2026. Exact audited baseline: the previously delivered 2.2.0 source ZIP.

- 46 release tests passed; zero failures, errors or skips. Includes battery rendering and screen/detach lifecycle, lost-animation-callback restart, bounded provider retry success/exhaustion, feature isolation, saved settings, provider visibility, UI controls and existing Gboard navigation-host recovery tests.
- Release APK assembled successfully with Java 17, Gradle 8.11.1, AGP 8.9.2, SDK 36 and build tools 35.0.0.
- Release lint: zero Error/Fatal findings. 122 warnings remain, including existing compatibility/deprecation warnings. Lint is still enabled; this is not a warning-free build.
- APK verified using v2 signing with one signer. aapt confirms package dev.chet.mypixelmodpack, versionName 3.0, versionCode 36. The signing key matches both delivered 2.2.0 and 2.1.4 sources byte-for-byte.
- Source/APK structure checks pass: one legacy dispatcher, all eleven components, Settings shortcut, four unique providers, independent stores, ChromePie resources/dependency, required permissions and battery animation controls.
- Source fingerprint guard checked against this release. The guard rejects a modified/older app/build.gradle in an isolated extraction before building.
- Native Android render tests produced all four charging preview styles; the resulting preview GIF is included.

The hook startup and Activity lifecycle changes were reviewed in source and compiled; tests do not inject Vector into real System UI, Gboard, Launcher3, Phone or browser processes. PixelCopy colour sampling and ROM-specific windows require physical-device testing. Gboard's existing recovery implementation is preserved; these checks do not claim its reboot issue is resolved.

The exact 2.2.0 ZIP correctly declares 2.2.0 / code 35 and includes the charging, SpeakerControl and ChromePie features. Its UI version comes from BuildConfig. The supplied evidence does not identify why the installed app was still 2.1.4; this release adds full-source and final-APK checks to detect that mismatch.

Install as an update, open the Pack to refresh access, confirm Version 3.0, then reboot with existing scopes and enabled-feature choices. System Framework scope is not needed. Use startup diagnostics to inspect fresh version-labelled reports, animation frame counts and Nav Match sample results.
