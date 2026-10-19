# Lane 20 — NOOP contribution promotion queue

> **Canonical repository-local queue (2026-10-16).** Issues are disabled in this downstream fork, so this file on `coord/lane20-promotion-queue-20261016` replaces the legacy cross-repository queue at [oss-factory #159](https://github.com/kvnloo/oss-factory/issues/159). Original issue contents and PR discussion remain archived, with authorship intact. **No automatic origin publication/merges.** NOOP uses PolyForm Noncommercial 1.0.0, not an OSI open-source license.

## 2026-10-19 Limitless pilot and exact-head audit

- **NOOP origin unchanged** from the pinned [ryanbr/noop@8e4b0c1](https://github.com/ryanbr/noop/commit/8e4b0c16e7640607600fa6bde5424095eba6034e). Existing experiments/proposals remain scoped to this origin snapshot, not a claim of origin merge.
- **Current heads green:** [#46](https://github.com/kvnloo/factory-ryanbr-noop/pull/46), [#47](https://github.com/kvnloo/factory-ryanbr-noop/pull/47), [#48](https://github.com/kvnloo/factory-ryanbr-noop/pull/48), [#49](https://github.com/kvnloo/factory-ryanbr-noop/pull/49) and composition-only [#50](https://github.com/kvnloo/factory-ryanbr-noop/pull/50) now have completed successful **exact-head** GitHub workflows. #46, #49 and #50 include Android and macOS/iOS **build workflows**, not live wearable or iOS device testing; #47–48 have Android builds/tests. No submitted code review and no upstream PR/merge were established.
- **Android Today #51:** existing draft updated on the same branch with [opt-in Breathe shortcut f397db8](https://github.com/kvnloo/factory-ryanbr-noop/commit/f397db871a0d380393cf6607c665b3d567330b7c), [responsive quick-action layout d3a61ef](https://github.com/kvnloo/factory-ryanbr-noop/commit/d3a61ef911803481a204c3c2a15f4531c7ef123a), and [stale scanning state fix + JVM vectors 4f4e60e](https://github.com/kvnloo/factory-ryanbr-noop/commit/4f4e60e08ba3c1c35bce25deca1ff17b1fdde9ad). On October 19, workflows for exact head `4f4e60e` were **queued**, so no green claim yet. Keep draft/fork-local.
- **Evidence, not another framework:** [wind-down Breathe → Journal feasibility pilot](./pilots/2026-10-19-wind-down-breathe-journal.md) specifies a manual check-in using the existing custom Boolean Journal and Tomorrow attribution. No automatic habit detection, new data store, clinical efficacy assertion, or extra RFC.
- **Origin permissions:** prior GitHub origin issue/comment attempts returned 403. Do not treat fork write access as authorization or technical ability to post to `ryanbr/noop`; the Oct 17 origin-code HITL gate remains unchanged. Pilot may proceed on the fork.

## 2026-10-18 P0 origin snapshot refresh

Origin pinned: [ryanbr/noop@8e4b0c1](https://github.com/ryanbr/noop/commit/8e4b0c16e7640607600fa6bde5424095eba6034e). Added **one sync base branch** `sync/origin-20261018-8e4b0c1` on the fork; neither `ryanbr/noop:main` nor fork `main` was changed. Matched each existing P0 source patch against the origin's current source before replaying it, and retained original fork commits as merge parents:

- [#49](https://github.com/kvnloo/factory-ryanbr-noop/pull/49) — explicit-No experiment controls: head `531c3b41`, **four changed files**, Swift + Kotlin synthetic tests, new [Android](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39111909374) and [Apple](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39111909433) CI.
- [#47](https://github.com/kvnloo/factory-ryanbr-noop/pull/47) — Insights Hub Rest composite: head `8d96b018`, **two changed files**, new [Android CI](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39111950195).
- [#48](https://github.com/kvnloo/factory-ryanbr-noop/pull/48) — classic Insights Rest composite: head `49329609`, **two changed files**, new [Android CI](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39112037092).

- [#46](https://github.com/kvnloo/factory-ryanbr-noop/pull/46) — synthetic healthspan recipes: head `fd2d7466`, **two changed files**, new [Android CI](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39112361799) and [Apple CI](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39112361770).
- [#50](https://github.com/kvnloo/factory-ryanbr-noop/pull/50) — integration-only 9-file preview: head `ac8bb0d6` combines #46–49 on same pinned origin; launched [Android CI](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39112274951) and [Apple CI](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39112274928). **NEVER MERGE.**

**QA gate (verified 2026-10-19):** Current-head GitHub Actions succeeded: #46 and #49 Android build/unit tests plus macOS build/Strand tests and iOS builds; #47 and #48 Android build/unit tests; integration-only #50 Android + Apple checks. On #46 and #49 the `Test NOOPiOS` step was **skipped**, not passed. These are synthetic/software checks, not device or clinical validation. Do not mark as origin-ready until independent review addresses the overlap between #48 and #49 in `InsightsScreen.kt` and explicit approval covers specific upstream PRs. The origin's `InsightsView.swift`, `InsightsScreen.kt`, and `InsightsHubScreen.kt` still contained the reported P0 bugs when pinned. No new feature PRs were created; preview #50 remains non-mergeable by policy. No real wearable/clinical verification.

## Origin publication decision — 2026-10-17

Explicit human authorization in the current conversation: promote NOOP downstream issue proposals to `ryanbr/noop`. Scope: one non-diagnostic clinician-informed UX/impact discussion, deduplicated existing-thread updates for current user-facing Insights/experiment correctness gaps, and links to the P0/P1/P2 research drafts for review. Check origin first and avoid duplicate issues; do not promote or merge held downstream code, make medical claims, copy private data, or ping maintainers. This origin-issue/comment authorization supersedes only the prior no-origin-publication hold for this bounded wave. Track exact upstream URLs here after publication.

## Origin write outcome — 2026-10-17

**BLOCKED: GitHub connected integration has no issue/comment write permission in `ryanbr/noop`.** Both authorized origin `create_issue` and existing-thread `add_comment_to_issue` attempted on 2026-10-17 returned GitHub HTTP 403 (`Resource not accessible by integration`). **No origin issue or comment was created.** Read-only origin audits verified the existing Rest projection and experiment control-selection code, and confirmed overlaps with upstream #735, #759, #762 and prior merged #1768.

Post-ready artifacts on this canonical queue branch:
- [Clinician-informed user-impact UX RFC](./rfcs/2026-10-17-clinician-informed-actionable-insights-ux.md) — proposed **one** non-diagnostic new origin issue, citing work #42–44, #46–49 and relevant current upstream threads.
- [Origin promotion handoff](./rfcs/2026-10-17-upstream-issue-promotion-handoff.md) — exact issue text and two targeted existing-thread comments (#735, #759), plus dedupe/hold rules.

After origin publication through an account with the required access, replace this blocker with exact origin links. This is **not** permission to create origin PRs, merge held drafts or assert clinical benefit.

## Ready for human fork review (not origin approval)

| Priority | Source | Current evidence | Next gate |
| --- | --- | --- | --- |
| P0 | [iOS docs PR #45](https://github.com/kvnloo/factory-ryanbr-noop/pull/45), head `4bf7f9bcafa4a81bf6ac612df30f1f8b49b163d7` | Five distinct documentation files; restored `docs/BUILD.md` to base rather than duplicate UtkuDenizAltiok's [origin #2717](https://github.com/ryanbr/noop/pull/2717). Fork PR non-draft; **all 3 exact-head GitHub Actions PASS**: [Source Hygiene #39004125357](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39004125357), [i18n Coverage #39004125361](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39004125361), [Tools Python CI #39004125337](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39004125337). Docs-only QA, not iOS device validation. | Recheck all exact-head checks/reviews and origin overlapping changes; separately approved origin action only. |

## Needs proof

| Priority | Source | Status / minimal gate |
| --- | --- | --- |
| P0 | [Journal synthetic recipes #46](https://github.com/kvnloo/factory-ryanbr-noop/pull/46), latest observed head `da5cfa4e1734` | **NEW_HEAD_CI_PENDING / FORK REVIEW:** A new Swift GRDB round-trip/test of reimport precedence plus a Swift GRDB + Android Room pair of synthetic two-recipe DB tests landed 2026-10-19. Earlier head `fd2d7466` passed Android, macOS/StrandTests and iOS builds; those results **do not** validate these additions. No device data; a real day-boundary/time-zone join is not proven. Independently review native/Android tests; current-head CI needed. |
| P0 | [Hub Rest #47](https://github.com/kvnloo/factory-ryanbr-noop/pull/47), [classic Rest #48](https://github.com/kvnloo/factory-ryanbr-noop/pull/48) | **CURRENT_HEAD_ANDROID_CI_PASS / REVIEW_HOLD (verified 2026-10-19):** current-origin merge commits in existing branches, 2 changed files each. Android build/unit tests [#47 PASS](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39111950195), [#48 PASS](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39112037092). Independent review and overlap reconciliation with #49 still required; device UX unverified. |
| P0 | [Explicit-No baseline #49](https://github.com/kvnloo/factory-ryanbr-noop/pull/49) | **CURRENT_HEAD_CI_PASS / REVIEW_HOLD (verified 2026-10-19):** 4-file Swift/Kotlin patch on pinned origin with preserved synthetic tests. Android build/unit tests [PASS](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39111909374); macOS build/Strand tests and iOS build [PASS](https://github.com/kvnloo/factory-ryanbr-noop/actions/runs/39111909433); iOS test step **SKIPPED**. Independent review and #48 overlap reconciliation remain required. |
| P1 | [Wearable source attribution draft #52](https://github.com/kvnloo/factory-ryanbr-noop/pull/52) | **DOWNSTREAM DRAFT / NOT_ORIGIN_READY (2026-10-19):** Exactly 5 files, +175/-4 at head `778fb3c5` on pinned `ryanbr/noop@8e4b0c1`. Records Health Connect *writing apps*, not watch-model claims. Three pure Kotlin CLI smoke cases passed; **no full Android CI or real-watch test claim**. Detailed credited [OSS reference matrix](https://github.com/kvnloo/factory-ryanbr-noop/blob/feat/health-connect-origin-attribution-20261019/docs/WEARABLE_OSS_REFERENCES.md). Next: app-build CI, i18n review, real Galaxy/Samsung Health -> Health Connect origin evidence, then independent review before any promotion. No upstream publication. |
| P1 | [Prospective contract #42](https://github.com/kvnloo/factory-ryanbr-noop/pull/42), [receipts #43](https://github.com/kvnloo/factory-ryanbr-noop/pull/43), [outcomes #44](https://github.com/kvnloo/factory-ryanbr-noop/pull/44) | **CHECK/HOLD:** prospective user requirement, cross-platform runtime, no causal health claims. |

## 2026-10-19 — Limitless execution and prospective multi-day gate

- Reused [#46](https://github.com/kvnloo/factory-ryanbr-noop/pull/46) rather than creating a second feature PR. Committed synthetic, in-memory SQLite/GRDB and Room DB read/write tests for BOTH late-caffeine/sleep-efficiency and screen-in-bed/sleep-performance recipes. One concurrent worker also added a GRDB test of WHOOP reimport plus native-No precedence; retain its independent provenance. A local SQL-only fixture check agreed on nYes=5, nNo=5, nUnanswered=2 and synthetic deltas −6/−12; **this is not a Swift/Android CI result**.
- Reviewed [#49](https://github.com/kvnloo/factory-ryanbr-noop/pull/49): the explicit-No baseline selection excludes unanswered days, matching existing Journal merge semantics; current-head native checks previously passed. Reviewed [#47](https://github.com/kvnloo/factory-ryanbr-noop/pull/47) and [#48](https://github.com/kvnloo/factory-ryanbr-noop/pull/48): source-resolved 0–100 Rest composite replaces 0–1 efficiency. All remain fork-only pending independent review, hardware/UX validation as relevant, overlapping `InsightsScreen.kt` reconciliation, and separate upstream permission.
- [#42](https://github.com/kvnloo/factory-ryanbr-noop/pull/42) remains **HOLD**. Its single exposure window followed by one outcome window cannot represent alternating daily habits with an outcome each night. Splitting into one contract per day loses study-level preregistration and comparison; do not weaken chronology or invent a second Journal.
- **Decision:** first use the existing Journal/BehaviorInsights day-level Yes/No/missing pipeline for descriptive associations. If a documented prospective use case requires more, design a SINGLE study-level registered plan referencing multiple dated episodes plus a prespecified outcome-day mapping, lag, sample and coverage thresholds, and provenance; keep individual observations in existing stores and test DST/midnight/sleep wake-day attribution on Swift and Kotlin before reconsidering #42–44. A model/optimizer is later, not this wave.

## Held or superseded

- [Integrated preview #50](https://github.com/kvnloo/factory-ryanbr-noop/pull/50) is composition-only. **Do not merge it** as a substitute for individually reviewed component PRs.
- Earlier [fork PRs #21–41](https://github.com/kvnloo/factory-ryanbr-noop/pulls) are **not automatically ready**. Reconcile owner/review/CI before prioritizing.
- `docs/BUILD.md` edits are owned by [upstream PR #2717](https://github.com/ryanbr/noop/pull/2717) (UtkuDenizAltiok). Do not republish identical edits.

## Evidence and policy gate

Review existing maintainer issues, all open owned PRs, current-head CI, baseline behavior, license and permission before requesting upstream changes. An explicit human approval for the specific origin action is required under [factory HITL](https://github.com/kvnloo/oss-factory/blob/main/factory/HITL.md); Linear is **retired**. Fork review-ready is not origin-ready. No human health records or claims about sensor/medical efficacy.

**Attribution:** ryanbr and NOOP maintainers supplied Rest scoring/resolver and release behavior; original contributors including digitalerdude, paradix86, and UtkuDenizAltiok retain credit for their findings/designs. Retain each PR's own detailed evidence and history. Do not tag maintainers in downstream coordination.

**Workflow:** Before each subsequent wave, read this same file and original PR heads. Update one row only when the evidence or status changes; do not mint another generic queue. The factory [#14](https://github.com/kvnloo/oss-factory/issues/14) is a repository index, not an authorization or QA board.
