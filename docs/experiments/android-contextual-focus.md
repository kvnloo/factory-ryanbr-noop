# Android contextual focus experiment

**Branch:** `experiment/android-contextual-minimal-20261018`
**Status:** fork-only prototype, not an upstream proposal.

## Interaction contract

- **One primary number:** existing, on-device Charge (0–100), always dated.
  Missing/invalid scores display an em dash; never invent a value.
- **Two supporting readings:** Sleep and live heart rate when streaming, otherwise Effort.
  Each opens its existing detailed screen.
- **One contextual action:** active workout, scanning, disconnected, awaiting a score, or review.
  The order is deterministic and unit tested; no AI/network inference.
- **Ephemeral suggestions:** dismiss for the current context only. They return on context
  transitions, and are never saved in the database or preferences.
- **Two-item dock:** Today and More. All advanced features remain in existing routes.
- **Escape hatch:** Full dashboard reveals the original Today. Use its Quick actions
  sheet → Return to focus to switch back, without altering layout preferences.

## Verification

```bash
git checkout experiment/android-contextual-minimal-20261018
cd android
./gradlew --no-build-cache --rerun-tasks assembleFullDebug testFullDebugUnitTest
adb install -r app/build/outputs/apk/full/debug/app-full-debug.apk
```

The debug build has its own `com.noop.whoop.debug` application id, so it can coexist
with the normal release app. **Do not assume it shares the production app's BLE bond
or database**; pairing and local data must be tested independently.

Check fresh onboarding, empty data, active workout handoff, BLE scan and reconnect,
Today/More navigation, screen-reader labels, large fonts, and dark/light modes.
No BLE or analytics algorithms are changed; real-device testing still matters.

## Next experiments

Consider finer contextual signals (e.g. newly synced sleep), contextual shortcut
ranking, and OS-level glance surfaces. Keep any recommendations explainable and
local-first. Do not turn this prototype into a second scoring system.
