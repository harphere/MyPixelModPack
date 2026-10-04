# Source audit — 2.1.4

Baseline: MyPixelModPack-source-v2.1.3-battery-options.zip.

- CursorModule registers the Gboard receiver earlier, adds an input-start visibility sync, cancels pending reports on hide/destroy, and delegates host ownership to NavArrowSession.
- NavArrowSession is stored on its own host view, avoiding a process-global map of strong view references. It unregisters while detached, retains the attach listener, reconnects on attach, deduplicates receivers/views, repairs removed children during layout, and performs four bounded visibility queries.
- Cursor movement and held-button repeat behaviour, glyphs, tint selection and Launcher3 host discovery are retained.
- All production Java outside Gboard Cursor Keys matches 2.1.3 byte-for-byte except two log version labels. Battery options, charging bolt, placement, other features, provider visibility and saved switches are preserved.
- Package ID and signing key unchanged; version 2.1.4, versionCode 34. Workflow artifact version updated.
