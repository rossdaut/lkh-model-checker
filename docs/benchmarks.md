# Benchmarks: running and checking results

This document explains how to reproduce and spot-check the experimental results of
the thesis using the command-line tools. There are two independent benchmark suites,
each backing one of the result tables:

| CLI | What it measures | Thesis table |
|-----|------------------|--------------|
| `lkh.cli.PddlBenchmarkCli`   | Effect of Partial Order Reduction on LTS construction, over IPC benchmarks | *Partial Order Reduction* table (Results chapter) |
| `lkh.cli.RandomBenchmarkCli` | Classic vs. Direct model-checking algorithms, over randomly generated LTSs | *checker comparison* table (Results chapter) |

A third CLI, `lkh.cli.PddlCaseRunnerCli`, is the single-case worker that
`PddlBenchmarkCli` spawns internally; it is documented at the end since it can also be
run on its own.

---

## Prerequisites

1. Build the shaded jar (includes all dependencies):

   ```bash
   mvn package -q
   ```

   This produces `target/lkhmc-1.0.jar`.

2. **Run every command from the repository root** (`lkh-model-checker/`). The PDDL
   benchmark paths are relative to that directory, so running from elsewhere will fail
   to find the domain/problem files.

All commands below assume that working directory and use
`java -cp target/lkhmc-1.0.jar <class> ...`.

---

## 1. Partial Order Reduction — `PddlBenchmarkCli`

Runs the model checker over a fixed list of 36 IPC instances using one (or all) of the
three LTS-construction strategies: `none` (no reduction), `stratified`
(Stratified Planning) and `sss` (Strong Stubborn Sets). Each case is run as an isolated
subprocess with a 60-second timeout.

For the thesis table the model-checking phase is skipped (`--skip-check`) so that the
measurement isolates the cost of building the LTS.

### Arguments

| Argument | Default | Description |
|----------|---------|-------------|
| `<output.csv>` (positional) | `src/main/resources/pddl-examples/sss-direct.csv` | Where to write the CSV. Only written when a single strategy is selected. |
| `--strategy none\|stratified\|sss\|all` | `sss` | Reduction strategy. `all` runs the three in sequence (console only, no CSV). |
| `--checker direct\|classic` | `direct` | Model-checking algorithm. Irrelevant together with `--skip-check`. |
| `--skip-check` | off | Build the LTS only, skip verification. Used for the POR table. |
| `--case <substring>` | (none) | Run only the cases whose name contains this substring (case-insensitive). |

Every case prints a summary line to the console regardless of whether a CSV is written.

### Spot-checking a single row of the table

To verify one row, run all three strategies for that case and read the LTS sizes off the
console. For example, `gripper / instance-1`:

```bash
java -cp target/lkhmc-1.0.jar lkh.cli.PddlBenchmarkCli \
    --case "gripper / instance-1" --strategy all --skip-check
```

Expected output (the `LTS=` column is `states / transitions`):

```
1 case(s) match "gripper / instance-1"
Running all strategies (console only, no CSV)
[1/1] ipc-1998 gripper / instance-1 | strategy=none       | ... | LTS=2657 / 62580 | ... | status=OK
[1/1] ipc-1998 gripper / instance-1 | strategy=stratified | ... | LTS=2657 / 60194 | ... | status=OK
[1/1] ipc-1998 gripper / instance-1 | strategy=sss        | ... | LTS=242 / 2805   | ... | status=OK
```

These three numbers correspond directly to the *None*, *Stratified* and *SSS* columns of
the POR table for that instance.

### Selecting cases

The `--case` filter matches by substring, so the breadth is controlled by how specific
the text is:

| Command fragment | Cases run |
|------------------|-----------|
| `--case gripper` | every gripper instance (the whole family) |
| `--case "ipc-1998 gripper"` | only the ipc-1998 gripper instances |
| `--case "6 balls"` | only `ai-planning gripper / 6 balls` |

The tool prints how many cases matched before running, so an over-broad filter is easy
to notice.

### Reproducing the full table

The table is built from three CSVs, one per strategy:

```bash
java -cp target/lkhmc-1.0.jar lkh.cli.PddlBenchmarkCli none.csv       --strategy none       --skip-check
java -cp target/lkhmc-1.0.jar lkh.cli.PddlBenchmarkCli stratified.csv --strategy stratified --skip-check
java -cp target/lkhmc-1.0.jar lkh.cli.PddlBenchmarkCli sss.csv        --strategy sss        --skip-check
```

