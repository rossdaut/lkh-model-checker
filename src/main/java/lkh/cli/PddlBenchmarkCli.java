package lkh.cli;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lkh.cli.PddlCaseRunnerCli.CheckerMode;
import lkh.cli.PddlCaseRunnerCli.Options;
import lkh.cli.PddlCaseRunnerCli.Result;
import lkh.cli.PddlCaseRunnerCli.StrategyMode;

public class PddlBenchmarkCli {
  private static final Path DEFAULT_OUTPUT = Path.of("src/main/resources/pddl-examples/sss-direct.csv");

  private static final List<Case> CASES_FINAL = List.of(
      // --- tire (lts_none=5) ---
      new Case("tire / tire-problem",
          "src/main/resources/pddl-examples/ia-modern-aproach/tire/domain.pddl",
          "src/main/resources/pddl-examples/ia-modern-aproach/tire/problem.pddl"),
      // --- ipc-2006 storage (lts_none=7) ---
      new Case("ipc-2006 storage / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-2006/domains/storage-propositional/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2006/domains/storage-propositional/instances/instance-1.pddl"),
      // --- ipc-2006 tpp (lts_none=8) ---
      new Case("ipc-2006 tpp / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-2006/domains/tpp-propositional-strips/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2006/domains/tpp-propositional-strips/instances/instance-1.pddl"),
      // --- ipc-2004 psr-small (lts_none=24) ---
      new Case("ipc-2004 psr-small / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-2004/domains/psr-small-strips/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2004/domains/psr-small-strips/instances/instance-1.pddl"),
      // --- rsc (lts_none=42) ---
      new Case("rsc / easy",
          "src/main/resources/pddl-examples/ia-modern-aproach/rsc/domain.pddl",
          "src/main/resources/pddl-examples/ia-modern-aproach/rsc/problem-easy.pddl"),
      // --- ipc-2008 parc-printer (lts_none=42, 3245) ---
      new Case("ipc-2008 parc-printer / instance-1 (no-costs)",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-1.pddl"),
      new Case("ipc-2008 parc-printer / instance-2 (no-costs)",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-2.pddl"),
      // --- ipc-2000 blocks (lts_none=125, 65990) ---
      new Case("ipc-2000 blocks / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-2000/domains/blocks-strips-typed/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-1.pddl"),
      new Case("ipc-2000 blocks / instance-10",
          "src/main/resources/pddl-examples/potassco/ipc-2000/domains/blocks-strips-typed/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-10.pddl"),
      // --- ipc-1998 movie (lts_none=128) ---
      new Case("ipc-1998 movie / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-1998/domains/movie-strips/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-1998/domains/movie-strips/instances/instance-1.pddl"),
      // --- logistics (lts_none=216) ---
      new Case("logistics / local-problem",
          "src/main/resources/pddl-examples/ia-modern-aproach/logistics/domain.pddl",
          "src/main/resources/pddl-examples/ia-modern-aproach/logistics/problem.pddl"),
      // --- ipc-2002 zenotravel (lts_none=336, 275625) ---
      new Case("ipc-2002 zenotravel / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/zenotravel-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-1.pddl"),
      new Case("ipc-2002 zenotravel / instance-3",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/zenotravel-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-3.pddl"),
      // --- ipc-2002 depots (lts_none=576, 40320) ---
      new Case("ipc-2002 depots / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/depots-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/depots-strips-automatic/instances/instance-1.pddl"),
      new Case("ipc-2002 depots / instance-2",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/depots-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/depots-strips-automatic/instances/instance-2.pddl"),
      // --- ipc-2006 pipesworld (lts_none=1053) ---
      new Case("ipc-2006 pipesworld / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-2006/domains/pipesworld-propositional-strips/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2006/domains/pipesworld-propositional-strips/instances/instance-1.pddl"),
      // --- ipc-1998 gripper (lts_none=2657, 121745) ---
      new Case("ipc-1998 gripper / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-1998/domains/gripper-round-1-strips/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-1998/domains/gripper-round-1-strips/instances/instance-1.pddl"),
      new Case("ipc-1998 gripper / instance-2",
          "src/main/resources/pddl-examples/potassco/ipc-1998/domains/gripper-round-1-strips/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-1998/domains/gripper-round-1-strips/instances/instance-2.pddl"),
      // --- ipc-2008 sokoban (lts_none=4200) ---
      new Case("ipc-2008 sokoban / instance-1 (no-costs)",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/sokoban-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/sokoban-sequential-optimal-strips-no-costs/instances/instance-1.pddl"),
      // --- ipc-2002 freecell (lts_none=4365) ---
      new Case("ipc-2002 freecell / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/freecell-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/freecell-strips-automatic/instances/instance-1.pddl"),
      // --- ipc-2002 driverlog (lts_none=10575) ---
      new Case("ipc-2002 driverlog / instance-1",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/driverlog-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2002/domains/driverlog-strips-automatic/instances/instance-1.pddl"),
      // --- ipc-2008 woodworking (lts_none=16875, 86632, 324567) ---
      new Case("ipc-2008 woodworking / instance-1 (no-costs)",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-1.pddl"),
      new Case("ipc-2008 woodworking / instance-2 (no-costs)",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-2.pddl"),
      new Case("ipc-2008 woodworking / instance-12-trimmed (no-costs)",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-12-trimmed.pddl"),
      // --- ai-planning woodworking (lts_none=44658) ---
      new Case("ai-planning woodworking / instance-1",
          "src/main/resources/pddl-examples/ai-planning/woodworking/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/woodworking/instances/instance-1.pddl"),
      // --- ipc-2008 scanalyzer (lts_none=46080) ---
      new Case("ipc-2008 scanalyzer / instance-1 (no-costs)",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/scanalyzer-3d-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2008/domains/scanalyzer-3d-sequential-optimal-strips-no-costs/instances/instance-1.pddl"),
      // --- ai-planning storage (lts_none=50238, 158784, TIMEOUT) ---
      new Case("ai-planning storage / storage-08",
          "src/main/resources/pddl-examples/ai-planning/storage/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/storage/instances/storage-08.pddl"),
      new Case("ai-planning storage / storage-10",
          "src/main/resources/pddl-examples/ai-planning/storage/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/storage/instances/storage-10.pddl"),
      new Case("ai-planning storage / storage-09",
          "src/main/resources/pddl-examples/ai-planning/storage/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/storage/instances/storage-09.pddl"),
      // --- ai-planning gripper (lts_none=121745, TIMEOUT, TIMEOUT) ---
      new Case("ai-planning gripper / 6 balls",
          "src/main/resources/pddl-examples/ai-planning/gripper/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/gripper/instances/gripper-6.pddl"),
      new Case("ai-planning gripper / 8 balls",
          "src/main/resources/pddl-examples/ai-planning/gripper/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/gripper/instances/gripper-8.pddl"),
      new Case("ai-planning gripper / 10 balls",
          "src/main/resources/pddl-examples/ai-planning/gripper/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/gripper/instances/gripper-10.pddl"),
      // --- ipc-2000 logistics (lts_none=134456) ---
      new Case("ipc-2000 logistics / instance-1-5pkg",
          "src/main/resources/pddl-examples/potassco/ipc-2000/domains/logistics-strips-typed/domain.pddl",
          "src/main/resources/pddl-examples/potassco/ipc-2000/domains/logistics-strips-typed/instances/instance-1-5pkg.pddl"),
      // --- ai-planning ferry (lts_none=171875, 559872, TIMEOUT) ---
      new Case("ai-planning ferry / instance-1",
          "src/main/resources/pddl-examples/ai-planning/ferry/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/ferry/instances/instance-1.pddl"),
      new Case("ai-planning ferry / l6-c6-s1",
          "src/main/resources/pddl-examples/ai-planning/ferry/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/ferry/instances/ferry-l6-c6-s1.pddl"),
      new Case("ai-planning ferry / l5-c7-s1",
          "src/main/resources/pddl-examples/ai-planning/ferry/domain.pddl",
          "src/main/resources/pddl-examples/ai-planning/ferry/instances/ferry-l5-c7-s1.pddl"));

  public static void main(String[] args) throws Exception {
    Path output = DEFAULT_OUTPUT;
    StrategyMode strategy = StrategyMode.SSS;
    CheckerMode checker = CheckerMode.DIRECT;
    boolean skipCheck = false;
    boolean allStrategies = false;
    String caseFilter = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--skip-check")) skipCheck = true;
      else if (args[i].equals("--strategy") && i + 1 < args.length) {
        String v = args[++i];
        if (v.equalsIgnoreCase("all")) allStrategies = true;
        else strategy = StrategyMode.parse(v);
      }
      else if (args[i].equals("--checker") && i + 1 < args.length) checker = CheckerMode.parse(args[++i]);
      else if (args[i].equals("--case") && i + 1 < args.length) caseFilter = args[++i];
      else output = Path.of(args[i]);
    }

    // Filter cases by case-insensitive substring of their name; no filter keeps all.
    List<Case> cases = CASES_FINAL;
    if (caseFilter != null) {
      String needle = caseFilter.toLowerCase();
      cases = new ArrayList<>();
      for (Case c : CASES_FINAL) {
        if (c.name().toLowerCase().contains(needle)) cases.add(c);
      }
      System.err.println(cases.size() + " case(s) match \"" + caseFilter + "\"");
      if (cases.isEmpty()) return;
    }

    List<StrategyMode> strategies = allStrategies
        ? List.of(StrategyMode.NONE, StrategyMode.STRATIFIED, StrategyMode.SSS)
        : List.of(strategy);

    // A single strategy writes the per-strategy CSV used to build the tables;
    // running all strategies is a console-only spot-check.
    PrintWriter writer = null;
    try {
      if (strategies.size() == 1) {
        Files.createDirectories(output.toAbsolutePath().getParent());
        writer = new PrintWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8));
        writer.println(Result.csvHeader());
      } else {
        System.err.println("Running all strategies (console only, no CSV)");
      }
      for (StrategyMode st : strategies) {
        Options opts = new Options(st, checker, skipCheck);
        for (int i = 0; i < cases.size(); i++) {
          Result result = runCase(cases.get(i), opts);
          if (writer != null) {
            writer.println(result.toCsv());
            writer.flush();
          }
          printSummary(i, cases.size(), result, opts);
        }
      }
    } finally {
      if (writer != null) writer.close();
    }
  }

  private static Result runCase(Case c, Options opts) throws Exception {
    Path snapshotFile = Files.createTempFile("benchmark-snapshot-", ".txt");
    long start = System.currentTimeMillis();

    List<String> command = new ArrayList<>(List.of(
        Path.of(System.getProperty("java.home"), "bin", "java").toString(),
        "-cp", System.getProperty("java.class.path"),
        PddlCaseRunnerCli.class.getName(),
        c.name, c.domain, c.problem, snapshotFile.toString(),
        "--strategy", opts.strategy().cliName,
        "--checker", opts.checker().cliName));
    if (opts.skipCheck()) command.add("--skip-check");

    Process process = new ProcessBuilder(command)
        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
        .redirectError(ProcessBuilder.Redirect.DISCARD)
        .start();
    try {
      boolean finished = process.waitFor(PddlCaseRunnerCli.TIMEOUT_MS, TimeUnit.MILLISECONDS);
      long elapsed = Math.min(System.currentTimeMillis() - start, PddlCaseRunnerCli.TIMEOUT_MS);
      if (!finished) {
        process.destroyForcibly();
        process.waitFor();
      }
      Result result = Result.readFrom(snapshotFile, c.name);
      result.timeMs = elapsed;
      if (!finished) result.status = "TIMEOUT";
      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
      Result result = new Result(c.name);
      result.timeMs = Math.min(System.currentTimeMillis() - start, PddlCaseRunnerCli.TIMEOUT_MS);
      result.status = "TIMEOUT";
      return result;
    } finally {
      Files.deleteIfExists(snapshotFile);
    }
  }

  private static void printSummary(int i, int total, Result r, Options opts) {
    String lts = r.ltsNodes > 0
        ? r.ltsNodes + " / " + r.ltsEdges
        : r.ltsGenerationNodes + " / " + r.ltsGenerationEdges + " generated";
    String kh = opts.skipCheck() ? "skipped"
        : r.khFinalNodes > 0 ? r.khFinalNodes + " / " + r.khFinalEdges
        : r.khGenerationNodes + " / " + r.khGenerationEdges + " generated";
    System.out.println("[" + (i + 1) + "/" + total + "] " + r.caseName
        + " | strategy=" + opts.strategy().cliName + " | checker=" + opts.checker().cliName
        + " | LTS=" + lts + " | KH=" + kh
        + " | passed=" + r.passed + " | time=" + r.timeMs + " ms | status=" + r.status);
  }

  private record Case(String name, String domain, String problem) {}
}
