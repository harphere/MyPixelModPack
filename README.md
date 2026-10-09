# My Pixel Mod Pack 2.2.0

Full source for one legacy LSPosed/Vector APK. This update builds on 2.1.4 and adds charging animations, SpeakerControl and ChromePie Status Match. The Gboard reboot issue is still pending; this release does not change the Gboard code.

## Build and update

Upload the ZIP contents to your repository root, including `.github`. Run Actions → Build Refreshed My Pixel Mod Pack. Download and extract the APK artifact, then install the APK as an update. Package ID and included signing key are unchanged. Keep your existing signing configuration if you have replaced the development key in your repository. Do not uninstall the Pack: an update preserves saved Pack switches and appearance settings.

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

Battery **Customize** now has a live **Charging animation** switch, enabled by default. The percentage switch, outside-in filled-circle discharge, portrait gradient and right-hand lightning bolt remain.

- Filled circle: a highlight expands from the centre to the current fill edge.
- Portrait: a highlight travels upward through the remaining fill.
- Dashed circle: segments brighten clockwise along the charged portion.
- Ring: a short highlight sweeps clockwise along the charged portion.

These highlights preserve the underlying colour scheme and measured level. Frames are scheduled at roughly 30 fps over a 1.8-second cycle, only while actively charging with a nonempty, nonfull battery and a visible icon on an interactive display. Frames stop on detach, hiding, screen off, animation OFF, unplugging, or full charge. No permanent background service or polling loop is added. The charging bolt can remain at full charge while plugged in. **Preview charging** lets you view the effect without connecting a charger; adjust the preview level below 100%.

`docs/charging-preview.gif` contains native Android drawable renders, not a phone screenshot.

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

Use **App & diagnostics → Startup diagnostics** for saved switches and last process reports. These are historical reports, not proof that a UI hook is currently active. Use Vector logs tagged `SpeakerControl`, `ChromePieMatch` / `ChromePie` or `GboardCursorKeys` for actual hook/command details. **Refresh app access** remains available for scoped apps that cannot see settings.

## Validation and limits

See `docs/validation-v2.2.0.md` for completed checks. Local Android builds and automated rendering/lifecycle/settings tests do not substitute for Vector injection and phone testing. The combined SpeakerControl/ChromePie integration needs confirmation on the device; keep the working standalone APKs available while testing.

ChromePie/AOSP source copyright and Apache notices are retained in the source files. This is a private source build combining the supplied working components.