The three CSVs are then merged into the LaTeX table by the script
`extras/benchmarks/gen_table.py` in the thesis workspace. Each full run takes a few
minutes; cases that hit the 60-second timeout are reported as `T/O`.

---

## 2. Classic vs. Direct — `RandomBenchmarkCli`

Compares the Classic and Direct algorithms for building the `A^kh` automaton on
**deterministic** LTSs generated randomly with the `generator` module. Working on
synthetic LTSs allows the size, density, number of initial states, witness-plan length
and witness count to be varied independently.

The experiment is organized in five stages, each varying a single parameter relative to
a common baseline (LTS of 200 states, density 3, 3 initial states, 3 witnesses, minimum
plan length 8). Both algorithms run on the *same* generated LTS for each
configuration/seed, and each cell averages 5 seeds.

| Stage | Varied parameter | Values |
|-------|------------------|--------|
| A | LTS size (`min_nodes`) | 100, 200, 400, 800, 1600 |
| B | initial states | 1, 2, 3, 4, 6 |
| C | witness plan length | 3, 5, 8, 12 |
| D | LTS density (edges per state) | 2, 3, 4, 6, 10 |
| E | implanted witnesses | 1, 3, 6 |

### Arguments

| Argument | Default | Description |
|----------|---------|-------------|
| `<output.csv>` (positional) | `extras/benchmarks/random.csv` | Where to write the CSV. |
| `--stage A\|B\|C\|D\|E` | (all stages) | Run only one stage. |
| `--direct-only` | off | Run only the Direct algorithm. |
| `--classic-only` | off | Run only the Classic algorithm. |

Each `(scenario, seed, checker)` run prints a progress line to standard error with the
status, number of generated nodes (`gen=`) and elapsed time.

### Spot-checking one stage

Stage B (initial states) is the one that most directly exercises the hypothesis. To run
just that stage with both algorithms:

```bash
java -cp target/lkhmc-1.0.jar lkh.cli.RandomBenchmarkCli stageB.csv --stage B
```

To get a quick, cheap signal first, run only the Direct algorithm (it finishes in
seconds while Classic can time out):

```bash
java -cp target/lkhmc-1.0.jar lkh.cli.RandomBenchmarkCli stageB.csv --stage B --direct-only
```

Each line reports `gen=<nodes generated>`, which is the *Nodos gen.* column of the
checker comparison table. The Classic algorithm generates orders of magnitude more nodes
than the Direct one, and the gap widens as the parameter grows.

### Reproducing the full table

```bash
java -cp target/lkhmc-1.0.jar lkh.cli.RandomBenchmarkCli random.csv
```

This runs the five stages with both algorithms (a few minutes, with Classic timing out on
the larger configurations). The CSV is then turned into the LaTeX table by
`extras/benchmarks/gen_table_checkers.py` in the thesis workspace. Rows where every seed
timed out are reported with a `>=` prefix indicating the partial node count reached
before the cutoff.

---

## 3. Single-case worker — `PddlCaseRunnerCli`

`PddlBenchmarkCli` spawns this class once per case as a subprocess. It builds the LTS for
one PDDL instance (optionally running the checker), writes partial progress to a snapshot
file, and prints one CSV line to standard output. It can be run on its own to measure a
single instance:

```bash
java -cp target/lkhmc-1.0.jar lkh.cli.PddlCaseRunnerCli \
    "my-case" path/to/domain.pddl path/to/problem.pddl /tmp/snapshot.txt \
    --strategy sss --skip-check
```

### Arguments

| Argument | Default | Description |
|----------|---------|-------------|
| `<name>` (positional) | — | Label used in the CSV output. |
| `<domain>` (positional) | — | Path to the PDDL domain file. |
| `<problem>` (positional) | — | Path to the PDDL problem file. |
| `<snapshot-file>` (positional) | — | File where partial counts are written (so the parent can recover them on timeout). |
| `--strategy none\|stratified\|sss` | `sss` | Reduction strategy. |
| `--checker direct\|classic` | `direct` | Model-checking algorithm. |
| `--skip-check` | off | Build the LTS only. |

The output is a single CSV line; the header is defined by `PddlBenchmarkCli`. The timeout
is 60 seconds, the same as the benchmark.
