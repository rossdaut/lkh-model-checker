# Interactive application

Besides the benchmark CLIs, the project ships an interactive, menu-driven application
(`lkh.App`) for loading a single model and checking `L_Kh` expressions against it by hand.
It is the convenient way to explore one problem without writing any code.

## Launching

Build the jar (`mvn package -q`) and run, from the repository root:

```bash
java -cp target/lkhmc-1.0.jar lkh.App
```

The interface is a text menu; you navigate by typing the number next to each option and
pressing Enter. Option `0` always goes back / exits the current menu.

## Overall flow

The application has two phases:

1. **Load a model** into the session (from PDDL, from a DOT file, or from a randomly
   generated LTS).
2. **Operate on the loaded model**: check expressions, check the problem goal, simulate
   execution, change the checking algorithm, or export the LTS.

The top of the screen always shows a summary of the current session (source, checker,
LTS size, and the POR strategy when applicable).

## Loading a model

From the main menu:

- **Cargar LTS desde PDDL** — build the LTS from a planning problem.
  - *Manual*: you are prompted for the domain and problem file paths.
  - *Ejemplos PDDL incluidos*: pick one of the bundled examples (Tire, Logistics).
  - After choosing the source you select a **Partial Order Reduction** strategy
    (`Ninguno`, `Stratified`, or `Strong Stubborn Sets`). The LTS is then built and its
    size reported.

- **Cargar LTS desde DOT** — load a model that already exists as a graph.
  - *Archivo .dot*: read an LTS from a `.dot` file.
  - *Generar desde configuracion*: generate a random LTS from a configuration file
    (the same generator used by `RandomBenchmarkCli`), useful for experimenting with
    initial-state uncertainty.

Once a model is loaded the application switches to the **session menu**.

## Operating on the model

The session menu offers:

- **Chequear goal del problema** (PDDL only) — checks `Kh(initial, goal)` for the
  problem's own initial condition and goal, i.e. whether the planning goal is reachable
  in the `Knowing-How` sense. If it holds, you can page through witness plans.

- **Chequear expresion** — type any `L_Kh` expression (for example
  `kh(p0 & p1, p2)`); the application parses it, evaluates it against the model, and
  reports whether it holds. For a top-level `Kh` expression that holds, you can inspect
  witness plans interactively.

- **Cambiar checker** — switch the model-checking algorithm:
  - *Direct* — the direct `A^kh` construction (this project's algorithm).
  - *Classic (Fervari)* — the classic construction, with or without minimization.

- **Exportar LTS a .dot** — write the current LTS to a `.dot` file for visualization or
  reuse.

- **Simular** — step through the LTS by hand: from the current state, pick one of the
  available actions and move to a successor. Non-deterministic actions prompt you to
  choose the target state. Goal states are flagged. This is helpful for understanding the
  structure of a model or debugging an unexpected checking result.

- **Limpiar LTS y volver al menu principal** — drop the loaded model and start over.

## Relationship to the benchmarks

The application and the benchmark CLIs share the same core: the PDDL-to-LTS builder, the
POR strategies, and both model-checking algorithms. The difference is purpose — the app
is for interactive, single-model exploration with human-readable output, while the
benchmark CLIs (see [benchmarks.md](benchmarks.md)) run many cases unattended and emit
machine-readable CSVs for the thesis tables.
