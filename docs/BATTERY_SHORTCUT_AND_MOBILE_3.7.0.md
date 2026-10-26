# Battery shortcut and mobile lettering — 3.7.0

Battery gradient > Customize now offers Tap battery to open Battery settings, off by default. It attaches to the Pack battery ImageView and updates through the existing settings observer. Uses android.intent.action.POWER_USAGE_SUMMARY via SystemUI ActivityStarter when available, with a standard startActivity fallback on ROMs without that dependency. Android still controls keyguard dismissal and activity availability. It requires the Pack battery feature, not a separately drawn ROM battery icon. The existing icon size setting determines the tap target size.

Square, Filled, Open Corners and Waves now use the same original rounded glyph paths and letter-height scale as K/H (N retains its heavier stroke). Filled badges still use transparent letter cutouts. All use the existing font-size selector. Waves shares the same letter height but offsets the text downward and scales its quarter-wave arcs from the glyph scale; intrinsic width reserves extra wave space. The 16dp intrinsic icon height is retained.

Based on complete 3.6.1, including DT2W reliability changes and all previous components. Install the Actions APK as an update and reboot. Then enable the battery shortcut and check the Battery settings destination; select a mobile style and font size, then restart SystemUI/reboot to refresh it.
