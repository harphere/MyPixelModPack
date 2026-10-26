# My Pixel Mod Pack 3.8.0

Waves now use arcs with a radius of 31% of icon height and a 6.5%-height stroke. They sit beside the top of the G, with added horizontal room, rather than squeezing small arcs above the letters. Existing font size options still apply to the letters.

Enable Status bar → Tap notification icons, then reboot or restart System UI. Default is off. Scope System UI only for this feature; AquaMail/YAATA/Phone need no extra scope for taps. Only visible notification icons for AquaMail (org.kman.AquaMail), YAATA (rpkandrodev.yaata), Android/Google Phone or the default dialer qualify. Opens the app home, not the individual message. Lock screen and other-user/work-profile notifications are excluded. No notification is marked read or dismissed.

Android reports one touch coordinate, not every icon covered by the finger. Only one visible icon is selected on DOWN; UP must remain over that same view. Unsupported icons do not open adjacent supported apps. Dragging beyond touch slop, a long press or multiple fingers cancels the shortcut. Normal shade drag events pass through. For a successful tap SystemUI receives CANCEL before the app is started through its ActivityStarter API. If the ROM lacks that API the shortcut logs a failure and opens nothing.

Compatibility depends on the ROM retaining PhoneStatusBarView and StatusBarIconView; device validation is required. Diagnostics show notification_tap startup and Vector logs use PackIconTap for launch failures.

All prior components, battery tap shortcut, configurable batteries, charging animations, SpeakerControl and DT2W changes are retained.
