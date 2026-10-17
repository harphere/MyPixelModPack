# Validation — 3.5.0

Baseline: complete 3.4.1, versionCode 44. Adds a WhatsApp communication-device backend and call-app selector. Retains the existing phone backend, actions, token, all other features and prior build-test fix.

All SpeakerControl Java classes compile locally against Android and Xposed API jars (using the compiled PackRuntime API from the previous targeted compilation). Source structure, XML and release hash checks pass. Full Android tests, lint, release build and APK signature checks run in Actions and were not run locally. WhatsApp call detection/routing, headset transitions and call-end cleanup require physical-device testing. See docs/SPEAKERCONTROL_WHATSAPP.md for limitations and diagnostics.
