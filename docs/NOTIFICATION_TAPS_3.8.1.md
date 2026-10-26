# Notification tap correction, 3.8.1

The first implementation required taps to land inside a notification icon view's small vertical rectangle. The new targeting uses the full status bar height, screen coordinates consistently, a 6dp horizontal gap allowance and nearest-centre selection. All notification icons participate, so an unsupported icon cannot select a supported neighbour. Hidden/overflow-dot icons are excluded where the ROM exposes getVisibleState.

The touch hook resolves PhoneStatusBarView's actual dispatchTouchEvent implementation, including ROM overrides. A launcher Intent is first sent through ActivityStarter, then a direct startActivity fallback if the ROM's Dependency lookup fails. If no launcher is available, an activity-only notification PendingIntent is the final fallback and may open the specific message rather than the app home. Service/broadcast notification actions are never invoked. Screen must remain unlocked and notification user must match SystemUI's user. Each gesture queues only one launch.

Diagnostics now expose notification_tap_last: hook reached/no icon, selected app, gesture cancelled, launch requested, or failure. No email subject, message content, sender or notification key is logged. Launch-requested means the API accepted the request, not proof that the app displayed.

Install APK built from this complete ZIP and REBOOT after installation. Keep Status bar → Tap notification icons enabled and System UI scoped. Test the small AquaMail icon in the top status bar while the notification shade is closed and the device unlocked. This feature does not change notification cards in the expanded shade. No app force stop or extra app scope is needed for tapping.

Local targeting/gesture harness and Java compilation passed. Full Android unit tests, lint and APK build run in GitHub Actions. Device cause remains unconfirmed until retested.
