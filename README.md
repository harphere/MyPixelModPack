# My Pixel Mod Pack 2.0.1 — launcher startup fix

This update adds startup paths for Vector processes where Application.attach has already happened when the module loads. It checks an already attached application, listens for attach, and also starts before Instrumentation calls Application.onCreate. Initialization occurs at most once per package/process. A settings-provider failure that installed no components can retry at the next boundary. If the host starts before unlock, it also retries when Android reports the user unlocked.

Upgrade directly from 2.0.0; the signing key, application ID, switches and appearance settings are unchanged. No uninstall is needed. Upload all ZIP contents including `.github/workflows/build.yml`; run **Build Refreshed My Pixel Mod Pack** and download `MyPixelModPack-refreshed-v2.0.1`.

Keep scope on your installed Launcher3 and Gboard. Pixel Launcher is only a scope option when that app actually exists on the phone. Nova is not the Quickstep taskbar host used by these component hooks. After installing, confirm cursor, OPA and navigation-icon switches are ON, their matching standalone modules are disabled, and reboot the phone.

The diagnostics button now displays the complete report in a selectable on-screen dialog with **Copy all**. This avoids relying only on pasted clipboard text. Reports distinguish dispatcher/settings problems from component hook errors; `ON; entry point returned` does not mean every hook was available.

Twelve automated tests, release build, lint and APK entry/signature checks are documented in VALIDATION.md. Vector injection timing and the phone's launcher hooks still need device verification. The absence of component logs pointed to startup routing, but without a dispatcher log we cannot prove the missed-attach hypothesis was the only cause.

The original 2.0.0 integration notes follow (first-install instructions apply only when coming from a v1.x pack).

One installable legacy LSPosed APK containing Battery Gradient, Gboard cursor arrows, mobile type icons, VoLTE/VoWiFi icons, OPA Home, navigation icons, Nav Bar Status Match, the network up/down indicator, and Double Tap 2 Wake. All nine switches start OFF. This source replaces the earlier pack's startup and settings integration; Android device behavior still requires testing.

## Build in GitHub Actions

For the cleanest rebuild, use a new empty repository and upload this ZIP's contents to its root. Include `.github/workflows/build.yml`, `scripts`, the complete `app` folder and `app/pack-development.jks`. Verify the workflow is named **Build Refreshed My Pixel Mod Pack**. Run **Actions → Build Refreshed My Pixel Mod Pack → Run workflow**. Download the `MyPixelModPack-refreshed-v2.0.1` artifact after the tests, release build, lint and APK entry checks pass.

If you reuse the old repository, replace the workflow too. It removes obsolete modern API application classes and the old FeatureGate before checking the source. Do not reuse a v1.x workflow with its old entry-count checks. The APK has exactly ONE legacy entry: `dev.chet.mypixelmodpack.PackDispatcher`; that dispatcher routes all nine components.

The included development signing key is consistent across builds from this ZIP. It is deliberately a public development key, not suitable for Play Store distribution. Future upgrades built with this same key should not require uninstalling. Keep the key file with the project.

## Install and scope

1. Keep your working standalone setup while building. Disable the old pack in Vector / LSPosed, then uninstall the old pack APK through Android Settings. This refresh uses a new signing key; a direct update from the old APK will normally fail.
2. Install the new APK, open **My Pixel Mod Pack**, and confirm all nine switches are OFF. Enable the pack in Vector / LSPosed and assign scope. Do not enable its switches until its all-OFF baseline has been checked after reboot.
3. Scope **System UI (`com.android.systemui`)** for Battery Gradient, mobile/Vo icons, network activity and DT2W. Scope **Gboard (`com.google.android.inputmethod.latin`) AND Pixel Launcher (`com.google.android.apps.nexuslauncher`) or Launcher3 (`com.android.launcher3`)** for cursor arrows. The arrows are placed in the launcher's taskbar navigation row, not inside the Gboard layout.
4. Navigation icons and OPA require the active launcher. Nav Bar Status Match requires each target app in the pack's scope. **System Framework is not required**, and the dispatcher intentionally skips package `android`.
5. Move one feature at a time: turn its pack switch ON, disable its corresponding standalone module in Vector, then reboot and verify. Keep standalone APKs installed for rollback. Test DT2W last because it was the trigger for the previous pack failure. Its original sensor behavior is retained, but installation is deferred until a context and main Looper exist.

Changing a switch or navigation/Vo appearance requires a reboot so every scoped process uses the same saved settings. Battery, mobile and network appearance retains its existing provider refresh behavior. An already-installed hook is not removed by switching OFF until the affected process restarts. If a feature fails, disable the whole pack and reboot before restoring its standalone module.

Settings in this refresh start from defaults. Old pack feature switches and old navigation/Vo preference stores are ignored. Configure appearance through each feature's settings button. DT2W follows the existing ROM tap-wake settings; this pack does not change those ROM settings.

## Diagnostics

The app has **Copy pack startup diagnostics**. It copies the saved switch values and the latest report from each scoped process. Reports include an epoch timestamp so old reports can be distinguished from the current reboot. `ON; entry point returned` means the component installation method ran; it does not claim every optional hook exists on the ROM. The component's Vector logs provide those details.

If something fails, copy those diagnostics and export Vector / LSPosed logs from the same reboot. Include SystemUI/launcher crash logs if either process repeatedly restarts. A missing provider/settings snapshot is explicitly reported; it cannot silently be treated as all switches OFF.

## Changes from the failed pack

- One dispatcher replaces separate LSPosed entry points. It creates component instances after `Application.attach`, before app startup UI is constructed, with the target app's context and class loader.
- Hook behavior in the working standalone Battery Gradient, Gboard, mobile, Vo, OPA and network sources is retained. Gboard and OPA use the Launcher3 taskbar controller. Battery's independent Iconify row movement and Tasker support remain.
- Feature switches and navigation/Vo settings use an exported, read-only settings snapshot provider, with independent preference files. There are no `XSharedPreferences` reads or world-readable preference requirements. The app is queryable so modern scoped apps can discover the provider.
- Each component entry is isolated so a thrown initialization error is reported and later entries still run. This does not automatically undo partially installed hooks or catch every asynchronous callback inside a component.
- Nav Bar Status Match retains the lazy Handler fix. Navigation modules use direct legacy before/after hooks; no original-method compatibility interceptor or modern entry metadata is shipped.
- DT2W's Handler is constructed after the app context exists. Its first sensor tap is suppressed only after sensor rearming and reset scheduling succeed. Earlier claims that an uncaught DT2W hook error alone explained every failure were not proven by a crash log; the refresh addresses reproducible integration weaknesses without claiming that diagnosis was confirmed.
- Mobile resource events are retained until configuration is read, then forwarded to the enabled mobile component before SystemUI app startup. Its view binder hook remains present.
- Signing is stable across GitHub runners. Settings tests, component failure-isolation tests, build, lint, and APK entry checks run before an artifact is published.

Battery Tasker broadcast action remains `dev.chet.batterygradient.SET_STYLE`. An explicit receiver must target `dev.chet.mypixelmodpack/dev.chet.batterygradient.StyleIntentReceiver`. Scope changes happen in Vector / LSPosed, not through an in-app scope request API.

See `SOURCE_AUDIT.md` for the component sources and integration changes, and `VALIDATION.md` for the checks actually completed.
