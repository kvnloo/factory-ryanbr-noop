# RFC implementation: immutable experiment evidence persistence

Status: P0.5 / P1.5 implementation
Blocked by: P0 prospective contract (#42), P1 evidence receipts (#43)

## Why this exists

The pure P0/P1 contracts can validate a caller-provided snapshot, but by themselves they cannot prove
that a prediction was durably recorded before exposure or prevent a later caller from silently
rewriting stored evidence.

This slice gives those contracts a durable local audit boundary without adding UI, scheduling,
analysis, recommendations, or another score.

## Storage rules

Two evidence-bearing records are immutable by stable id:

1. experimentContract — preregistered evidence fields only
2. experimentReceipt — one completed analysis result referencing a stored contract

For either record:

- first insert wins
- the store itself stamps persistedAtMs
- replaying the exact same evidence is an idempotent no-op
- reusing the same id with different evidence is a hard conflict
- there is intentionally no update API

Receipt source ids and confounder annotations live in ordered child tables so provenance is not hidden
inside platform-specific JSON.

Lifecycle status is not part of the immutable evidence record. planned/running/completed/abandoned is
mutable workflow metadata; persisting it inside the preregistration row would make a normal status
transition look like evidence tampering.

## Chronology

A first contract insert is rejected unless:

created <= prediction lock <= store persistence time <= exposure start

and a first receipt insert is rejected unless:

outcome end <= analyzed <= store persistence time

The usual baseline/exposure/outcome ordering is also checked.

An exact retry is checked against the already-persisted evidence before the current clock is considered,
so a retry after exposure remains an idempotent no-op rather than becoming an artificial failure.

persistedAtMs is still a local operating-system clock, not a cryptographic timestamp. This prevents
accidental late preregistration and silent rewriting; it does not claim resistance to a user deliberately
backdating the device clock.

## Schema

Cross-platform additive schema:

- experimentContract
- experimentReceipt
- experimentReceiptSource
- experimentReceiptConfounder
- index idx_experimentReceipt_contract

Swift: GRDB migration v42-experiment-evidence
Android: Room migration 35 -> 36

The shared schema oracle is updated in the same change.

## Package boundary

WhoopStore does not import StrandAnalytics: StrandAnalytics already depends on WhoopStore.
Storage therefore exposes primitive DTOs; a later adapter maps validated P0/P1 analytics contracts into
these records without inverting the dependency graph.

## Non-goals

- no P2 external-outcome persistence
- no mutable experiment lifecycle table
- no personal-response/Bayesian model
- no causal claim
- no optimizer
- no scheduler
- no recommendation engine
- no Limitless/NZT score
