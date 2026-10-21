# Limitless Protocol → NOOP: conversation-to-GitHub traceability

> **Fork-only design/provenance index, recovered 2026-10-20 (America/Chicago).** This is a map of historical product/research plans to existing NOOP code, fork PRs, specific issues, or work owned by another project. It is **not** a claim that ideas were implemented, scientifically demonstrated, upstream-accepted, or comprehensively recovered from every ChatGPT conversation.

## Evidence and coverage standard

- Sources reviewed: searchable summaries of relevant conversations from 2023–2026; a prior *Limitless → NOOP: Conversation Archaeology, Deduplication Map, and RFC Portfolio* analysis (which expressly marked its chat-history coverage incomplete); the 1,522-line *Limitless / zer0: An Autonomous Human-Augmentation System* research artifact (research hypotheses, not NOOP implementation); live `kvnloo/noop` repository paths, issue/PR inventory and [AGENTS](../AGENTS.md)/[SCOPE](SCOPE.md)/[CONTRIBUTING](CONTRIBUTING.md). These summaries are not original verbatim transcripts.
- The **complete conversation archive is not available from this audit interface**. Exact dates, sequence, original words and supersession for all chat ideas cannot be certified. Items below are *recovered and mapped*, not an assertion of an exhaustive transcript audit.
- Status labels: **BASE** = relevant file visible in fork `main` (not proof of feature completeness); **FORK PR** = pushed branch/code, review/CI/device gates remain; **ISSUE ONLY** = design/evaluation proposal, no implementation claim; **EXTERNAL** = another repo owns implementation; **HOLD** = explicitly blocked; **UNVERIFIED** = requires source or evidence before promotion.
- The **single historical GitHub concept/ABAB index** is [#90](https://github.com/kvnloo/noop/issues/90). This committed document is its versioned technical companion, not a competing backlog; append newly recovered evidence to #90 and update this file through an ordinary reviewed commit.
- GitHub has **one** canonical NOOP promotion queue: [#74](https://github.com/kvnloo/noop/issues/74). [#66–73](https://github.com/kvnloo/noop/issues/66) are task trackers. The research issues below are not new promotion queues.
- **No actual biometrics, private logs, chat transcripts, health measurements, device credentials, or identifiable cognitive-test data belong in public issues/commits.**

## North-star contract

The historical "Limitless pill" / Health OS / Blueprint / ALIE / LUCID vision reduces to one narrow, falsifiable, local-first loop:

`observe state → choose/record a low-burden intervention → lock a prediction → capture delayed outcome with provenance and missingness → challenge/falsify → revise only from evidence`

NOOP already supplies most observation, state, Journal, physiological analytics, and local storage. The architectural gap is *prospective* experiments over existing Journal factors and metric keys; it is **not** a new health score, second Journal, cloud service, generic Protocol Builder, or "digital twin" product.

## A/B/A/B coverage and disposition matrix

Every row should resolve to a stable code path, PR, issue, or explicit external/out-of-scope decision. **A** probes semantic variations (recovery, state, physiology, habits, nutrition, cognition, flow, outcomes); **B** probes exact older concepts and technical symbols; repeat **A** against source code and **B** against issue/PR evidence. A keyword match alone never proves feature parity.

| Historical concept / concrete user intent | Existing NOOP primitive or independent owner | Verified GitHub receipt | Disposition |
|---|---|---|---|
| WHOOP/Goose clean-room self-owned health data, HR/RR/PPG, sleep, recovery, stress, offline storage | [Repository](../README.md), [storage packages](../Packages/WhoopStore), [protocol](../Packages/WhoopProtocol), [scope](SCOPE.md), [attribution](../ATTRIBUTION.md) | BASE; fork PR [#36](https://github.com/kvnloo/noop/pull/36), legacy stack in [#73](https://github.com/kvnloo/noop/issues/73) | Reuse, don't rewrite Goose or include proprietary code |
| Personal State Estimator; Charge/Readiness/Stress and time-of-day baselines | [metric catalog](../Strand/Data/MetricCatalog.swift), [Intelligence Engine](../Strand/Data/IntelligenceEngine.swift), existing sleep/analytics | BASE; [#80](https://github.com/kvnloo/noop/issues/80) | Model *components*, not an opaque new score |
| Health OS, Health Graph, Data Vault and local health records | [Repository.swift](../Strand/Data/Repository.swift), [local store](../Packages/WhoopStore), [data model](DATA_MODEL.md), [Compare](../Strand/Screens/CompareView.swift) | BASE, existing app infrastructure | Enhance source-aware views instead of second database |
| Read-only local query/API / AI-readable health data | [NoopLocalAccess](../Packages/NoopLocalAccess), [local access work](https://github.com/kvnloo/noop/issues/73) | BASE package + historically stacked fork [PRs #2–8](https://github.com/kvnloo/noop/pull/8), [#34](https://github.com/kvnloo/noop/pull/34) | Audit stack for staleness; no parallel server |
| Healthspan/Fitness Age/Vitality/Pace-of-Aging | [analytics](../Packages/StrandAnalytics), [HealthView](../Strand/Screens/HealthView.swift), origin [#2071](https://github.com/ryanbr/noop/issues/2071) | BASE components / upstream proposal | Enhance existing rather than new "Limitless age" |
| Journal v2, protocol/exposure and environmental factors | [JournalCatalog](../Strand/Data/JournalCatalog.swift), [JournalLogCard](../Strand/Screens/JournalLogCard.swift), upstream [#759](https://github.com/ryanbr/noop/issues/759) | BASE + [#70](https://github.com/kvnloo/noop/issues/70) | Canonical factors/aliases, explicit user input |
| Atomic Habits: context cue → two-minute action → explicit check-in → fallback → weekly review | Existing Journal/Plan/Insights; Android [#54](https://github.com/kvnloo/noop/pull/54), Apple [#63](https://github.com/kvnloo/noop/pull/63) | FORK PR, [#70](https://github.com/kvnloo/noop/issues/70) | Experimental; no inferred habit completion |
| Never import yesterday's Yes/No as today's evidence; unknown ≠ no; native/import conflict and Unicode dedup | [#53](https://github.com/kvnloo/noop/pull/53), [#58](https://github.com/kvnloo/noop/pull/58), [#59](https://github.com/kvnloo/noop/pull/59), [#60](https://github.com/kvnloo/noop/pull/60) | FORK PR; QA receipts [#70](https://github.com/kvnloo/noop/issues/70) | Resolve overlapping alternatives before promotion |
| Pre-sleep HR → next-morning recovery and wind-down with coverage | Existing [Breathe](../Strand/Screens/BreathingView.swift), origin [#1760](https://github.com/ryanbr/noop/issues/1760), fork [#51](https://github.com/kvnloo/noop/pull/51) | BASE / FORK PR, [#72](https://github.com/kvnloo/noop/issues/72) | No second Sleep Planner or causal claims |
| Beat-synced meditation over live HR/IBI, sparse-source fallback | Existing Breathe, concurrent [#81](https://github.com/kvnloo/noop/pull/81) | FORK PR (separate active code owner) | Never equate noisy optical timing with diagnosis; no assumed merge/device qualification |
| Training Facility: Entry Hall, Habit Forge, Learning Lab, Flow Chamber, Enhancement Arsenal, Integration Center; later Today, Plan, Food, Movement, Diagnostics, Health Graph, Data Vault navigation | Current NOOP screens (where applicable); [#88](https://github.com/kvnloo/noop/issues/88) | ISSUE ONLY for unbuilt cross-surface navigation | Progressive disclosure; do not claim every historical screen shipped |
| Protocol Builder, Experiment Lab, Prediction Registry, Personal Evidence Engine | Journal + metrics + [LabBookView](../Strand/Screens/LabBookView.swift), proposed [#76](https://github.com/kvnloo/noop/issues/76) | HOLD fork [#42](https://github.com/kvnloo/noop/pull/42); ISSUE ONLY for accepted semantics | One thin experiment reference contract, not second intervention store |
| Timestamped experiment receipts and distinct lagged outcomes | Proposed [#77](https://github.com/kvnloo/noop/issues/77) | HOLD [#43](https://github.com/kvnloo/noop/pull/43), [#44](https://github.com/kvnloo/noop/pull/44) | No receipt may pretend a prediction was prospectively locked without durable proof |
| Two pilot experiments: late caffeine→sleep, screen time→sleep; synthetic Journal fixtures | [#46](https://github.com/kvnloo/noop/pull/46), [#49](https://github.com/kvnloo/noop/pull/49), [#57](https://github.com/kvnloo/noop/pull/57) | FORK PR, [#70](https://github.com/kvnloo/noop/issues/70) | Synthetic proof ≠ real-world effect |
| Flowstate Trainer, Flow Chamber, distraction shield, Loop Habit × ActivityWatch | Native Journal/Breathe + external [Loop](https://github.com/kvnloo/uhabits), [ActivityWatch](https://github.com/kvnloo/activitywatch) | [#85](https://github.com/kvnloo/noop/issues/85) ISSUE ONLY | EXTERNAL A/B research; no attention score inferred from focus duration |
| DTE/Limitless Benchmark: intrinsic vs AI-assisted capability, delayed far-transfer | Independent learning/eval tools (e.g. [Anki](https://github.com/kvnloo/anki), [z0evals](https://github.com/kvnloo/z0evals)) | [#78](https://github.com/kvnloo/noop/issues/78) ISSUE ONLY | EXTERNAL; only consented scalar outcomes might later cross boundary |
| Directed Plasticity, Cognitive Annealing, Plasticity Compiler, Temporal Multiplexing, LUCID | Learning protocol and state/task research, NOT NOOP cognitive-training app | [#79](https://github.com/kvnloo/noop/issues/79) ISSUE ONLY | EXTERNAL experiments; negative results count |
| Personal response model (not "digital twin"); safe adaptive experiments | Proposed state/outcome research [#80](https://github.com/kvnloo/noop/issues/80) | ISSUE ONLY | No automatic intervention or accuracy claim before prospective calibration |
| Epistemic firewall, evidence ranking, Skeptic/Replication/Hypothesis agents | Proposed claims/evidence graph [#82](https://github.com/kvnloo/noop/issues/82) | ISSUE ONLY | External verification and explicit uncertainty; never invent science |
| AI exocortex / Coach; actions gated by user consent | [CoachView](../Strand/Screens/CoachView.swift), [scope](SCOPE.md), local access [#73](https://github.com/kvnloo/noop/issues/73) | BASE + [#83](https://github.com/kvnloo/noop/issues/83) ISSUE ONLY | Fact-first, consent-bounded, no hosted AI by default |
| Evidence-ranked affordable intervention choices and N-of-1 recipes | Existing Journal + Breathe, proposed [#84](https://github.com/kvnloo/noop/issues/84) | ISSUE ONLY | Annotation/evidence backlog, not a treatment recommender |
| VR, EEG/Muse, strobe, eye tracking, neurofeedback, VR/flow protocols | External [FlowState](https://github.com/kvnloo/FlowState) and research tools | [#86](https://github.com/kvnloo/noop/issues/86) ISSUE ONLY | EXTERNAL; no clinically acting stimulation |
| ATLAS (nutrition → indoor garden → cooking robotics → biometric outcome), Blueprint Garden | [Blueprint](https://github.com/kvnloo/blueprint) / independent robotics and nutrition tools; NOOP optional context source | [#87](https://github.com/kvnloo/noop/issues/87) ISSUE ONLY | EXTERNAL; do not move autonomous farming or robot control into NOOP |
| Oura Gen 3 live gestures → opt-in z0live/Lawnchair | [OuraProtocol](../Packages/OuraProtocol), fork [#65](https://github.com/kvnloo/noop/pull/65), [#66](https://github.com/kvnloo/noop/issues/66)–[#68](https://github.com/kvnloo/noop/issues/68) | FORK PR and release/CI receipts | Downstream only; physical BLE and owner-consent proof required |
| Health Connect provenance, auto-sync correctness, BT-on prompt, WHOOP clock | [#52](https://github.com/kvnloo/noop/pull/52), [#55](https://github.com/kvnloo/noop/pull/55), [#62](https://github.com/kvnloo/noop/pull/62), [#64](https://github.com/kvnloo/noop/pull/64) | FORK PR, [#69](https://github.com/kvnloo/noop/issues/69) | Device and per-head test gates; no inference from CI alone |
| Cross-platform language parity and iOS installation guidance | [#45](https://github.com/kvnloo/noop/pull/45), [#63](https://github.com/kvnloo/noop/pull/63) | FORK PR | Respect Swift/Kotlin byte-level parity/CI and source install |
| N-of-1 trial ontology, simulation, safety and replication | [#89](https://github.com/kvnloo/noop/issues/89) | ISSUE ONLY | Must predeclare endpoints and privacy boundaries |

## Technical contracts recovered beyond names

### 1. Prospective, missingness-safe evidence

The minimal experiment [#76](https://github.com/kvnloo/noop/issues/76) references *existing* canonical factor IDs and metric keys rather than assigning ambiguous aliases new clinical meaning. Proposals include `experiment_id`, `hypothesis`, `baseline/exposure/outcome windows`, `primary_outcome_metric_key`, `lag`, `predicted_direction`, `minimum_coverage/samples`, `predeclared falsification`, `created_at`, `prediction_locked_at`, `status`, `analysis_recipe_version` and source/consent/export metadata.

- Explicit **Yes**, explicit **No**, **Skipped** and **unanswered** remain separate. No absent Journal day becomes a control day. No missing biometrics becomes a negative intervention.
- Analyze per local wake-day/session with UTC timestamps, DST/timezone rules, clock skew, multi-day treatments and lagged windows. Imported and native sources retain identities and precedence.
- A completed analysis [#77](https://github.com/kvnloo/noop/issues/77) can return **supports / contradicts / inconclusive** with sample size, coverage, uncertainty, source lineage, confounders and recipe version. These are associations unless causal design supports a stronger claim.
- [Drafts #42–44](https://github.com/kvnloo/noop/pull/42) remain **HOLD**, including the two-real-use-case gate; a pure data type is not proof of durable preregistration.
- Example candidate N-of-1 pilots: caffeine cutoff vs sleep; bedtime screen reduction vs sleep; wind-down breathing vs pre-sleep HR/next morning; meal/training timing vs recovery; structured learning window vs delayed test. Record failures and inconclusive outcomes.

### 2. Cognitive state and personal response (research, not current NOOP model)

An August Blueprint/zer0 conversation proposed a high-level state vector `x_t = [C,S,M,N,E,H]` with dynamics `P(x_(t+1) | x_t, a_t)` and a multiobjective `J = w_L L + w_M M + w_A A + w_E E − λ R − λ T`. This historical *candidate formulation* has **not** been validated as NOOP mathematics. See [#80](https://github.com/kvnloo/noop/issues/80) for the later, narrower model.

For a testable observation use `E[Y_(t+k) | S_t, I_t, C_t]`: measured baseline state, user-confirmed intervention, explicit confounders and a prespecified outcome at lag `k`, with uncertainty. Prediction is not intervention effect: counterfactual claims need a stronger experimental design. Test against a simple baseline (sleep + clock time + previous task performance), protect holdouts and report calibration, leakage, burden and risk.

### 2a. Capability decomposition, epistemic efficiency, and inference (external research)

The older *Limitless / zer0* research document used the **conceptual**, not literally multiplicative, expression: `Effective Capability = Biology × Learning × State Control × AI Leverage × Consolidation × Epistemic Accuracy`. It distinguished a short repeatable **state battery** (sleepiness, vigilance, arousal) from a **capability battery** (novel tasks, delayed retention, unseen far transfer) and distinguished `P_augmented` from `P_human-only`. Those quantities should not be collapsed into a NOOP user score; [#78](https://github.com/kvnloo/noop/issues/78) owns independent benchmark validation.

For proposed **epistemic efficiency**, the same research artifact operationalized `ETCE = novel insights surviving independent validation / high-confidence insights generated` and a companion `false-confidence rate = P(incorrect | high_confidence)`. The expansion of the acronym ETCE was **not recovered**, and this ratio remains a **research hypothesis, not a NOOP metric**. Confidence is user-rated per insight; correctness/usefulness/predictive validity needs blinded independent scoring. An intervention that boosts the volume of plausible insights while raising confident error should fail its epistemic gate. See [#89](https://github.com/kvnloo/noop/issues/89) and [#82](https://github.com/kvnloo/noop/issues/82).

A candidate future state-space *research* model was `z_(i,t+1) = f_(θ_i)(z_(i,t),u_(i,t),c_(i,t)) + ε_(i,t)`, `y_(i,t) = g_(φ_i)(z_(i,t)) + η_(i,t)`, with hierarchical priors and posterior uncertainty. A suggested *selection policy* balanced `E(ΔDTE | a,x_t) + β·information_gain − λ·CVaR(risk) − γ·burden` over **user-consented low-risk choices**. None of this has met NOOP's prospective-data gate; it belongs to downstream research [#80](https://github.com/kvnloo/noop/issues/80), not to a default-on optimizer.

### 3. Cognitive Annealing / Temporal Multiplexing (external experimental protocol)

Recovered stages: `EXPLORE → SELECT → ENCODE → CONSOLIDATE → VALIDATE → EXECUTE → RECOVER`, preceded by state observation and task selection. [#79](https://github.com/kvnloo/noop/issues/79) defines controlled factorial/repeated-measures comparisons, order/circadian blocking, safe noninvasive interventions, explicit phase transitions, delayed unaided transfer, null results and sleep-preserving stop rules. Do not treat "neural entropy", vendor attention readings or momentary arousal as validated learning outcomes.

### 4. Durable learning benchmark (external)

[#78](https://github.com/kvnloo/noop/issues/78): novel held-out microdomains; immediate acquisition; 24h, 7d and optional 30d unassisted tests; near/far transfer; assisted versus AI-withdrawn comparison; controlled parallel forms, test-retest reliability, practice effect and attrition. A proposed research endpoint is `DTE_7d = (θ_7d_far_transfer − θ_baseline) / learning_minutes`. Item-response candidate `P(X_ij=1)=sigmoid(a_j(θ_i−b_j))` belongs to the benchmark's psychometrics, **not** a NOOP health score. Share only purpose-limited aggregate/scalar outcomes if [#77](https://github.com/kvnloo/noop/issues/77) later passes its gate.

### 5. Epistemic firewall and agent permission model

The recurring principle is `measured observation > derived metric > retrospective association > preregistered prediction > repeated personal effect > independently replicated evidence` in *evidential strength*, not in automated action authority. [#82](https://github.com/kvnloo/noop/issues/82) owns claim provenance, skeptical critiques, independent reproduction and failure-to-replicate receipts. [#83](https://github.com/kvnloo/noop/issues/83) owns a bounded UI/agent contract: explain data; suggest with evidence and uncertainty; prepare possible action; commit only with explicit user approval. Any external model must never silently turn observation into diagnosis or consent into background export.

## Ownership and safety boundaries

- **NOOP** owns user-controlled sensors/imports, on-device SQLite, transparent physiology, Journal, Breathe, Insights, source provenance and opt-in local access. [AGENTS](../AGENTS.md), [SCOPE](SCOPE.md) and [privacy](PRIVACY_SECURITY.md) are controlling. Keep offline-first; no new hosted server/account/telemetry; one narrow *default-off user-owned one-way export* exception. No medical-device claims or silent background interventions.
- **Anki/FSRS / Learning Lab** own decks, learning scheduler, retrieval outcomes and cognitive benchmarking. **ActivityWatch/Loop** own attentional proxy/instrumentation and manual habit adherence; **Blueprint/ATLAS** own growing/food/robotics; **z0intelligence/z0evals** own speculative model inference and protected evaluation. Only a consented, minimal timestamped observation *might* cross into NOOP after #77 proves two independent use cases.
- **Clean-room provenance:** credit [johnmiddleton12/my-whoop](https://github.com/johnmiddleton12/my-whoop), [b-nnett/goose](https://github.com/b-nnett/goose) and other humans exactly as [ATTRIBUTION](../ATTRIBUTION.md) and [NOTICE](../NOTICE) require. Reimplement protocol facts; never copy unlicensed proprietary code/assets. Keep NOOP's [PolyForm Noncommercial](../LICENSE) separate from different licenses in Blueprint or external tools.
- **Excluded from this product:** clinical advice/alerts, neurostimulation control, pharmacologic recommendation engine, single NZT/Limitless capability score, unverified "digital twin", EEG/VR suite, robotic farm controllers, an autonomous causal-optimizer agent, a second Journal, a second cloud/data vault, or unaudited bidirectional sync.

## Recovered chronology (retrieval quality noted)

| Period | Recovered theme | Reliability |
|---|---|---|
| 2023-07 | Flowstate/Muse EEG explorations | Summary/index only; original full chat not inspected |
| 2025-10–11 | Performance digital twin, ALIE-like VR/EEG, sleep/HRV/learning loop | Summaries only |
| 2025-12 | Limitless Protocol and ATLAS closed loop (bio-state → meals → garden → cooking → outcomes) | Summaries only |
| 2026-06 | Health OS/Blueprint UX: Today, Plan, Food, Movement, Diagnostics, Health Graph, Data Vault, Protocol Builder, Experiment Lab | Roadmap/summary; not proof every screen exists |
| 2026-08 | zer0/Blueprint state equations, Cognitive Annealing, directed plasticity, DTE, temporal modes | Summaries; see formalization above |
| 2026-09 | NOOP clean-room/GitHub contribution and local CLI/MCP stack | Repo paths, historical PRs and summaries |
| 2026-10-01 | Archaeology narrowed missing gap to prospective experiment contract | Earlier report explicitly marked chat-history coverage incomplete |
| 2026-10-19/20 | Journal/Atomic Habits, Health Connect, Oura→z0live, source correctness | Fork PRs/issues and QA receipts in #66–74 |
| 2026-10-21 UTC | Broader Limitless research backfill | Verified issue numbers #76–80, #82–89 |

## Gate/receipt checklist

- [x] Canonical fork and source/scope checked: `kvnloo/noop`; repository has writable branch/PR workflow.
- [x] Older names normalized to existing functionality, a fork PR, an issue, an external owner or an explicit exclusion; no parallel issue per synonym.
- [x] P0 experiment, lagged outcomes, learning benchmark, cognitive annealing, response model, epistemic firewall, Coach, recipes, flow, EEG, ATLAS, Today navigation and research methods have GitHub issue homes (#76–80, #82–90).
- [x] Existing ongoing implementation has a **single** promotion queue [#74](https://github.com/kvnloo/noop/issues/74); no mass upstream proposals or false merged status.
- [ ] Obtain full chat export / line-by-line conversation index for true **every-chat** coverage; check missing original art, symbols, chronologies and contradictory decisions. Until then, coverage = **partial / best available**, not 100%.
- [ ] Prove two distinct real user experiments beyond existing native Journal/metrics before admitting #42–44, #76–77.
- [ ] Resolve/qualify overlapping Journal PRs #53/#58/#59/#60 and app hardware tests; exact-head CI and device claims must be logged in their owner issues, not inferred from this document.
- [ ] Add validated off-app benchmark/flow data only after privacy review, falsification, consent and reproducible fixtures; do not introduce ungrounded personal-treatment claims.
- [ ] Reconcile future issues and PRs using **A semantic conversation search → B detailed/symbolic retrieval → A code-path and duplicate inspection → B remote issue/commit verification**. Repeat until every *accessible* requirement has a receipt or an explicit scope decision; preserve unresolved archive-coverage limitation.

**Source of truth for history/ABAB coverage is [#90](https://github.com/kvnloo/noop/issues/90); source of truth for promotion remains [#74](https://github.com/kvnloo/noop/issues/74), not this document.** This index is a fork-local architectural/provenance reference; it does not justify merging held experiment code or advertising a research claim as shipped functionality.
