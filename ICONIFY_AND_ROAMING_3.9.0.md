# My Pixel Mod Pack 3.9.0

## Iconify dual status bar

Battery placement now locates Iconify's tagged status-bar battery anywhere in the status-bar tree, retains the native anchor after it moves, and follows the battery branch beside the signal branch. It no longer assumes the first row is always the battery row or that the battery remains a direct ComposeView child of system_icons. Row swapping and portrait-only/single-row orientation changes preserve the logical battery branch. Existing sizing, orientation, percentage, charging animation and battery Settings tap remain unchanged. The separate charging bolt remains removed.

Reference inspected: Iconify beta commit f619d546fe14bbee38cc7748139f31168e6255e1, DualStatusbar.kt and Preferences.kt (dual_statusbar_end_side / iconify_sb_battery_icon). https://github.com/Mahmud0808/Iconify/blob/f619d546fe14bbee38cc7748139f31168e6255e1/app/src/main/java/com/drdisagree/iconify/xposed/modules/statusbar/DualStatusbar.kt

The exact debug 3054 APK was not available for execution; this is a source-based compatibility correction, requiring device validation. Prefer Iconify's default battery style while the Pack provides the battery artwork. Dual status bar can remain enabled. Changing both modules' battery styles at the same time can compete for rendering.

## Optional roaming indicator removal

Status bar → Hide roaming indicator (R), default off. Enable and reboot/restart System UI. Disabling also requires restart. This independent feature does not require the Pack's 4G/5G artwork switch. It hides only SystemUI's mobile_roaming and mobile_roaming_space views, including later visibility updates from the modern mobile icon binder. No subscription, radio, roaming status, APN, mobile data, call handling or carrier setting is changed. A different ROM implementation without those view IDs is left unchanged, with a diagnostic.

## Retest

Build the complete ZIP via the included GitHub Actions, install MyPixelModPack-3.9.0.apk and reboot. Keep existing LSPosed scope; System UI is sufficient for these changes. Test the battery in both dual-status-bar row orders and after rotating. Test roaming R hidden with the switch on, then restored after turning it off and rebooting. Diagnostics include battery placement, hide_roaming and hide_roaming_view.

All previous Pack features and settings are retained. A source-integrity lock is included: replace the complete repository contents, including .github, scripts and the lock file, rather than mixing release files.
