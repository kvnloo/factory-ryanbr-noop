# Feature: CLI Data Freshness

## Sub-features

1. **Explicit database path**: `--db-path` overrides env and default container
2. **Missing database handling**: Exit 1 with stderr `NOOP database is unavailable`
3. **Empty database**: Returns JSON with all `latest*` keys as `null`
4. **Quiet mode**: `--quiet` suppresses stderr diagnostics

## How to get to it

User-facing commands:

```sh
noop-local-access query data_freshness --db-path /path/to/whoop.sqlite
noop-local-access query data_freshness --db-path /path/to/whoop.sqlite --pretty
noop-local-access query data_freshness --db-path /path/to/whoop.sqlite --quiet
noop-local-access resource noop://data/freshness --db-path /path/to/whoop.sqlite
```

## Driving it with noop-verify

The `noop-verify` harness provides:

```sh
noop-verify data-freshness --db-path /tmp/test.sqlite
```

Expected behavior:

### With missing database

- Exit code 1
- stderr contains `NOOP database is unavailable`
- stdout is not valid JSON success

### With empty database

- Exit code 0
- stdout is valid JSON object
- All `latest*` keys exist and are `null`: `latestHeartRateSample`, `latestRrInterval`, `latestEvent`, `latestSleepSession`, `latestWorkout`, `latestBattery`, `latestStep`, `latestResp`, `latestSkinTemp`, `latestSpo2`, `latestGravity`, `latestSleepState`

### With seeded database

- Exit code 0
- Keys with data return `{ts, iso, ageSeconds}` objects
- Keys without data stay `null`

## Gotchas

1. **Database must be readable**: Even an empty sqlite file will open; a missing file or permission denied is the error path.

2. **Freshness keys are nullable**: Do not assume all 12 keys are present with data. A fresh NOOP install has no samples, so all keys are `null`.

3. **Resource URI is equivalent**: `resource noop://data/freshness` and `query data_freshness` hit the same dispatcher method, but `resource` does not support tool-specific flags.

4. **Quiet mode is for automation**: `--quiet` suppresses stderr but does not change exit codes or stdout. Use it when parsing JSON from scripts.

5. **No network fallback**: This is on-device only. Missing DB stays missing; the CLI will not phone home or create a new database.

6. **TemporaryDatabase test fixture**: The test target has a `TemporaryDatabase` helper that seeds minimal data including `hrSample` rows. This is enough to prove non-null `latestHeartRateSample` without a real strap.
