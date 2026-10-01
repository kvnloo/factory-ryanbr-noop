# RFC follow-up: Lagged outcome receipts + evidence provenance

Status: Draft / P1  
Blocked by: P0 prospective experiment contract

## Goal

Turn a completed prospective experiment into a local, auditable **evidence receipt** rather than a
mutable retrospective story.

## Receipt fields

- experiment id
- prediction-created / prediction-locked timestamps
- data sources used
- metric / analysis recipe version
- baseline window
- exposure window
- outcome window
- lag
- sample count
- coverage / missingness
- effect estimate
- uncertainty representation
- confounder annotations
- analysis timestamp
- result: supports / contradicts / inconclusive

## Rules

- inadequate coverage produces **inconclusive**, never a fabricated effect
- provenance and recipe version survive backup/export
- result is reproducible from the referenced windows and recipe
- later edits are auditable
- observational analysis must not emit causal language

## Non-goals

No scheduler, optimizer, coaching recommendation, or hidden score.

This is the concrete implementation of the historical “epistemic firewall” idea.
