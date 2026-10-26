# Auto SpeakerControl — 3.7.0

## Tasker setup
Use Send Intent. Action: dev.chet.speakercontrol.TOGGLE. Package: dev.chet.mypixelmodpack. Target: Broadcast Receiver. Keep TOKEN:<the value shown in the Pack SpeakerControl screen>. Class and Data: blank. Optional reply_package:net.dinglisch.android.taskerm and request_id:<your correlation value>. ENABLE, DISABLE and STATUS work through the same Auto receiver. Existing tasks targeting a dialer or WhatsApp package continue to target that app explicitly.

## Scope and operation
SpeakerControl must be enabled. Scope the Pack to the default Phone app/InCall UI and WhatsApp (and Business only if used), then reboot. No System Framework scope is needed. In the control screen Auto is the default target; manual targets remain available.

Each command gathers authenticated STATUS replies for one second. If exactly one distinct backend reports an active call, the command goes to that package once and its verified result is relayed. Zero positive replies or multiple positive replies produce a failure result and no route change. It does not queue commands for a future call. Simultaneous Auto requests are rejected while one is in progress. Late discovery replies are ignored. A command timeout is reported without resending TOGGLE.

Auto detects only responding hooked backends, not every call Android might have. WhatsApp detection inherits the Java AudioManager communication-mode limitation described in SPEAKERCONTROL_WHATSAPP.md, including potential voice-note mode and native-routing differences. If a backend is missing/not scoped/not ready, Auto cannot infer its state. If both Phone and WhatsApp report active, select a manual target. The backend checks its call/audio state again before changing routes.

## Device checks
With no calls: Auto STATUS/TOGGLE should return no active backend and change nothing. During a phone call, then a WhatsApp voice call: STATUS identifies the backend and TOGGLE changes sound output once. Test speaker-to-earpiece and back, headset availability, call end during discovery, and consecutive Tasker requests. Confirm on the device; this release has not been exercised in Vector on a phone.
