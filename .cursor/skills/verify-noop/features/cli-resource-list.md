# Feature: CLI Resource List

## Sub-features

1. **List known URIs**: `resource --list` returns JSON array of resource URIs
2. **URI dispatch**: `resource <uri>` returns the same JSON as MCP `resourcePayload`
3. **Short form acceptance**: URIs work with or without `noop://` prefix
4. **Unknown URI handling**: Exit 64 for unrecognized URIs

## How to get to it

User-facing commands:

```sh
noop-local-access resource --list
noop-local-access resource --list --pretty
noop-local-access resource noop://tools/catalog
noop-local-access resource tools/catalog
noop-local-access resource noop://data/freshness --db-path /path/to/whoop.sqlite
noop-local-access resource data/freshness --db-path /path/to/whoop.sqlite
noop-local-access resource noop://health/snapshot --db-path /path/to/whoop.sqlite
noop-local-access resource noop://metrics/catalog
noop-local-access resource noop://sources
```

## Driving it with noop-verify

Direct CLI invocation (no dedicated harness command yet):

```sh
BIN=Packages/NoopLocalAccess/.build/debug/noop-local-access
$BIN resource --list
$BIN resource --list --pretty
$BIN resource tools/catalog
$BIN resource noop://tools/catalog
$BIN resource data/freshness --db-path /tmp/test.sqlite
```

Expected behavior:

### List command

- Exit code 0
- stdout is valid JSON array
- Array contains exactly 5 URIs: `noop://tools/catalog`, `noop://data/freshness`, `noop://health/snapshot`, `noop://metrics/catalog`, `noop://sources`

### URI dispatch

- `tools/catalog`: Same JSON array as `query --list-tools`
- `data/freshness`: Same JSON object as `query data_freshness`
- `health/snapshot`: Same JSON as `query health_snapshot --days 14`
- `metrics/catalog`: JSON object with metric definitions
- `sources`: JSON array of device source identifiers

### Unknown URI

- Exit code 64
- stderr contains error message (unless `--quiet`)

## Gotchas

1. **Short forms are a convenience**: The CLI accepts `tools/catalog` and `noop://tools/catalog` equally. The MCP protocol always uses the full `noop://` prefix.

2. **Resource list is hardcoded**: The 5 URIs are the complete set. Do not expect dynamic discovery from the database.

3. **Database-dependent resources**: `data/freshness` and `health/snapshot` require `--db-path`; `tools/catalog`, `metrics/catalog`, and `sources` do not.

4. **No tool arguments on resources**: `resource health/snapshot` always uses default `--days 14`. Use `query health_snapshot --days N` for custom windows.

5. **Exit 64 is usage error**: Unknown URI is treated as a usage mistake, not a runtime error (which would be exit 1).

6. **Pretty flag applies**: `resource --list --pretty` indents the JSON array, matching `query --list-tools --pretty`.
