# NoopLocalAccess Feature Map

This directory maps the testable features of `noop-local-access` CLI and MCP.

Each feature file follows this structure:

## Sub-features

What smaller capabilities make up this feature.

## How to get to it

User-facing commands and arguments to reach this feature.

## Driving it with noop-verify

How the `noop-verify` harness exercises this feature.

## Gotchas

Edge cases, error modes, and known traps.

---

## Feature List

- **cli-catalog**: Version, list tools, and tools catalog
- **cli-data-freshness**: Data freshness query with explicit db-path
- **cli-resource-list**: Resource listing and URI resolution
- **cli-usage-errors**: Exit code 64 for malformed commands
- **hr-series**: Heart rate series query with time bounds
