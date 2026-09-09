# Feature: CLI Usage Errors

## Sub-features

1. **Unknown command**: Exit 64 with usage help
2. **Malformed flags**: Exit 64 for invalid arguments
3. **Missing required arguments**: Exit 64 for tools that need parameters
4. **Help text**: `--help`, `-h`, and `help` command print usage

## How to get to it

User-facing commands that should error:

```sh
noop-local-access unknown-command
noop-local-access query
noop-local-access query metric_series
noop-local-access query metric_series --key
noop-local-access query event_series
noop-local-access resource unknown://bad/uri
noop-local-access tools extra-argument
```

User-facing commands that print help:

```sh
noop-local-access --help
noop-local-access -h
noop-local-access help
noop-local-access query --help
```

## Driving it with noop-verify

Direct CLI invocation (no dedicated harness command yet):

```sh
BIN=Packages/NoopLocalAccess/.build/debug/noop-local-access

# Should exit 64
$BIN unknown-command; echo "Exit: $?"
$BIN query metric_series; echo "Exit: $?"
$BIN resource bad://uri; echo "Exit: $?"

# Should exit 0 with help text
$BIN --help
$BIN query --help
```

Expected behavior:

### Unknown command

- Exit code 64
- stderr contains `Unknown command: <name>`
- stderr includes usage help text

### Missing required arguments

- Exit code 64
- stderr contains specific error (e.g. `metric_series requires key`)
- Examples:
  - `query metric_series` without `--key`
  - `query event_series` without `--kind`
  - `query hr_series --from-ts X` without `--to-ts` (both required)

### Malformed flags

- Exit code 64
- stderr contains parse error
- Examples:
  - `query --unknown-flag`
  - `tools extra-arg`

### Help invocation

- Exit code 0
- stdout contains usage text with all commands and tools
- Help text includes flag descriptions and examples

## Gotchas

1. **Exit 64 is UNIX convention**: Mirrors `EX_USAGE` from `sysexits.h`. This is distinct from exit 1 (runtime error) and exit 2 (INCONCLUSIVE for this skill).

2. **Help does not error**: `--help` exits 0, even though it does not perform a real query. This is standard CLI behavior.

3. **Quiet mode still errors**: `--quiet` suppresses diagnostics on stderr, but exit 64 is unchanged. Error exit codes are not "output".

4. **Paired arguments**: Some tools require both members of a pair: `--from-ts` and `--to-ts`, `--from-day` and `--to-day`. Passing only one is a usage error.

5. **Tools vs query commands**: The `tools` command rejects extra arguments, but `query --list-tools` does not (it is part of the query parser, which ignores unrecognized flags after parsing).

6. **Resource URI validation**: Unknown resource URIs exit 64 (usage error), but database errors (missing file, permission denied) exit 1 (runtime error).
