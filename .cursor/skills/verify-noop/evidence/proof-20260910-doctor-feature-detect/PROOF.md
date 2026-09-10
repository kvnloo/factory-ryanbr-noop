# Verification Proof: staging doctor feature-detect + catalog CLI port

**Run ID**: `proof-20260910-doctor-feature-detect`
**Date**: 2026-09-10 ~00:17 UTC (2026-09-09 ~19:17 CT)
**Platform**: Linux x86_64 (Debian/Ubuntu box)
**Swift**: 6.0.3 (swift-6.0.3-RELEASE)
**SQLite**: amalgamation 3.53.4 with SQLITE_ENABLE_SNAPSHOT=1
**Branch**: `fix/verify-noop-doctor-feature-detect` → `staging`
**Fork**: `kvnloo/factory-ryanbr-noop` only

## Executive Summary

**VERDICT: PASS (doctor exit 0)**

PER-1268 follow-on on staging:

1. Ported cheap CLI catalog surface from local-access stack: `--version` / `-V`, `query --list-tools`, `tools`, and MCP `noop://tools/catalog`.
2. Doctor now **feature-detects** those catalog asserts from `noop-local-access --help`. Missing features → **SKIP + overall exit 2 INCONCLUSIVE**. Never FAIL absence. Never fake exit 0 / PASS when skipped.
3. With the port present, doctor fully exercises catalog asserts and **PASS**.

## Doctor output

```
=== Doctor: NoopLocalAccess CLI ===
Checking swift... Swift version 6.0.3 (swift-6.0.3-RELEASE)
Building... OK
Binary exists... OK
Version output... OK (0.1.0)
List tools... OK (6 tools)

PASS: All doctor checks succeeded
```

**doctor exit code: 0**

## Catalog captures

- `--version` → `0.1.0` (exit 0)
- `query --list-tools` → 6-tool JSON array matching staging `NoopToolDispatcher.toolNames`
- `tools` → same JSON array

Staging tool set (not the 17-tool stack):

`health_snapshot`, `metric_series`, `data_freshness`, `sleep_summary`, `workout_summary`, `hr_series`

## Feature-detect contract

| Help advertises | Assert result | Doctor exit |
|---|---|---|
| `--version` + `--list-tools` and both work | OK | 0 PASS |
| either missing from help | SKIP those asserts | 2 INCONCLUSIVE (not PASS) |
| advertised but broken | FAIL | 1 |

See `feature-detect-skip-probe.txt` for the help-string probe documenting SKIP behavior on trees without catalog CLI.

## Artifacts

- `doctor.txt`
- `commands.txt`
- `version.txt`
- `list-tools.json`
- `tools.json`
- `feature-detect-skip-probe.txt`
- `PROOF.md`

## Notes

- Card PER-1268 stays Done; this is a follow-on PR into staging only.
- Full series/CLI stack from `feat/local-access-query-stack` was **not** merged — only the cheap catalog surface needed for doctor.
