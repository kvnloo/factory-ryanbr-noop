# NOOP origin promotion handoff — 2026-10-17

**Permission:** Human explicitly authorized promoting NOOP downstream issue proposals into `ryanbr/noop` during this wave. **Blocker:** Connected GitHub integration returned **403 Resource not accessible by integration** on origin `create_issue` and `add_comment_to_issue` as of 2026-10-17. Do not represent publication as complete until origin URLs exist. Use an authenticated GitHub account with write access to public issues; do not attempt an unauthorized privilege workaround.

**Consolidation:** exactly one new issue; add two targeted comments to existing issues, avoiding duplicate reports. Do not create origin PRs or merge held code under this scope.

## 1. Create one new upstream issue

- Target: https://github.com/ryanbr/noop/issues/new
- Title: **RFC: clinician-informed UX — from wearable insights to safe, useful next steps**
- Full issue body: https://github.com/kvnloo/factory-ryanbr-noop/blob/coord/lane20-promotion-queue-20261016/docs/rfcs/2026-10-17-clinician-informed-actionable-insights-ux.md (copy everything from `## Goal` through the final provenance paragraph, preserving links and caveats)
- After posting, capture the issue URL as the one to send to a clinician. This is a feedback request, **not** clinical sign-off.

## 2. Comment on existing origin issue #735: Android Rest shows 1%

https://github.com/ryanbr/noop/issues/735

> Follow-up on item 2 (Rest showing 1%), with current-source review from Oct 17:
>
> Both Android `InsightsHubScreen.kt` and classic `InsightsScreen.kt` label their Rest outcome `sleep_performance` yet project raw `DailyMetric.efficiency` (0–1) as if it were a 0–100 composite. This also risks distorting classic behavior comparisons, correlations and experiment snapshots. NOOP already has `resolvedSeries("sleep_performance")`, so the narrow approach is to reuse that series instead of inventing scoring.
>
> Fork-local distinct fixes: [Hub #47](https://github.com/kvnloo/factory-ryanbr-noop/pull/47), [classic #48](https://github.com/kvnloo/factory-ryanbr-noop/pull/48). Android build/JVM checks passed on their Oct 12 heads; current-base review and real-device evidence remain outstanding. **Not upstream PRs yet.** Posting here rather than opening a duplicate issue.

## 3. Comment on upstream Journal issue #759: explicit-No baseline follow-up

https://github.com/ryanbr/noop/issues/759

> A distinct call site remains after [merged #1768](https://github.com/ryanbr/noop/pull/1768): Personal Experiment's baseline in both `Strand/Screens/InsightsView.swift` and Android `InsightsScreen.kt` selects days *not in the logged-Yes set*. That treats **unanswered** journal days as if the person explicitly answered **No**, potentially inflating the control group.
>
> Proposed narrow fix: intersect outcome days with the existing explicit-No controls, restricted to before experiment start. [Fork draft #49](https://github.com/kvnloo/factory-ryanbr-noop/pull/49) includes symmetric tests. Oct 12 CI passed Android and macOS, with iOS compile only (iOS tests skipped); no real-world/clinical validation. Distinct from #1768's fixed effect-ranker path; source still present on origin `main` Oct 17. Ready for maintainer triage; no upstream PR opened here.

## 4. Keep these classified correctly

- [Research drafts #42, #43, #44](https://github.com/kvnloo/factory-ryanbr-noop/pulls) are **referenced within the UX RFC only**; don't post as separate approved feature proposals until user need and correctness semantics are shown.
- [Journal synthetic recipe #46](https://github.com/kvnloo/factory-ryanbr-noop/pull/46) remains test evidence, not observed health impact.
- [Integration preview #50](https://github.com/kvnloo/factory-ryanbr-noop/pull/50) is **never** an upstream promotion item.
- [Docs #45](https://github.com/kvnloo/factory-ryanbr-noop/pull/45) is a separate non-UX upstream PR candidate; avoid duplicate `docs/BUILD.md` edits from [upstream #2717](https://github.com/ryanbr/noop/pull/2717). Needs refreshed own-origin permission/review.
- Earlier local access stack #1–41 is not included in this origin-issues-only authorization.

## Closure

Record exact created origin issue URL, comment URLs, thread feedback, and any maintainer changes back into `docs/OSS_FACTORY_PROMOTION_QUEUE.md`. Do not claim origin publication while only fork docs exist. The user can send the origin RFC URL together with https://github.com/ryanbr/noop/releases/latest once it is live.
