# Feature: Heart Rate Series

## Sub-features

1. **Time-bucketed HR data**: Returns average HR per time bucket
2. **Time window modes**: Either `--hours N` (recent) or `--from-ts`/`--to-ts` (absolute)
3. **Bucket size**: `--bucket-seconds` controls aggregation window
4. **Limit and truncation**: `--limit` caps output, `truncated` flag indicates clipping
5. **Device filtering**: `--device-id` selects a specific source

## How to get to it

User-facing commands:

```sh
noop-local-access query hr_series --hours 1 --db-path /path/to/whoop.sqlite
noop-local-access query hr_series --hours 6 --bucket-seconds 300 --limit 100 --db-path /path/to/whoop.sqlite
noop-local-access query hr_series --from-ts 1234567890 --to-ts 1234571490 --db-path /path/to/whoop.sqlite
noop-local-access query hr_series --hours 1 --device-id my-whoop --db-path /path/to/whoop.sqlite
noop-local-access query hr_series --hours 1 --pretty --db-path /path/to/whoop.sqlite
```

## Driving it with noop-verify

Direct CLI invocation (no dedicated harness command yet):

```sh
BIN=Packages/NoopLocalAccess/.build/debug/noop-local-access
DB=/tmp/test.sqlite

# Basic query
$BIN query hr_series --hours 1 --db-path "$DB"

# Custom bucket and limit
$BIN query hr_series --hours 6 --bucket-seconds 300 --limit 50 --db-path "$DB"

# Absolute time range
$BIN query hr_series --from-ts 1234567890 --to-ts 1234571490 --db-path "$DB"
```

Expected behavior:

### Successful query

- Exit code 0
- stdout is valid JSON object with keys:
  - `points`: Array of `{ts, iso, avgBpm}` objects
  - `truncated`: Boolean indicating if results were capped
- Empty database returns `{"points": [], "truncated": false}`

### Missing database

- Exit code 1
- stderr contains `NOOP database is unavailable`

### Usage errors

- Exit code 64 for:
  - Only `--from-ts` without `--to-ts` (or vice versa)
  - Negative hours or bucket-seconds
  - Missing `--db-path` and no `NOOP_DB_PATH` env

### With seeded data

- Points array contains bucketed HR values
- `avgBpm` is the mean of all `hrSample.value` rows in that bucket
- `truncated` is `true` if more buckets exist than `--limit`

## Gotchas

1. **Default bounds**: `--hours 6` is the default lookback, `--bucket-seconds 60` (1 minute) is the default bucket, `--limit 500` is the default cap. These match the MCP tool bounds.

2. **Paired timestamps**: `--from-ts` and `--to-ts` are both-or-neither. The CLI rejects a half-specified range.

3. **Unix timestamps**: The `ts` field is seconds since epoch. The `iso` field is the ISO 8601 string representation (UTC).

4. **Bucketing is server-side**: The CLI does not post-process; the dispatcher computes averages from `hrSample` table rows. Empty buckets do not appear in the output.

5. **Device ID is optional**: Omitting `--device-id` uses the default source (`my-whoop`). Multiple devices require explicit filtering per query.

6. **Truncation is not an error**: `truncated: true` means the tool hit the `--limit` cap. This is expected for long time ranges with small buckets. Adjust `--bucket-seconds` or `--limit` to see more.

7. **No interpolation**: Missing HR samples leave gaps. The tool returns only buckets with data.

8. **Series tools are symmetric**: The same argument structure applies to `spo2_series`, `skin_temp_series`, `resp_series`, `step_series`, `gravity_series`, `battery_series`, `sleep_state_series`, and `rr_series` (though `rr_series` has no `--bucket-seconds`).
