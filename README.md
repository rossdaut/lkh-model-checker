# LKH Model Checker

A model checker for the *Knowing-How* logic (`L_Kh`). It takes a planning problem (in
PDDL) or a labelled transition system (LTS), builds a model, and verifies `Kh(φ, ψ)`
expressions over it. It also implements two LTS-reduction strategies (Partial Order
Reduction) and two model-checking algorithms (Classic and Direct), which are compared
experimentally in the accompanying thesis.

## Requirements

- **Java 17** (the project targets `maven.compiler.release = 17`).
- **Maven**.
- **PDDL4J 4.0.0**, installed in your local Maven repository (see below). At the time of
  writing it is not available on Maven Central.

### Installing PDDL4J locally

1. Download the `pddl4j-4.0.0.jar` from https://github.com/pellierd/pddl4j.
2. Install it into your local Maven repository:

   ```bash
   mvn install:install-file \
     -Dfile=lib/pddl4j-4.0.0.jar \
     -DgroupId=fr.uga.pddl4j \
     -DartifactId=pddl4j \
     -Dversion=4.0.0 \
     -Dpackaging=jar
   ```

## Building

```bash
mvn package -q
```

This produces a shaded jar with all dependencies bundled: `target/lkhmc-1.0.jar`.
All commands below should be run **from the repository root**, since some paths
(PDDL benchmark files) are resolved relative to it.

## Two ways to use it

### 1. Interactive application

A text-menu application to load a single model and check expressions against it by hand:

```bash
java -cp target/lkhmc-1.0.jar lkh.App
```

You can load a model from PDDL, from a `.dot` file, or from a randomly generated LTS, and
then check `L_Kh` expressions, inspect witness plans, simulate execution, or export the
LTS. See **[docs/app.md](docs/app.md)** for the full flow.

### 2. Benchmarks

Command-line tools that run the experiments behind the thesis result tables and print
results to the console:

- `lkh.cli.PddlBenchmarkCli` — effect of Partial Order Reduction over IPC instances.
- `lkh.cli.RandomBenchmarkCli` — Classic vs. Direct model-checking algorithms over
  randomly generated LTSs.

For example, to spot-check one row of the POR table:

```bash
java -cp target/lkhmc-1.0.jar lkh.cli.PddlBenchmarkCli \
    --case "gripper / instance-1" --strategy all --skip-check
```

See **[docs/benchmarks.md](docs/benchmarks.md)** for every argument, more examples, and
how the console output maps to the thesis tables.

## Documentation

- [docs/benchmarks.md](docs/benchmarks.md) — running and verifying the benchmark results.
- [docs/app.md](docs/app.md) — the interactive application flow.
