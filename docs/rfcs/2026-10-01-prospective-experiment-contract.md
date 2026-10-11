# RFC: Prospective experiment contract over existing Journal factors + metrics

Status: Draft / P0  
Date: 2026-10-01

## Goal

Add the smallest missing primitive from the Limitless → NOOP research: a **prospective experiment
contract** that references existing NOOP factors and metrics without creating a second Journal, scoring
system, scheduler, or recommendation engine.

NOOP already measures state and supports retrospective relationships. This RFC adds a way to say,
*before seeing the result*:

> I predict factor/intervention X, under these declared windows, will move metric Y in direction Z; this
> is the coverage/sample floor and this is what would falsify the hypothesis.

## Existing primitives reused

- Journal / structured factor keys
- existing metric keys
- personal baselines
- local-first analytics
- BehaviorInsights / comparisons
- existing backup/export conventions
- downstream local query stack

This must complement rather than duplicate upstream structured-factor, pre-sleep-feedback, and
Healthspan work.

## First-slice data contract

- `id`
- `title`
- `hypothesis`
- `factorKey`
- `primaryMetricKey`
- baseline window
- exposure/intervention window
- outcome window
- predicted direction
- minimum coverage
- minimum samples
- falsification rule
- created timestamp
- prediction-lock timestamp
- analysis recipe version
- status: planned / running / completed / abandoned

Timestamps use epoch milliseconds in the pure contract so Swift/Kotlin semantics are identical and no
calendar/time-zone behavior leaks into validation.

## Required invariants

- non-empty identifiers / title / hypothesis / factor / metric / falsification rule / recipe version
- every window has `start < end`
- baseline ends at or before exposure starts
- outcome starts at or after exposure ends
- prediction is locked at or before exposure starts
- contract is created at or before prediction lock
- minimum coverage is finite and in `(0, 1]`
- minimum samples is positive

## First implementation slice

1. pure Swift contract + validator in `StrandAnalytics`
2. Kotlin mirror under `com.noop.analytics`
3. matching validation vectors and stable wire values
4. no database migration yet

## Acceptance criteria

- same valid contract is accepted on Swift and Kotlin
- same invalid contract yields the same ordered issue codes
- prediction locked after exposure begins is rejected
- insufficient/invalid coverage configuration is rejected
- round-tripping the Swift Codable value preserves the contract
- no network, storage, scheduler, score, AI loop, or medical claim is introduced

## Non-goals

- persistence
- experiment scheduling
- result/effect computation
- causal inference
- Bayesian optimization
- personal response models
- external cognitive-device integrations
- treatment / supplement recommendations
- any “Limitless” or “NZT” score

Those are downstream layers and must not be smuggled into P0.

## Evidence gate before promotion (2026-10-11)

This is a **structural value contract**, not an integrated experiment. The validator accepts
any nonblank `factorKey` or `primaryMetricKey`; it does **not** check the live Journal
catalog or the user's available metric series. Passing unit tests alone does not show
that an experiment can be executed or that NOOP users need this contract.

### Two candidate recipes against real built-in keys

| Candidate (observational, not causal) | Exact Journal canonical | Existing metric catalog key |
| --- | --- | --- |
| Reported late caffeine vs sleep efficiency | `Did you have caffeine late in the day?` | `sleep_efficiency` (`my-whoop`) |
| Reported screen use in bed vs sleep performance | `Did you view a screen in bed?` | `sleep_performance` (`my-whoop`) |

Sources checked at NOOP origin `e58017508d9731c51d0631fe0eb4bb1d7ea0c582`:
`Strand/Data/JournalCatalog.swift` (`starterQuestions`) and
`Strand/Data/MetricCatalog.swift` (`MetricCatalog` descriptors).
The former P0 test's `caffeine_timing` is **not** a built-in Journal canonical
(the full question text is the key), so the Swift/Kotlin fixture now uses the
actual built-in label. A self-reported No does not establish an earlier caffeine
cutoff or even that caffeine was consumed: this is a binary association question,
not a dosage/timing intervention.

### What the time windows can actually express

- One isolated exposure followed by one later outcome fits the validator.
- A multi-day alternation with one sleep outcome **each night** does not: the
  first night's outcome starts before the overall exposure window ends, and P0
  returns `outcome_starts_before_exposure_ends`. Both language tests now pin
  that specific limitation with epoch-millisecond intervals.
- Splitting a study into one contract per day evades the overlap constraint but
  loses the registered *study-level* comparison/assignment and cannot by itself
  establish an exposure/control sample gate. Do not solve this by silently
  labelling each day an independent trial or by dropping the chronology guard.

### Stopping rule / status

**CHECK — hold P1/P2 promotion.** Before adding persistence, more outcome
schemas, recommendations, or model-fitting, demonstrate two actual local,
consented or fully synthetic end-to-end observation sets using existing
Journal and metric storage. Record the day-boundary join, explicit Yes/No
vs unlogged state, per-group sample and coverage denominators, confounders,
and whether a one-exposure contract is sufficient for the real intended job.
Do not upload personal health observations to GitHub. If the existing
BehaviorInsights/Journal pipeline answers both questions sufficiently,
prefer that to this additional contract.

No user experiment, effect size, real-device collection, or clinical outcome
is claimed by these tests or by this document.
