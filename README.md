# My Pixel Mod Pack 3.1.0

Complete source for one legacy LSPosed/Vector APK, updated from delivered 3.0.1. Adds a slim Landscape Battery J and extends ChromePie pale-grey contrast handling. Version 3.1.0 uses versionCode 38. Package ID, signing key and settings stores are preserved. See REPLACE_FILES.md for an exact incremental replacement list.

Version 3.1.0 strengthens charging ticker recovery, System UI battery fallback attachment, Nav Bar Match lifecycle handling and bounded settings-startup retries. The existing Gboard reconnect implementation is preserved; recovery after every physical reboot remains unverified.

## Battery J landscape and Wikipedia pie correction

The portrait selection now displays **Landscape (Battery J)**: Iconify's slim horizontal perimeter and four fill segments, with the Pack's red → amber → yellow → green gradient from left to right. Fill drains continuously from right to left. The neutral segment track and outline remain visible. The percentage is still optional; its smaller label fits the horizontal frame, and the right-side charging bolt is scaled to suit the slimmer body. Charging animation sweeps left to right through the remaining fill and retains all visibility/full/unplugged stop conditions.

The saved style key and existing Tasker argument `portrait` are preserved for compatibility and now choose this landscape style. Existing users selecting portrait switch automatically; the other three battery styles retain their implementation.

