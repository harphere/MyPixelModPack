# My Pixel Mod Pack 2.0.2 — provider access fix

This is a focused update to the installed 2.0.1 pack. It retains the original UI, all nine feature hooks, signing key, application ID and saved settings. It does not include the 2.1.0 UI redesign or Android Settings shortcut.

## Build and install

Upload all ZIP contents to your GitHub repository root, including `.github/workflows/build.yml`. Run **Build Refreshed My Pixel Mod Pack**. Download **MyPixelModPack-provider-fix-v2.0.2**, install its APK over 2.0.1, and reboot. No uninstall is needed.

Then:

1. Open the pack. It refreshes app access automatically; wait for **App access refreshed**. Use **Refresh app access** to repeat if needed.
2. Keep the desired apps scoped in Vector/LSPosed and unchecked in the Nav Bar Match blacklist.
3. Force stop eero, AquaMail and Costco, then reopen them.
4. Inspect startup diagnostics for their new `/ startup` and `/ match` reports. Diagnostics now include the Nav Bar Match internal enable flag, blacklist and access-refresh result.

After newly installing or reinstalling an app, reopen the pack or press Refresh app access, then restart that app. Granted hosts retain the read-only visibility permission when the pack loads successfully, so Android can retain it across restarts. Grants apply only to installed apps in the pack's Android user/profile.

## What changed

Eero's supplied log shows the legacy module was injected, but the settings provider returned `Unknown authority dev.chet.mypixelmodpack.settings.v2` at both Application.attach and Application.onCreate. The dispatcher correctly declined to install hooks without saved configuration. Reporting failed for the same reason.

Android documents that URI permission grants establish visibility into the provider's app. The pack now gives installed third-party and launchable system apps a read-only, persistable grant on a dedicated non-data URI (`...settings.v2/visibility`). The host retains that grant before reading settings. The exported provider's existing snapshot allowlist and diagnostic caller-UID checks are unchanged. No private files, settings-write API, or write permission is added. LSPosed scope still determines injection.

This targets the confirmed provider-access failure. Eero's underlying visibility restriction and AquaMail/Costco's failure mechanism have not been independently reproduced on a physical device. If access still fails, send the access-refresh result and the affected app's MyPixelModPack Vector startup log.

The Nav Bar Match settings checkbox now displays its saved state rather than always looking enabled. Existing feature settings are preserved; no feature switch is forced on or off.

## Scopes

- System UI: battery, mobile/service icons, network indicator, DT2W.
- Gboard AND installed Launcher3: cursor arrows. Force stop Gboard after an update if arrows are missing.
- Launcher3: navigation icons and OPA. Nova does not host the Quickstep navigation row on this setup.
- Each target app: Nav Bar Status Match.
- System Framework is unnecessary. Standalone modules for enabled pack features remain disabled.

Java 17, Gradle 8.11.1, AGP 8.9.2, SDK 36; legacy API 82. Target SDK remains 28 to preserve proven broadcasts. See VALIDATION.md for completed checks and limitations.
