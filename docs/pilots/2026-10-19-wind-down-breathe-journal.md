# Wind-down pilot: Breathe → Journal → next-morning observation

**Status (2026-10-19): downstream feasibility recipe, not an efficacy experiment or upstream proposal.** This document belongs to the existing NOOP queue; do not create a second habit database, score, RFC or promotion issue.

## What exists
- Android fork [contextual Today PR #51](https://github.com/kvnloo/factory-ryanbr-noop/pull/51) exposes the existing Breathe route beside Journal; manual user choice, no session auto-start and no claimed completion tracking.
- Android and Apple NOOP already expose physiological metrics, Breathe functionality and Journal. Android's `JournalAddRow` permits a custom Boolean question with explicit Yes/No/unanswered, and its day picker offers **Tomorrow** for logging actions that may inform the next morning.
- Fork [#49](https://github.com/kvnloo/factory-ryanbr-noop/pull/49) corrects experiment baseline selection to exclude unanswered days; [#46](https://github.com/kvnloo/factory-ryanbr-noop/pull/46) exercises existing Journal/metric recipe wiring using synthetic data. Both require independent review; do not treat either as clinical evidence.

## Minimal manual walkthrough (14 nights for feasibility, not effect estimation)
1. In Journal, make one custom **Yes/No** item, for example `Did I finish guided breathing before bed?` Record the exact canonical question key; do not invent a second factor taxonomy. If downstream [tiny-habit PR #61](https://github.com/kvnloo/factory-ryanbr-noop/pull/61) is enabled, **reuse its generated Journal question** instead of adding a duplicate entry. PR #61 is an independently held Android prototype, not a shipped upstream feature.
2. At the bedtime cue the user chooses, open **Today → Breathe** on Android and optionally complete a guided session. This navigation is a shortcut, not instrumentation or an intervention prescription.
3. After a fresh 2+ minute Breathe session, the fork now offers **Log journal** directly on Breathe; alternatively use **Today → Journal**. Select **Tomorrow** when logging before that night's sleep, and explicitly answer **Yes** on a completed night, **No** on an observed night without completion, or leave unanswered if unknown. The answers remain self-reported; under-2-minute sessions show no completion shortcut.
4. The next morning, inspect the Journal day attribution and existing sleep outcomes. Record whether a matching `sleep_efficiency` metric, sleep session and source/coverage exist. No sensor data: **no outcome**, not zero.
5. Repeat solely to check app workflows, timestamp joins, explicit controls and export. A 14-night sample is not proof that breathing improves recovery or sleep.

## Evaluation / failure gates
- **Primary engineering outcome:** manual Breathe navigation, session UI, custom Journal answer and following-morning metric can be reconciled end-to-end on a real device without network or extra permissions.
- **Scientific status:** observational only; no randomization, balanced controls, causal statements or clinical claims. Do not calculate a change score if sleep metric coverage is absent or groups are too sparse.
- **Controls:** only explicitly answered No days, never unlogged days (fork #49); Yes/No imports must respect native-answer precedence; a mismatch is a test failure.
- **Day boundaries:** user travel, cross-midnight sleep and time-zone change require explicit inspection, not naïve same-date joins. Confirm `Tomorrow` on pre-bed logging maps to the intended wake morning.
- **Device states:** disconnected strap, unavailable Charge, active workout and stale BLE scanning flags must not imply a completed Breathe session. NOOP #51 has independent pure-state tests; a live-device test is still needed.
- **Safety:** user can stop; no prescriptive intervention, supplement/medication advice, diagnosis or fabricated physiological effect. Outcome remains unspecified if not measured.
- **Privacy and backup:** local-first, consentful manual entries and existing export/delete controls; verify a backup roundtrip before product promotion.

## Promotion decision
**Current: HOLD for downstream pilot evidence.** Require a native Android build/test on the exact PR #51 head, TalkBack/small-screen smoke check, verified manual Journal day join on a real device, and maintainer fit assessment. Promote the smallest independently reviewable fix only when separately authorized; #50 is integration-only and never a merge candidate. Original NOOP contributors own the upstream features; this recipe only connects them.
