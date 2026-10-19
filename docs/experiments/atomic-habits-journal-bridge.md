# Two-minute habit stacking in the existing Journal (Android pilot)

This fork experiment implements a thin Atomic Habits-style **cue -> two-minute action -> explicit check-in** flow. It reuses NOOP's existing user-owned Journal catalog, native Yes/No rows, behavior effects and Personal Experiment surfaces. No extra habit database, metrics, permissions, cloud service, or score is introduced.

## User path

1. Open **Insights -> Journal -> Two-minute habit**. Enter a concrete trigger ("brushing my teeth") and a small action ("read one page").
2. **Add to Journal** creates a custom **Behaviour** Yes/No question: "After brushing my teeth, did you read one page for two minutes?". It does **not** mark it done.
3. After doing the action, log **Yes**; after consciously skipping, log **No**; otherwise leave it unanswered. Journal Insights can analyze it under their existing coverage/lag constraints.
4. Edit/rename/hide/delete the item using existing Journal controls. The persisted canonical question remains stable across display renames. The standard Journal storage and backup remain authoritative.

## Guardrails

- No identity questionnaire, streak penalties, invented adherence or causal claims.
- The UI does not pre-answer, auto-repeat, or infer completion from biometrics.
- Both parts are required and limited to 80 normalized characters; Unicode whitespace is collapsed; duplicate canonical keys are disabled.
- Android-only fork pilot. macOS/iOS parity, translations and accessibility/device validation need review before upstream promotion.
- Based on pinned `ryanbr/noop@8e4b0c1`; preserve NOOP maintainers' authorship and PolyForm Noncommercial license.
