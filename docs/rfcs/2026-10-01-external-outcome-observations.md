# Experiment RFC: Local external outcome observations

Status: Experiment / P2  
Blocked by: P0 + P1

## Goal

Prototype one narrow generic external-outcome record so NOOP physiology can be studied against outcomes
NOOP does not natively own.

Examples:
- reaction time / PVT
- retrieval-test accuracy
- 24h / 7d / 30d retention
- task completion time
- typing/error rate
- subjective flow/focus
- externally computed cognitive score

## Proposed record

- timestamp
- outcome key
- numeric/value payload
- unit
- source
- optional session id
- provenance

## Constraints

- local-first only
- generic observation seam, not a Muse/EEG/VRT/dual-n-back subsystem
- no medical claims
- no autonomous recommendation loop
- prototype before adding core persistence/UI

## Promotion gate

Promote beyond experiment status only if at least two independent experiment recipes need the same
generic observation contract.
