# Research RFC: Personal response-model harness

Status: Research / P3  
Blocked by: P0 + P1 + P2

## Goal

Research a falsifiable personal response model only after NOOP has enough prospectively structured
evidence.

Target form:

`E[Y(t+k) | state(t), intervention(t), context(t)]`

- state comes from existing NOOP measurements and baselines
- intervention/context comes from Journal + experiment contracts
- outcome comes from NOOP metrics or the external-outcome seam

## Start boring

First candidates:
- stratified personal baselines
- shrinkage / partial pooling
- simple state-space models where justified

The model must be allowed to say **insufficient evidence**.

## Promotion gate

No product RFC until a model:
1. is evaluated on held-out prospective experiments,
2. beats a transparent baseline,
3. exposes uncertainty/calibration,
4. does not require a hidden blended score.

## Non-goals

- no “digital twin” branding before predictive calibration
- no treatment recommendations
- no autonomous intervention selector
- no agent-generated claim outranking measured evidence
