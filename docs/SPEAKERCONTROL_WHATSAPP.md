# SpeakerControl — WhatsApp (3.6.0)

## Install
Build the complete source using the included Actions workflow. Install as an update, keep SpeakerControl enabled, and scope the Pack to WhatsApp (com.whatsapp), plus your existing Phone app/InCall UI. Optional WhatsApp Business package: com.whatsapp.w4b. Reboot, or restart WhatsApp after the scope change. Open the Pack and Refresh app access if its settings are unavailable.

## Use
In SpeakerControl select Phone, WhatsApp or WhatsApp Business. During a voice call, STATUS reads the route; ENABLE requests the speaker, DISABLE requests the earpiece, TOGGLE switches based on the observed device. Requests are verified rather than treated as successful just because Android accepted them. Missing earpiece routes (including some video-call configurations) produce an error without changing the route.

## Tasker
Keep existing action names dev.chet.speakercontrol.ENABLE / DISABLE / TOGGLE / STATUS. Use Target: Broadcast Receiver and Package: com.whatsapp (or com.whatsapp.w4b), not the Pack package or Phone package. Retain the TOKEN extra shown in the control screen. Optional reply_package: net.dinglisch.android.taskerm, plus request_id for correlation. Existing Phone tasks continue to use the dialer package.

## Implementation and limits
The WhatsApp backend hooks AudioManager.setMode inside the scoped app and registers its command receiver in a process that enters MODE_IN_COMMUNICATION. Uses getAvailableCommunicationDevices/setCommunicationDevice and verifies getCommunicationDevice. It does not inspect messages, record audio, modify microphone permissions, hook TLS or depend on obfuscated WhatsApp UI class names. It releases its own device preference when the app leaves communication mode. No commands are queued for future calls.

This is not yet verified with the user's WhatsApp build. If WhatsApp uses a native routing path without calling the hooked Java setMode, the receiver may not become ready. Multiple simultaneous communication processes or WhatsApp overriding the selected device may also require a follow-up adjustment. Voice-note communication mode can be indistinguishable from calls via this API; use commands only during an actual call. A timeout outside a call is expected.

If controls fail, capture SpeakerControl and MyPixelModPack logs while starting a WhatsApp call, then sending STATUS and TOGGLE. Expected logs: WhatsApp audio-mode hook ready, WhatsApp command receiver ready, then a route result. Confirm both the sound output and WhatsApp speaker button; the app's visual button may not reflect external route selection immediately.
