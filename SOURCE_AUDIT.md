# Source audit — 3.0

Audited the exact delivered MyPixelModPack-source-v2.2.0-charging-speaker-chromepie.zip, not the surviving 2.1.4 working folder. The ZIP is version 2.2.0 / code 35; its UI uses BuildConfig.VERSION_NAME. An installed UI reporting 2.1.4 therefore does not identify that complete 2.2.0 build.

All eleven components and the optional Settings shortcut are present and independently gated. SpeakerControl/ChromePie sources, permissions, activities, resources, provider store and commons-lang3 dependency are included. Java 17 / Gradle 8.11.1 / AGP 8.9.2 / SDK 36 / build-tools 35.0.0 are retained.

Changes are restricted to shared startup/version diagnostics, battery lifecycle/animation, Nav Match lifecycle and build verification. Gboard, OPA, navigation icons, mobile/service icons, network activity, DT2W, SpeakerControl and ChromePie algorithms retain their 2.2.0 sources. The package, development signing key, saved switches, blacklist and appearance stores are preserved. Full source hashes enforce a coherent release upload.

Version 3.0 / code 36. Device injection, ROM-specific hooks and actual app surfaces still require testing on the user's phone.
