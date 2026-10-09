# Validation: 2.2.0

Completed on 2026-10-08 against the final source:

- Gradle 8.11.1, AGP 8.9.2, Java 17, compile SDK 36, build tools 35.0.0.
- `:app:testReleaseUnitTest :app:assembleRelease :app:lintRelease`: BUILD SUCCESSFUL.
- 43 unit/Robolectric tests; zero failures, errors or skipped tests. Includes the existing 36 tests, five charging tests and two combined-settings tests.
- Native Android rendering verifies all four pulse styles change the charged area, leave transparent pixels and the bolt lane unchanged, and stop at full charge. A drawable layer with SRC_ATOP keeps highlight coverage inside the existing pixels.
- Scheduler tests verify cancellation and recovery for hidden/disabled/full/unplugged states. Attached-view tests exercise SCREEN_OFF/SCREEN_ON and detach/reattach.
- Settings tests verify the live animation switch, percentage/style preservation, ChromePie private store and provider snapshot, and both new Pack gates defaulting OFF.
- Release lint: no Error/Fatal findings. Retained incomplete ChromePie translations deliberately fall back to English (file-local MissingTranslation suppression); its original hidden statusbar service lookup has a method-local WrongConstant suppression. No global lint disabling/baseline added.
- APK signature verified with apksigner (v2, one signer).
- Source/APK structural checks verify one legacy dispatcher, eleven independently gated feature components, optional Settings shortcut, unique providers, manifest/XML, private settings access, and absence of modern Xposed metadata.
- Byte comparison with 2.1.4 confirms all existing non-battery feature Java packages—including Gboard—and the included signing key are unchanged. Only battery, Pack UI/runtime/dispatcher/provider wiring and new component files changed.
- ChromePie incorporates the supplied base plus test2 through test7 source updates; matching, icon mode, near-white light-grey treatment and first-opening retries remain. Settings reads now use the Pack provider; zygote startup/world-readable preferences are removed.
- SpeakerControl preserves the supplied 1.0.0 commands, token, call route handling and verification. Registration now uses an already available Pack host context. Phone-host detection limits hook installation to default dialer/InCall hosts.

The GIF in this folder is rendered from the actual Android drawable in Robolectric. It is a preview, not an injected phone capture. Runtime Vector injection, ordinary phone-call routing and Titanium pie interaction still require device confirmation. Gboard's reboot recovery issue is not addressed in this release.