Iconify references: [LandscapeBatteryJ.kt](https://github.com/Mahmud0808/Iconify/blob/5515543241d906af78ad44751ea96933a7ad7cc7/app/src/main/java/com/drdisagree/iconify/xposed/modules/statusbar/batterystyles/LandscapeBatteryJ.kt) and [battery_configs.xml](https://github.com/Mahmud0808/Iconify/blob/5515543241d906af78ad44751ea96933a7ad7cc7/app/src/main/res/values/battery_configs.xml). See THIRD_PARTY_NOTICES.md and LICENSE.

ChromePie now recognises very light neutral colours such as Wikipedia's observed #EAECF0. These use an opaque #E8E8E8 pie with black icons, even if the icon preference is White. Selected/submenu shades remain distinct and opaque. Coloured/dark menus and their icon preferences retain their existing behaviour. Status-colour sampling and its existing bounded retries are unchanged; this fixes the pale-grey classification shown in the supplied screenshot.

## Near-white navigation icon correction

Titanium's supplied screenshot has a pale navigation background around #F9F9FC. The earlier rule required every RGB channel to be at least 250, so it requested white glyphs on this shade. This update requests black navigation icons on very light neutral backgrounds (minimum channel 232; channel spread at most 16). Dark and strongly coloured backgrounds retain white icons. This changes the app's navigation appearance signal consumed by Launcher3 custom icons; ChromePie menu colouring is unchanged.

The source check now tolerates .gitkeep placeholders, while still rejecting mismatched source hashes and extra real app files.

## Build and update

Replace the complete repository source with this ZIP's contents, including `.github/workflows/build.yml`, root Gradle files, `app/build.gradle`, manifest, scripts and release-source-sha256.json. Do not upload only the Java files or reuse an old workflow/Gradle file. Run Actions → **Build MyPixelModPack 3.1.0**. Source fingerprint checks reject incomplete/mixed uploads; final APK checks require package dev.chet.mypixelmodpack, versionName 3.1.0 and versionCode 38. Download the **MyPixelModPack-3.1.0** artifact and install **MyPixelModPack-3.1.0.apk**. Confirm **Version 3.1.0** on the Pack home page before testing. Download and extract the APK artifact, then install the APK as an update. Package ID and included signing key are unchanged. Keep your existing signing configuration if you have replaced the development key in your repository. Do not uninstall the Pack: an update preserves saved Pack switches and appearance settings.

Both new features start OFF. In the Pack, open **Calls & browser**, configure the feature, then enable its switch. Add the relevant apps to the Pack scope in LSPosed/Vector. Disable the matching standalone modules and reboot once to load the updated hooks. You can keep the standalone APKs installed; their settings remain separate. ChromePie settings are not automatically imported into the Pack: configure its menu, trigger edges and icon colour once under **Customize**.

## Scope

| Feature | Pack scope |
| --- | --- |
| Battery, mobile/service icons, network activity, DT2W | System UI |
| Navigation icons, OPA, Gboard navigation-row arrows | Launcher3 |
| Gboard input/cursor commands | Gboard |
| Nav Bar Status Match | Each target app |
| SpeakerControl | Default Phone app; InCall UI if your ROM separates it |
| ChromePie Status Match | Titanium / supported Chromium browser |
| Optional Settings shortcut | Android Settings |

System Framework is not required. Keep corresponding standalone modules disabled when using a Pack feature. Changing feature switches requires the host process to restart or a reboot. Appearance changes can apply live where stated below.

## Charging battery animation

Battery **Customize** now has a live **Charging animation** switch, enabled by default. The percentage switch, outside-in filled-circle discharge, landscape gradient and right-hand lightning bolt remain.

- Filled circle: a highlight expands from the centre to the current fill edge.
- Landscape Battery J: a highlight travels horizontally through the remaining fill.
- Dashed circle: segments brighten clockwise along the charged portion.
- Ring: a short highlight sweeps clockwise along the charged portion.

These highlights preserve the underlying colour scheme and measured level. Frames are scheduled at roughly 30 fps over a 1.8-second cycle, only while actively charging with a nonempty, nonfull battery and a visible icon on an interactive display. Frames stop on detach, hiding, screen off, animation OFF, unplugging, or full charge. No permanent background service or polling loop is added. The charging bolt can remain at full charge while plugged in. **Preview charging** lets you view the effect without connecting a charger; adjust the preview level below 100%.

`docs/charging-preview.gif` shows the 3.1.0 native Android drawable renders, not a phone screenshot.

## SpeakerControl

Ordinary active phone calls only. WhatsApp calls, microphone control and media routing are not included. Uses the same tested InCallService route commands and result verification as the standalone 1.0.0 source. The receiver registers after Pack context is available, rather than waiting for an Application.attach event that has already happened.

Open **Customize** to see the default dialer package and test STATUS, ENABLE, DISABLE or TOGGLE during a connected phone call. ENABLE requests speakerphone; DISABLE requests the earpiece. No commands are queued for a later call. Supported audio routes are checked and results verified.

Existing Tasker configuration remains valid:

- Action: `dev.chet.speakercontrol.ENABLE`, `.DISABLE`, `.TOGGLE` or `.STATUS`
- Package: your default Phone app package, shown in Customize
- Target: **Broadcast Receiver**
- Extra: `TOKEN:<same value shown in Customize>`
- Leave Class, Data and MIME blank.

For Tasker replies, add `reply_package:net.dinglisch.android.taskerm` and optionally `request_id:<your ID>`. Receive `dev.chet.speakercontrol.RESULT`. Existing extras and TOKEN are unchanged. Replies to the built-in control screen now target the Pack package. Wait for a reply before sending another route command.

## ChromePie Status Match

Includes the full standalone ChromePie menu and its latest test7 matching behaviour; it does not depend on the standalone APK. Keep that standalone module disabled to prevent two menus. Uses the original supported Chromium activity list and menu/actions. Browser revisions can still affect the original Chromium internals.

Retains icon mode Auto / White / Black, automatic black icons for white/near-white status colours, an opaque light-grey `#E8E8E8` pie for those colours, and the bounded first-opening colour retries. Configure under **Customize**, then force-stop/reopen the browser after changing its settings. Reboot once after enabling the Pack feature.

ChromePie has a private `chromepie_v1` settings store, read through the same Pack snapshot provider as the existing components. No world-readable preferences or zygote initialization are used. Pending menu attachment is cancelled when an activity is destroyed and each activity gets at most one Pack menu. Standalone launcher hiding is omitted because the Pack has one shared launcher entry.

## Existing features and diagnostics

Battery gradient, Gboard arrows, mobile type styles, VoLTE/VoWiFi, OPA, navigation icons, Nav Bar Match, network activity and Double Tap to Wake remain independently gated. The 2.0.2 app visibility grants and provider retry path, optional Settings entry, feature settings stores, one legacy entry point and APK signing key remain in place.

Use **App & diagnostics → Startup diagnostics** for versionCode/build ID, battery options and last process reports. New host reports carry their hook version. The battery animation report includes charging status, visibility, running state and executed frame count; Nav Match reports include hook installation and PixelCopy sample results. Older host reports remain historical until those processes restart. These are historical reports, not proof that a UI hook is currently active. Use Vector logs tagged `SpeakerControl`, `ChromePieMatch` / `ChromePie` or `GboardCursorKeys` for actual hook/command details. **Refresh app access** remains available for scoped apps that cannot see settings.

## Validation and limits

See `VALIDATION.md` for this release; `docs/validation-v2.2.0.md` is historical. Local Android builds and automated rendering/lifecycle/settings tests do not substitute for Vector injection and phone testing. The combined SpeakerControl/ChromePie integration needs confirmation on the device; keep the working standalone APKs available while testing.

ChromePie/AOSP source copyright and Apache notices are retained in the source files. This is a private source build combining the supplied working components.

## Preserved version 3.0 timing changes

- Settings failures at startup receive at most four delayed attempts (250 ms, then 1 s, 3 s, 10 s apart), plus the existing unlock retry. Successful or potentially partial installations retain the deduplication claim. No all-features-on fallback is used.
- Battery animation restarts when its visible ImageView callback is restored or the view reattaches. It still stops while hidden, detached, screen off, unplugged, full or disabled. BatteryMeterView attachment can install the status-row icon even if the controller path did not fire.
- Nav Match uses Instrumentation Activity resume/pause/destroy boundaries, posts attachment until the decor is ready, coalesces layout samples, preserves its three delayed rechecks, cancels pending work on pause/destroy and ignores late PixelCopy results from inactive Activities. Existing colour selection, blacklist, contrast and bottom overlay rules remain.

After installing, open the Pack once to refresh app access, check **3.1.0**, verify existing scopes and reboot. Keep matching standalone modules disabled for enabled Pack features. Test animation while actually charging below 100%; preview charging is available without a charger. Test Nav Match by reopening a scoped, non-blacklisted app. If a problem remains, copy the new diagnostics before toggling switches.
