# My Pixel Mod Pack 2.1.3 — battery styles

This release builds on 2.1.1, retaining its category UI, optional Settings shortcut and working 2.0.2 provider-access fix.

## Battery changes

Under Status bar → Battery Gradient, use **Show battery percentage** to turn the number on or off. This applies live to all four styles, including the preview. Screen readers retain the percentage even when the text is hidden.


- **Filled circle (outside in):** fixed neutral outer track and a centred coloured disc. Radius is proportional to battery percentage, shrinking inward as the charge decreases. Its fixed radial gradient runs red at the centre through amber and yellow to green at the outer edge. Lower charge removes outer green/yellow rings first. At 0% no coloured fill remains.
- **Portrait:** a vertical battery body and terminal. Fixed gradient runs green at the top through yellow and amber to red at the bottom. The fill height is proportional to charge and drains from the top. The neutral outline remains visible.
- Percentage text is optional and OFF by default. Charging uses a lightning bolt to the right of the battery, with reserved space so the battery keeps its full height and does not shift when charging starts. Dashed circle and ring styles are unchanged.
- Style changes apply live. The portrait value is also accepted by the existing Tasker style intent as `portrait`; existing intent names and override/default behaviour remain.

## Build and install

Upload all extracted files, including `.github/workflows/build.yml`, to the repository. Run the Build Refreshed My Pixel Mod Pack workflow and download the MyPixelModPack-ui-v2.1.3 artifact. Install the APK over the current pack without uninstalling; the existing key, application ID, scopes and preferences are retained. Open the pack once and restart System UI or reboot after the update. Select Battery Gradient under Status bar, choose a style and use its preview slider.

Keep equivalent standalone modules disabled while using their pack features.

## Retained behaviour

Hook installation and Iconify row discovery/movement are retained. The icon slot now measures 32 × 24 dp to reserve the bolt lane; its battery body remains 24 dp. Other components and provider visibility repair are retained. The four category pages, diagnostics copying and app access refresh are retained from 2.1.1.

The optional Android Settings shortcut defaults off. Enable it in App & diagnostics, scope `com.android.settings` and restart Settings. Placement depends on the ROM.

Launcher3/Quickstep hosts this device's navigation buttons, including when Nova is selected as home. Scope Launcher3, System UI and Gboard for cursor arrows; force-stop Gboard if it retains an old process. Pixel Launcher is not required.

Java 17, Gradle 8.11.1, SDK 36 and build tools 35.0.0 are used. The existing target SDK 28 is retained for the proven broadcast behaviour. See VALIDATION.md for checks and limits.
