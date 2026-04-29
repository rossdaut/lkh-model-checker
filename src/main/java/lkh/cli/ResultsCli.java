package lkh.cli;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lkh.expression.Expression;
import lkh.lts.LTS;
import lkh.lts.builder.DefaultActionSelectionStrategy;
import lkh.lts.builder.PDDL;
import lkh.modelchecker.ClassicAutomataModelChecker;
import lkh.modelchecker.DirectAutomataModelChecker;
import lkh.lts.builder.ActionSelectionStrategy;
import lkh.por.StrongStubbornSetActionSelectionStrategy;
import logger.GraphLogger;
import logger.LoggerContext;

public class ResultsCli {
  private static final long TIMEOUT_MS = 60000;
  private static final int SNAPSHOT_MASK = 0x3FF;
  private static final Path DEFAULT_OUTPUT = Path.of(
      "src/main/resources/pddl-examples/sss-direct.csv");


  private static final List<CaseData> CASES = List.of(
      new CaseData(
          "tire / tire-problem",
          "src/main/resources/pddl-examples/tire-domain.pddl",
          "src/main/resources/pddl-examples/tire-problem.pddl"),
      new CaseData(
          "rsc / easy",
          "src/main/resources/pddl-examples/rsc-domain.pddl",
          "src/main/resources/pddl-examples/rsc-problem-easy.pddl"),
      new CaseData(
          "ipc-2000 blocks / instance-1",
          "src/main/resources/pddl-examples/external/potassco/ipc-2000/domains/blocks-strips-typed/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2000/domains/blocks-strips-typed/instances/instance-1.pddl"),
      new CaseData(
          "logistics / local-problem",
          "src/main/resources/pddl-examples/logistics-domain.pddl",
          "src/main/resources/pddl-examples/logistics-problem.pddl"),
      new CaseData(
          "ipc-1998 gripper / instance-1",
          "src/main/resources/pddl-examples/external/potassco/ipc-1998/domains/gripper-round-1-strips/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-1998/domains/gripper-round-1-strips/instances/instance-1.pddl"),
      new CaseData(
          "ipc-1998 gripper / instance-2",
          "src/main/resources/pddl-examples/external/potassco/ipc-1998/domains/gripper-round-1-strips/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-1998/domains/gripper-round-1-strips/instances/instance-2.pddl"),
      new CaseData(
          "ipc-2002 zenotravel / instance-1",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/zenotravel-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-1.pddl"),
      new CaseData(
          "ipc-2002 depots / instance-1",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/depots-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/depots-strips-automatic/instances/instance-1.pddl"),
      new CaseData(
          "ipc-2002 zenotravel / instance-2",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/zenotravel-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/zenotravel-strips-automatic/instances/instance-2.pddl"),
      new CaseData(
          "ipc-2008 parc-printer / instance-1 (no-costs)",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-1.pddl"),
      new CaseData(
          "ipc-2008 parc-printer / instance-2 (no-costs)",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-2.pddl"),
      new CaseData(
          "ipc-2008 woodworking / instance-1 (no-costs)",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-1.pddl"),
      new CaseData(
          "ipc-2002 satellite-strips-automatic / instance-1",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/satellite-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/satellite-strips-automatic/instances/instance-1.pddl"),
      new CaseData(
          "ipc-2008 parc-printer / instance-3 (no-costs)",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/parc-printer-sequential-optimal-strips-no-costs/instances/instance-3.pddl"),
      new CaseData(
          "ipc-2008 woodworking / instance-2 (no-costs)",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-2.pddl"),
      new CaseData(
          "ipc-2000 logistics / instance-1 (5pkg)",
          "src/main/resources/pddl-examples/external/potassco/ipc-2000/domains/logistics-strips-typed/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2000/domains/logistics-strips-typed/instances/instance-1-5pkg.pddl"),
      new CaseData(
          "ipc-2002 satellite-strips-automatic / instance-2-lite",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/satellite-strips-automatic/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2002/domains/satellite-strips-automatic/instances/instance-2-lite.pddl"),
      new CaseData(
          "ipc-2008 woodworking / instance-12-trimmed (no-costs)",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/domain.pddl",
          "src/main/resources/pddl-examples/external/potassco/ipc-2008/domains/woodworking-sequential-optimal-strips-no-costs/instances/instance-12-trimmed.pddl"),
      new CaseData(
          "ai-planning ferry / 5 locations, 6 cars (seed 1)",
          "src/main/resources/pddl-examples/external/ai-planning/ferry/domain.pddl",
          "src/main/resources/pddl-examples/external/ai-planning/ferry/instances/ferry-l5-c6-s1.pddl"),
      new CaseData(
          "ai-planning ferry / 6 locations, 6 cars (seed 1)",
          "src/main/resources/pddl-examples/external/ai-planning/ferry/domain.pddl",
          "src/main/resources/pddl-examples/external/ai-planning/ferry/instances/ferry-l6-c6-s1.pddl"));

  /*
   * Backup exploratory list before the 60 s rebalance:
   * ipc-2004 satellite / instance-1
   * ipc-2002 rovers / instance-1
   * ipc-2002 driverlog / instance-1
   * ipc-2000 logistics / instance-1
   * ipc-1998 logistics-round-2-strips / instance-1
   * ipc-1998 logistics-round-2-strips / instance-2
   * ai-planning gripper / 6 balls
   * ai-planning storage / instance-08
   * ai-planning storage / instance-10
   */

  public static void main(String[] args) throws Exception {
    if (args.length >= 3 && args[0].equals("--case")) {
      StrategyMode strategy = StrategyMode.SSS;
      CheckerMode checker = CheckerMode.DIRECT;
      boolean skipCheck = false;
      for (int i = 3; i < args.length; i++) {
        if (args[i].equals("--skip-check")) {
          skipCheck = true;
        } else if (args[i].equals("--strategy") && i + 1 < args.length) {
          strategy = StrategyMode.parse(args[++i]);
        } else if (args[i].equals("--checker") && i + 1 < args.length) {
          checker = CheckerMode.parse(args[++i]);
        }
      }
      executeCase(CASES.get(Integer.parseInt(args[1])), Path.of(args[2]), skipCheck, strategy, checker);
      return;
    }
    if (args.length >= 5 && args[0].equals("--custom")) {
      StrategyMode strategy = StrategyMode.SSS;
      CheckerMode checker = CheckerMode.DIRECT;
      boolean skipCheck = false;
      CaseData caseData = new CaseData(args[1], args[2], args[3]);
      Path output = Path.of(args[4]);
      for (int i = 5; i < args.length; i++) {
        if (args[i].equals("--skip-check")) {
          skipCheck = true;
        } else if (args[i].equals("--strategy") && i + 1 < args.length) {
          strategy = StrategyMode.parse(args[++i]);
        } else if (args[i].equals("--checker") && i + 1 < args.length) {
          checker = CheckerMode.parse(args[++i]);
        }
      }
      long start = System.currentTimeMillis();
      executeCase(caseData, output, skipCheck, strategy, checker);
      Result result = Result.readFrom(output, caseData.name);
      result.timeMs = Math.min(System.currentTimeMillis() - start, TIMEOUT_MS);
      System.out.println(result.toCsv());
      return;
    }

    Path output = DEFAULT_OUTPUT;
    StrategyMode strategy = StrategyMode.SSS;
    CheckerMode checker = CheckerMode.DIRECT;
    boolean skipCheck = false;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--skip-check")) {
        skipCheck = true;
      } else if (args[i].equals("--strategy") && i + 1 < args.length) {
        strategy = StrategyMode.parse(args[++i]);
      } else if (args[i].equals("--checker") && i + 1 < args.length) {
        checker = CheckerMode.parse(args[++i]);
      } else {
        output = Path.of(args[i]);
      }
    }
    run(output, skipCheck, strategy, checker);
  }

  private static void run(Path output, boolean skipCheck, StrategyMode strategy, CheckerMode checker) throws Exception {
    Files.createDirectories(output.toAbsolutePath().getParent());

    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {
      writer.println(Result.header());

      for (int i = 0; i < CASES.size(); i++) {
        Result result = runCase(i, skipCheck, strategy, checker);
        writer.println(result.toCsv());
        writer.flush();
        printSummary(i, result, skipCheck, strategy, checker);
      }
    }
  }

  private static void printSummary(int caseIndex, Result result, boolean skipCheck, StrategyMode strategy, CheckerMode checker) {
    String ltsSummary = result.ltsNodes > 0
        ? result.ltsNodes + " / " + result.ltsEdges
        : result.ltsGenerationNodes + " / " + result.ltsGenerationEdges + " generated";
    String khSummary = skipCheck
        ? "skipped"
        : (result.khFinalNodes > 0
            ? result.khFinalNodes + " / " + result.khFinalEdges
            : result.khGenerationNodes + " / " + result.khGenerationEdges + " generated");
    System.out.println(
        "[" + (caseIndex + 1) + "/" + CASES.size() + "] "
            + result.caseName
            + " | strategy=" + strategy.cliName
            + " | checker=" + checker.cliName
            + " | LTS=" + ltsSummary
            + " | KH=" + khSummary
            + " | passed=" + result.passed
            + " | time=" + result.timeMs + " ms"
            + " | status=" + result.status);
  }

  private static Result runCase(int caseIndex, boolean skipCheck, StrategyMode strategy, CheckerMode checker) throws Exception {
    CaseData caseData = CASES.get(caseIndex);
    return runCase(caseData, skipCheck, strategy, checker);
  }

  private static Result runCase(CaseData caseData, boolean skipCheck, StrategyMode strategy, CheckerMode checker)
      throws Exception {
    Path tempFile = Files.createTempFile("preliminary-results-", ".txt");
    long start = System.currentTimeMillis();
    String javaBin = Path.of(System.getProperty("java.home"), "bin", "java").toString();
    String classPath = System.getProperty("java.class.path");
    List<String> command = new ArrayList<>(List.of(
        javaBin,
        "-cp",
        classPath,
        ResultsCli.class.getName(),
        "--custom",
        caseData.name,
        caseData.domainFile,
        caseData.problemFile,
        tempFile.toString()));

    if (skipCheck) {
      command.add("--skip-check");
    }
    command.add("--strategy");
    command.add(strategy.cliName);
    command.add("--checker");
    command.add(checker.cliName);

    Process process = new ProcessBuilder(command)
        .redirectOutput(ProcessBuilder.Redirect.DISCARD)
        .redirectError(ProcessBuilder.Redirect.DISCARD)
        .start();

    try {
      boolean finished = process.waitFor(TIMEOUT_MS, TimeUnit.MILLISECONDS);
      long elapsed = Math.min(System.currentTimeMillis() - start, TIMEOUT_MS);

      if (!finished) {
        process.destroyForcibly();
        process.waitFor();
        Result result = Result.readFrom(tempFile, caseData.name);
        result.timeMs = elapsed;
        result.status = "TIMEOUT";
        return result;
      }

      Result result = Result.readFrom(tempFile, caseData.name);
      result.timeMs = elapsed;
      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      process.destroyForcibly();
      Result result = new Result(caseData.name);
      result.timeMs = Math.min(System.currentTimeMillis() - start, TIMEOUT_MS);
      result.status = "TIMEOUT";
      return result;
    } finally {
      Files.deleteIfExists(tempFile);
    }
  }

  private enum StrategyMode {
    NONE("none") {
      @Override
      ActionSelectionStrategy build() {
        return new DefaultActionSelectionStrategy();
      }
    },
    SSS("sss") {
      @Override
      ActionSelectionStrategy build() {
        return new StrongStubbornSetActionSelectionStrategy();
      }
    };

    private final String cliName;

    StrategyMode(String cliName) {
      this.cliName = cliName;
    }

    abstract ActionSelectionStrategy build();

    private static StrategyMode parse(String value) {
      return value.equalsIgnoreCase("none") ? NONE : SSS;
    }
  }

  private enum CheckerMode {
    DIRECT("direct"),
    CLASSIC("classic");

    private final String cliName;

    CheckerMode(String cliName) {
      this.cliName = cliName;
    }

    private static CheckerMode parse(String value) {
      return value.equalsIgnoreCase("classic") ? CLASSIC : DIRECT;
    }
  }

  private static final class CaseData {
    private final String name;
    private final String domainFile;
    private final String problemFile;

    private CaseData(String name, String domainFile, String problemFile) {
      this.name = name;
      this.domainFile = domainFile;
      this.problemFile = problemFile;
    }
  }

  private static void executeCase(CaseData caseData, Path tempFile, boolean skipCheck, StrategyMode strategy, CheckerMode checker) throws Exception {
    Result result = new Result(caseData.name);
    long deadline = System.currentTimeMillis() + TIMEOUT_MS;
    GraphLogger[] loggers = new GraphLogger[2];
    Runnable snapshot = () -> {
      if (loggers[0] != null && loggers[1] != null) {
        try {
          result.save(tempFile, loggers[0], loggers[1]);
        } catch (Exception ignored) {
        }
      }
    };
    GraphLogger ltsLogger = new DeadlineGraphLogger("LTS", deadline, snapshot);
    GraphLogger khLogger = new DeadlineGraphLogger("KH Automaton", deadline, snapshot);
    loggers[0] = ltsLogger;
    loggers[1] = khLogger;

    try {
      PDDL pddl = new PDDL(
          caseData.domainFile,
          caseData.problemFile,
          strategy.build());

      LTS<Integer, String> lts;
      try (var scope = LoggerContext.withLogger(ltsLogger)) {
        lts = pddl.buildLTS();
      }
      ltsLogger.setSize(lts.getSize());
      result.syncFromLoggers(ltsLogger, khLogger);

      if (skipCheck) {
        result.status = "OK";
        return;
      }

      Expression initial = pddl.getInitialExpression();
      Expression goal = pddl.getGoalExpression();
      Expression query = Expression.kh(initial, goal);

      boolean passed;
      if (checker == CheckerMode.CLASSIC) {
        ClassicAutomataModelChecker<Integer, String> modelChecker =
            new ClassicAutomataModelChecker<>(lts, pddl.getInitialState(), false);
        try (var scope = LoggerContext.withLogger(khLogger)) {
          passed = modelChecker.check(query);
        }
      } else {
        DirectAutomataModelChecker<Integer, String> modelChecker =
            new DirectAutomataModelChecker<>(lts, pddl.getInitialState());
        try (var scope = LoggerContext.withLogger(khLogger)) {
          passed = modelChecker.check(query);
        }
      }

      result.syncFromLoggers(ltsLogger, khLogger);
      result.passed = passed ? "true" : "false";
      result.status = "OK";
    } catch (TimeoutSignal e) {
      result.syncFromLoggers(ltsLogger, khLogger);
      result.status = "TIMEOUT";
    } catch (Throwable e) {
      result.syncFromLoggers(ltsLogger, khLogger);
      result.passed = "";
      e.printStackTrace(System.err);
    } finally {
      result.save(tempFile, ltsLogger, khLogger);
      LoggerContext.clearLogger();
    }
  }

  private static final class TimeoutSignal extends RuntimeException {
  }

  private static final class DeadlineGraphLogger extends GraphLogger {
    private final long deadline;
    private final Runnable snapshot;
    private int eventCount;

    private DeadlineGraphLogger(String name, long deadline, Runnable snapshot) {
      super(name, false);
      this.deadline = deadline;
      this.snapshot = snapshot;
    }

    @Override
    public void log(logger.LogEvent event) {
      super.log(event);
      if ((++eventCount & SNAPSHOT_MASK) == 0) {
        snapshot.run();
      }
      if ((eventCount & 0xFF) == 0 && System.currentTimeMillis() > deadline) {
        throw new TimeoutSignal();
      }
    }
  }

  private static final class Result {
    private final String caseName;
    private int ltsGenerationNodes;
    private int ltsGenerationEdges;
    private int ltsNodes;
    private int ltsEdges;
    private int khGenerationNodes;
    private int khGenerationEdges;
    private int khFinalNodes;
    private int khFinalEdges;
    private String passed;
    private String status;
    private long timeMs;

    private Result(String caseName) {
      this.caseName = caseName;
      this.passed = "";
      this.status = "ERROR";
      this.timeMs = 0;
    }

    private static String header() {
      return "case_name,lts_generation_nodes,lts_generation_edges,lts_nodes,lts_edges,kh_generation_nodes,kh_generation_edges,kh_final_nodes,kh_final_edges,passed,time_ms,status";
    }

    private String toDataLine() {
      return caseName + "\t"
          + ltsGenerationNodes + "\t"
          + ltsGenerationEdges + "\t"
          + ltsNodes + "\t"
          + ltsEdges + "\t"
          + khGenerationNodes + "\t"
          + khGenerationEdges + "\t"
          + khFinalNodes + "\t"
          + khFinalEdges + "\t"
          + passed + "\t"
          + status;
    }

    private String toCsv() {
      return quote(caseName) + ","
          + ltsGenerationNodes + ","
          + ltsGenerationEdges + ","
          + ltsNodes + ","
          + ltsEdges + ","
          + khGenerationNodes + ","
          + khGenerationEdges + ","
          + khFinalNodes + ","
          + khFinalEdges + ","
          + quote(passed) + ","
          + timeMs + ","
          + quote(status);
    }

    private void syncFromLoggers(GraphLogger ltsLogger, GraphLogger khLogger) {
      ltsGenerationNodes = ltsLogger.getGeneratedNodesCant();
      ltsGenerationEdges = ltsLogger.getGeneratedEdgesCant();
      ltsNodes = ltsLogger.getNodesCant();
      ltsEdges = ltsLogger.getEdgesCant();
      khGenerationNodes = khLogger.getGeneratedNodesCant();
      khGenerationEdges = khLogger.getGeneratedEdgesCant();
      khFinalNodes = khLogger.getNodesCant();
      khFinalEdges = khLogger.getEdgesCant();
    }

    private void save(Path path, GraphLogger ltsLogger, GraphLogger khLogger) throws Exception {
      syncFromLoggers(ltsLogger, khLogger);
      Files.writeString(path, toDataLine(), StandardCharsets.UTF_8);
    }

    private static Result readFrom(Path path, String caseName) throws Exception {
      Result result = new Result(caseName);
      String line = Files.readString(path, StandardCharsets.UTF_8).trim();
      if (line.isEmpty()) {
        return result;
      }

      String[] parts = line.split("\t", -1);
      if (parts.length < 11) {
        return result;
      }

      result.ltsGenerationNodes = Integer.parseInt(parts[1]);
      result.ltsGenerationEdges = Integer.parseInt(parts[2]);
      result.ltsNodes = Integer.parseInt(parts[3]);
      result.ltsEdges = Integer.parseInt(parts[4]);
      result.khGenerationNodes = Integer.parseInt(parts[5]);
      result.khGenerationEdges = Integer.parseInt(parts[6]);
      result.khFinalNodes = Integer.parseInt(parts[7]);
      result.khFinalEdges = Integer.parseInt(parts[8]);
      result.passed = parts[9];
      result.status = parts[10];
      return result;
    }

    private String quote(String text) {
      return "\"" + text.replace("\"", "\"\"") + "\"";
    }
  }
}
