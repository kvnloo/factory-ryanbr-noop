# Experimental Oura Gen 3 gesture → Lawnchair z0live

**Downstream Android-only research. OFF / not wired to live BLE.** This change
provides a headless impulse detector and an explicit Activity intent factory.
It does not add a setting, start capture, pair a ring, launch z0live automatically
or record audio. The existing Oura HR/IBI and WHOOP paths are untouched.

## Why this needs a separate experiment

NOOP has Oura Gen3 app-key authentication and a live HR GATT source, but it
does **not** yet use the separate realtime accelerometer channel. The available
clean-room research describes a 0x06 realtime-measurement control with the
accelerometer type bit and 0x33 live ACM notifications carrying pairs of
three-axis i16-LE samples. Those claims require an original-ring gold capture
before being used to create new BLE commands and parsing paths. The ring
does not expose a verified contact/touch double-tap sensor: taps are *inferred*
from accelerometer impulses.

The pure `OuraTapDetector` consumes timestamped XYZ integers, uses a gravity
baseline, refractory/quiet/cooldown gates, and emits double/triple/quad gestures.
**380 counts is a synthetic-test starting estimate, not a hardware result.**
It intentionally does not read physiological measurements or persist data.

## Handoff already possible from a visible UI

`OuraZ0LiveHandoff.visibleControlsIntent()` constructs an explicit, pinned
`ACTION_VIEW z0live://voice` intent for the fork's
`app.lawnchair.play.debug` z0live link trampoline. When the caller explicitly
starts this intent from a visible UI, Lawnchair opens its controls.
**The link cannot auto-start microphone capture.** It must never be called as
a background BLE launch, and no receiver is exported in NOOP for this feature.

## Next hardware and security milestones

1. Owner explicitly enables a **bounded** Oura Gen3 motion session; reuse the
   existing ring auth key in Android Keystore. No reset/rekey or pairing takeover.
2. Implement independently written 0x06 live ACM start/stop and 0x33 frame
   decode from facts, after golden packets are available. **Always** issue
   realtime-OFF on success, timeout, error, disarm and disconnect. Stop the
   sensor when NOOP is not armed; cap battery use and false positives.
3. Calibrate hand taps vs walking/typing/hand washing; log only opt-in,
   app-private, bounded diagnostics. Exclude raw capture and keys from PRs.
4. Use a visible Activity or notification action to open z0live controls.
   Background BLE events cannot silently start an Activity or microphone.
5. In Lawnchair, a separate authenticated/armed session gate can later route
   a fresh double-tap to an **already running** z0live session's mute/unmute.
   Never trust an exported raw broadcast or caller-supplied Boolean.
6. Qualify on a real Ring Gen 3 + Android 17 / Samsung One UI: packet shape,
   other Oura-app connection coexistence, latency, false-trigger rate,
   battery drain, permissions and emergency disconnect.

## Provenance

Cross-repo design: [Lawnchair issue #26](https://github.com/kvnloo/lawnchair/issues/26).
Desktop donor: [kvnloo/.files PR #25](https://github.com/kvnloo/.files/pull/25).
Research facts: [NOOP OURA_PROTOCOL](https://github.com/ryanbr/noop/blob/main/docs/OURA_PROTOCOL.md)
and [Th0rgal/open_oura](https://github.com/Th0rgal/open_oura).
No source code copied from reverse-engineering references.

## Verification

- Kotlin unit tests: `cd android && ./gradlew testFullDebugUnitTest --tests com.noop.oura.OuraTapDetectorTest`
- Android compilation: `cd android && ./gradlew compileFullDebugKotlin`
- Physical BLE + z0live session qualification: **NOT RUN**.
