# My Pixel Mod Pack 3.7.0

Adds an optional status-bar battery tap shortcut to Battery settings. Standard mobile badges and Waves now share the H/K vector lettering and sizing; Waves scales with the glyphs. Includes all 3.6.1 features and DT2W changes. VersionCode 47. See docs/BATTERY_SHORTCUT_AND_MOBILE_3.7.0.md.

## Build and update

See REPLACE_FILES.md for the exact changed files. Upload the complete ZIP contents to the repository root, including the supplied workflow, Gradle files and release-source-sha256.json. Run **Build MyPixelModPack 3.7.0**, download its artifact, install **MyPixelModPack-3.7.0.apk** as an update and reboot. Confirm Version 3.7.0 on the home page. Settings and existing scopes are preserved. No System Framework scope is required.

## Battery controls

Open **Status bar → Battery gradient → Customize**.

- Filled circle: retains outside-in discharge and the fixed green/yellow/amber/red gradient.
- Dashed circle and ring: retain the existing renderers.
- Rectangular: the previous portrait shape, continuous fill and top-to-bottom colour scheme are restored. Choose Portrait or Landscape; landscape rotates the body/fill/pulse clockwise, giving green at the right and red at the left, draining right to left. Percentage stays upright; the lightning bolt stays to the right.
- **Size for this style:** 25–100% of the previous dimensions, saved separately for each style. Rectangle defaults to 50%; circle styles default to 100%. Switching styles restores that style's size. Both width and height scale proportionally; 50% means half each dimension.
- Existing optional percentage and charging animation controls remain. Animation is visible only while charging below 100%, and stops while hidden, detached, screen off, full, disabled or unplugged. Preview charging and its level slider remain available.

Style, size and orientation apply through the existing live observer. The System UI icon's actual layout space is resized with it. The saved `portrait` style key and existing Tasker style argument are preserved. Battery J is removed; the workflow removes its obsolete source automatically. The setting defaults back to Portrait at half size on upgrade.

## Tower status-symbol concepts

`docs/tower-style-concepts.png` / `.svg` compare the supplied Tower/Router Wi-Fi and lattice-tower data artwork with new vibration, alarm-clock, speakerphone and mute concepts. Editable Android vector candidates are in `docs/tower-vectors/`.

These are design previews, not active hooks. Shared theme selection can make matching status symbols follow a Wi-Fi/data style, but that requires a theme selector or supported access to the selected overlay and verified ROM slot/state handling. The current source preserves existing hooks while the designs are reviewed. Clock means the alarm status symbol, rather than the time text. Speakerphone/mute state integration must be verified against the ROM's available slots.

## Scope

| Feature | Pack scope |
| --- | --- |
| Battery, mobile/service icons, network activity, DT2W | System UI |
| Navigation icons, OPA and Gboard navigation arrows | Launcher3 |
| Gboard input/cursor commands | Gboard |
| Nav Bar Status Match | Each target app |
| SpeakerControl | Default Phone app / separate InCall UI |
| ChromePie Status Match | Titanium / supported Chromium browser |
| Optional Settings shortcut | Android Settings |

Keep matching standalone modules disabled for enabled Pack features. Hook feature switches require restart/reboot; appearance settings apply live where supported.

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


## Diagnostics and validation

Startup diagnostics now include active battery size and orientation, plus existing saved switches, provider-access information and version-labelled host reports. Reports are historical, not a live health check.

See VALIDATION.md for checks and device-test limits. Native previews are in docs/battery-preview.png, docs/battery-sizing-preview.png and docs/charging-preview.gif. Tower concepts are preview-only assets and do not alter the active icon theme.
