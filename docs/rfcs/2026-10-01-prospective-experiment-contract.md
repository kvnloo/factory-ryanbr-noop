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
