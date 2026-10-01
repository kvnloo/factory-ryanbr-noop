# Experiment RFC: Local external outcome observations

Status: Prototype implementation / P2  
Blocked by: P0 + P1

## Goal

Prototype one narrow generic external-outcome contract so NOOP physiology can be studied against
outcomes NOOP does not natively own, without turning NOOP into a cognitive-training or device-specific
measurement suite.

Examples:
- reaction time / PVT
- retrieval-test accuracy
- 24h / 7d / 30d retention
- task completion time
- typing/error rate
- subjective flow/focus
- externally computed cognitive score

## Dedup check

This is **not** a second metric store.

Existing primitives already cover adjacent shapes:

- `metricSeries` is the right substrate for daily scalar projections, but its natural key is
  `(deviceId, day, key)`. It can preserve only one value per key/day, so it cannot represent several
  sessions or trials on the same day.
- Lab Book preserves precise timestamps and then projects latest-per-day into `metricSeries`, but its
  semantics are intentionally lab/health-record specific. Reusing it for PVT, retrieval, typing, or flow
  would pollute that domain model.
- the generic event log can carry opaque payloads, but it is not a typed scalar-outcome analytics
  contract. It remains a possible persistence transport to evaluate later; P2 does not couple to it.

The missing primitive is therefore only: **multiple timestamped, versioned scalar observations per
day/session with explicit provenance**.

## First implementation slice

Pure Swift + Kotlin value type and validator only. No database migration.

Fields:

- stable observation id
- `observedAtMs`: measurement timestamp
- `ingestedAtMs`: when NOOP received/constructed the observation
- outcome key
- finite numeric value
- explicit unit
- source id
- optional source-native record id
- optional session id
- required measurement protocol/version

`observedAtMs` and `ingestedAtMs` deliberately have **no ordering invariant**. An external device or
tool may have clock skew; preserving both facts is better than silently coercing or rejecting one.

## Validation

- ids/keys/unit/source/version cannot be blank
- timestamps cannot be negative
- values must be finite
- optional source-record/session ids must be nonblank when present
- no higher-is-better semantics or interpretation is stored in the raw observation

## Constraints

- local-first only
- generic observation seam, not a Muse/EEG/VRT/dual-n-back subsystem
- no medical claims
- no autonomous recommendation loop
- no result interpretation in this record
- prototype before adding core persistence/UI

## Persistence decision

Do not add a new table yet.

After real experiment recipes exercise this contract, choose the smallest persistence route that
preserves:
- multiple same-day observations
- source-native identity / dedup
- session grouping
- measurement version provenance
- lossless backup/export
- Swift/Kotlin parity

A daily projection into `metricSeries` may be useful later, but must be a derived view rather than the
source of truth because aggregation would destroy trial/session detail.

## Promotion gate

Promote beyond experiment status only if at least two independent experiment recipes need the same
generic observation contract and cannot be represented losslessly with existing primitives.
