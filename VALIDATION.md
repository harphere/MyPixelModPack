# Validation — 2.1.4

October 4, 2026.

- All 36 release unit tests passed with zero failures/errors. Four new tests exercise reattachment of the same navigation host, automatic restoration of removed arrow children, bounded query retries/cancellation after detach, and receiver/view deduplication during repeated recovery.
- The 32 existing battery rendering, provider access, startup isolation and settings/UI regression tests also passed.
- Release assembly and lint passed with no Error/Fatal findings. APK signature verification and single legacy dispatcher checks passed.
- Source comparison confirms all production Java outside Gboard Cursor Keys is byte-for-byte 2.1.3 except version labels. Signing key unchanged. The battery percentage switch, charging bolt and working provider-access fix are preserved.
- Java 17, Gradle 8.11.1, SDK 36 and build tools 35.0.0 were used. Temporary local proxy settings are excluded from the ZIP; GH Actions runs normal build checks.

The tests use actual Android view attachment and broadcast delivery under Robolectric, but do not simulate Vector injection or a physical Gboard/Launcher3 process. Device verification remains necessary. This repair targets a verified lifecycle bug, and cannot promise to resolve every possible missing-hook or ROM-specific issue. Initial installation requires a reboot to load the new hooks into both processes.

Recovery performs no force-stop or shell action. Launcher queries are package-targeted and limited to four per attachment/recovery over three seconds. Gboard input/window events send three visibility reports over half a second, cancelled when hidden or destroyed. No perpetual polling or background service is added.
