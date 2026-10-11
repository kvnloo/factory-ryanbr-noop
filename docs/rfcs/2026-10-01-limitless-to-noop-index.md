# Limitless → NOOP RFC index

Status: downstream research plan  
Date: 2026-10-01  
Base: `staging`

This index translates the deep-research report into the smallest non-duplicative NOOP workstream.

The central rule is: **reuse existing NOOP primitives before creating anything new**.

NOOP already owns local physiological data, baselines, Journal/context, retrospective BehaviorInsights,
sleep/recovery/readiness/healthspan analytics, and a downstream local query stack. The missing seam is
prospective evidence: declaring a prediction before exposure, preserving the analysis contract, then
recording delayed outcomes with provenance.

## Dependency chain

### P0 — Prospective experiment contract

RFC: [Prospective experiment contract](2026-10-01-prospective-experiment-contract.md)

Pure cross-platform value type + validator. No persistence in the first slice.

Unlocks:
- preregistered N-of-1 experiments
- timestamped hypotheses
- explicit baseline / exposure / outcome windows
- falsification before retrospective analysis

### P1 — Lagged outcome receipts

RFC: [Lagged outcome receipts](2026-10-01-lagged-outcome-receipts.md)

Blocked by P0.

Adds auditable result receipts with coverage, missingness, recipe version, provenance, effect estimate,
uncertainty, and an explicit supports / contradicts / inconclusive result.

### P2 — External outcome observations

RFC: [External outcome observations](2026-10-01-external-outcome-observations.md)

Blocked by P0 + P1.

Generic local observations for outcomes NOOP does not natively own: reaction time, retention, task time,
typing error rate, subjective flow, etc. Do not create device-specific cognitive subsystems.

### P3 — Personal response-model research harness

RFC: [Personal response-model harness](2026-10-01-personal-response-model-harness.md)

Blocked by P0 + P1 + P2.

Research-only until held-out prospective calibration beats transparent baselines.

### Experiment backlog — historical “Limitless” ideas as recipes

RFC: [Experiment backlog](2026-10-01-limitless-experiment-backlog.md)

Blocked by P0; some recipes also require P2.

Wind-down, caffeine timing, training timing, breathwork, sleep regularity, flow, learning-state matching,
and AI-assisted vs unassisted performance should become experiment recipes over existing primitives,
not new branded engines.

## Explicitly not creating

- a Limitless / NZT / biological-optimization score
- a second Journal or behavior database
- a second Healthspan engine
- a second local API / MCP surface
- a generic “digital twin”
- autonomous treatment or supplement recommendations
- neurostimulation control
- a cognitive-training suite

## Existing work to extend instead of duplicate

- structured Journal/context work (upstream `ryanbr/noop#759`)
- pre-sleep HR → next-morning outcome work (upstream `ryanbr/noop#1760`)
- Healthspan work (upstream `ryanbr/noop#2071`)
- downstream local query stack (fork PR #8)

GitHub Issues are disabled in this fork, so these RFC files are the canonical downstream work items until
Issues are enabled.
