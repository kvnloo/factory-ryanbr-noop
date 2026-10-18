# RFC: Clinician-informed UX — from wearable insights to useful next steps

> Ready-to-post upstream issue draft. Original source: downstream Lane 20 and research PRs. Not yet published in `ryanbr/noop` due GitHub integration 403. Not a clinical claim or a request for medical-device functionality.

**App:** https://github.com/ryanbr/noop/releases/latest  
**Proposed upstream title:** `RFC: clinician-informed UX — from wearable insights to safe, useful next steps`

## Goal

NOOP makes wearable data available offline and locally. How do we improve the **user's ability to understand a trend, judge its uncertainty, and choose one appropriate, low-risk action**, rather than just showing more numbers?

Seek practitioner/clinician and real-user **UX critique**, not endorsement, diagnosis, clinical validation, or treatment advice.

## 10-minute review: four existing journeys

1. **Today / Readiness** — Can a user distinguish a measured value from a derived score or estimate, see what changed, and recognize missing/stale data?
2. **Journal → Insights / “What moves you?”** — Are observed associations framed without causal overclaiming? Is the suggested follow-up easy and optional?
3. **Personal Experiment** — Can someone understand an explicit **No** vs **not logged**, a baseline, an inconclusive result, and whether a proposed habit test is worth doing?
4. **Coach / next step** — Can the app explain *why*, propose a simple user-controlled next step, and require confirmation before any edit?

**Questions for a clinician:** What is confusing or could generate false reassurance/anxiety? Which low-risk behavior or self-observation is reasonable to suggest? What uncertainty/context or plain-language caution is missing? What UX change would most improve follow-through? What should be omitted?

## Design hypothesis (needs validation)

**Signal → context/drivers → source & confidence/coverage → one optional next step → follow-up.** A valid output is **“not enough information.”** Show no automatic diagnosis, triage, prescriptions, hidden medical verdict, or definite causal language.

Evaluate three *specific* proposed screen/copy changes with qualitative comprehension tasks and simple non-clinical UX measures (can the user find the explanation, describe uncertainty correctly, identify a next step, and decline without friction?). Do not infer health improvement from synthetic tests or engagement metrics.

## Existing upstream context: no competing system

- Journal factors, merge/insight semantics: https://github.com/ryanbr/noop/issues/759
- Coach actions with preview + confirmation: https://github.com/ryanbr/noop/issues/762
- Existing Android Insights UX report including Rest showing 1%: https://github.com/ryanbr/noop/issues/735
- Healthspan screen and contributor/uncertainty direction: https://github.com/ryanbr/noop/issues/2071
- Unanswered days are not a No in generic effect comparisons: https://github.com/ryanbr/noop/pull/1768 (merged)

## Downstream evidence, not upstream approvals

- Existing Journal/metric recipes, **synthetic only**: https://github.com/kvnloo/factory-ryanbr-noop/pull/46
- Separate Android Rest/composite projections in Hub and classic Insights: https://github.com/kvnloo/factory-ryanbr-noop/pull/47 and https://github.com/kvnloo/factory-ryanbr-noop/pull/48
- Personal Experiment baseline uses explicit No days: https://github.com/kvnloo/factory-ryanbr-noop/pull/49
- Research **HOLD**, pending verified unmet user requirement and native integration: prospective P0 contract https://github.com/kvnloo/factory-ryanbr-noop/pull/42 ; evidence receipts P1 https://github.com/kvnloo/factory-ryanbr-noop/pull/43 ; external outcomes P2 https://github.com/kvnloo/factory-ryanbr-noop/pull/44

P0–P2 do not yet prove actual user need, experimental effect or clinical merit. Reuse the existing Journal/Metric/Coach contracts before adding a parallel schema or scoring layer.

## Requested first deliverable

A prioritized list of **three changes to existing screens/copy**, plus concrete examples of misleading wording and comprehension checks. Decide with the maintainer which original upstream issue should own implementation. Review with synthetic/example data only.

**Boundaries:** offline/on-device; no patient data or uploaded records; NOOP is not a medical device; no claims of health efficacy, diagnosis, prescriptions, automatic treatment or user behavior change. See https://github.com/ryanbr/noop/blob/main/docs/SCOPE.md and https://github.com/ryanbr/noop/blob/main/docs/CONTRIBUTING.md.

**Provenance:** NOOP maintainers and earlier contributors own the existing Journal, metrics, Rest resolver, and Coach flows; this consolidates downstream experiments and requests feedback rather than asserting those systems were invented here.
