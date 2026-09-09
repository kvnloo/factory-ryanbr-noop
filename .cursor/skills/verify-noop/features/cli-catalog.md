# Feature: CLI Catalog

## Sub-features

1. **Version reporting**: `--version` and `-V` flags print the product version
2. **List tools**: `query --list-tools` and `tools` command both return JSON array of tool names
3. **Tool count**: The array contains exactly 17 tools (as documented in AGENTS.md)

## How to get to it

User-facing commands:

```sh
noop-local-access --version
noop-local-access -V
noop-local-access query --list-tools
noop-local-access query --list-tools --pretty
noop-local-access tools
```

## Driving it with noop-verify

The `noop-verify` harness provides:

```sh
# Integrated into doctor
noop-verify doctor

# Direct invocation
noop-verify version
noop-verify list-tools
```

Expected behavior:

- Version output is non-empty single line, exit 0
- List-tools output is valid JSON array, exit 0
- Tools command output is valid JSON array, exit 0
- Array contains these 17 tool names: `health_snapshot`, `metric_series`, `data_freshness`, `sleep_summary`, `workout_summary`, `hr_series`, `rr_series`, `event_series`, `event_kinds`, `sleep_stages`, `spo2_series`, `skin_temp_series`, `resp_series`, `step_series`, `gravity_series`, `battery_series`, `sleep_state_series`

## Gotchas

1. **Version is runtime-defined**: The version string comes from `noopLocalAccessServerVersion` in `NoopLocalAccessCore`, not from `Package.swift` (which has no version field per SPM).

2. **Two paths to tool list**: Both `query --list-tools` and `tools` return the same array, but `tools` rejects extra arguments while `query --list-tools` is part of the query parser.

3. **No database required**: These commands work without `--db-path` or `NOOP_DB_PATH`. Do not waste cycles creating a database for catalog tests.

4. **Tool count is a contract**: If the array length is not 17, something is wrong. The dispatcher's `toolNames` array in `ToolDispatcher.swift` is the canonical source.

5. **Pretty flag works**: `query --list-tools --pretty` indents the output, but the parse test only needs to confirm valid JSON, not formatting.
