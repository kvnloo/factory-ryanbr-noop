# RFC follow-up: Lagged outcome receipts + evidence provenance

Status: Implementation / P1  
Blocked by: P0 prospective experiment contract

## Goal

Turn a completed prospective experiment into a local, auditable **evidence receipt** rather than a
mutable retrospective story.

## First implementation slice

The first code slice is deliberately pure and DB-free on both Swift and Kotlin.

A receipt embeds the **entire preregistered P0 contract snapshot** instead of copying a few fields.
That freezes the original hypothesis, factor/metric keys, windows, prediction direction, evidence
gates, falsification rule, lock timestamp, and analysis recipe version inside the receipt itself.

Observed evidence then adds:

- analysis timestamp
- data-source ids using NOOP's existing provenance vocabulary
- baseline + outcome sample counts
- baseline + outcome coverage / derived missingness
- derived lag from exposure end -> outcome start
- effect estimate
- uncertainty interval
- confounder annotations
- result: supports / contradicts / inconclusive

## Evidence rules

- an invalid P0 contract cannot be hidden inside a receipt
- analysis cannot be finalized before the prespecified outcome window closes
- source provenance must be present, nonblank, and duplicate-free
- observed coverage must be finite and in [0, 1]
- sample counts cannot be negative
- effect estimates and uncertainty bounds must be finite
- an uncertainty interval must have lower <= upper
- when both are present, the point estimate must lie inside its uncertainty interval
- a **supports** or **contradicts** result is illegal unless:
  - an effect estimate exists
  - an uncertainty interval exists
  - baseline and outcome coverage both meet the P0 coverage gate
  - baseline and outcome sample counts both meet the P0 sample gate
- weak / missing evidence may only resolve to **inconclusive**

This does not itself decide whether an effect supports the prediction. That classification remains
owned by the preregistered analysis recipe + falsification rule; P1 only prevents a conclusive label
when the declared evidence floor was not met.

## Why embed the contract instead of hashing it?

The repository's cross-platform contract requires stable platform-neutral identities for persisted
data. Before introducing another hash algorithm or backup format, embedding the compact immutable
contract gives an exact auditable snapshot with no new identity primitive.

A later persistence slice can add a canonical digest only if a concrete storage/export need justifies it.

## Persistence follow-up

Persistence is intentionally deferred until the receipt semantics are reviewed.

When persisted:

- Swift GRDB + Android Room schemas must land in the same PR
- migrations must be additive and covered by existing schema-oracle / migration tests
- contract + receipt provenance must survive backup/export
- later corrections must be auditable rather than silently rewriting prior evidence

## Non-goals

No scheduler, optimizer, coaching recommendation, causal claim, autonomous intervention selector,
or hidden score.

This is the concrete implementation of the historical “epistemic firewall” idea.
