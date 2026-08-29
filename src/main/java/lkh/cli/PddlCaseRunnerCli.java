package lkh.cli;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lkh.expression.Expression;
import lkh.lts.LTS;
import lkh.lts.builder.pddl.DefaultActionSelectionStrategy;
import lkh.lts.builder.pddl.PDDL;
import lkh.lts.builder.pddl.ActionSelectionStrategy;
import lkh.modelchecker.ClassicAutomataModelChecker;
import lkh.modelchecker.DirectAutomataModelChecker;
import lkh.por.StratifiedActionSelectionStrategy;
import lkh.por.StrongStubbornSetActionSelectionStrategy;
import logger.DeadlineGraphLogger;
import logger.DeadlineGraphLogger.TimeoutSignal;
import logger.GraphLogger;
import logger.LoggerContext;

public class PddlCaseRunnerCli {
  static final long TIMEOUT_MS = 60_000;

  // Usage: <name> <domain> <problem> <snapshot-file> [--strategy none|sss|stratified] [--checker direct|classic] [--skip-check]
  public static void main(String[] args) throws Exception {
    String name = args[0];
    String domain = args[1];
    String problem = args[2];
    Path snapshotFile = Path.of(args[3]);
    Options opts = Options.parse(args, 4);
    long start = System.currentTimeMillis();
    Result result = run(name, domain, problem, snapshotFile, opts);
    result.timeMs = Math.min(System.currentTimeMillis() - start, TIMEOUT_MS);
    System.out.println(result.toCsv());
  }

  static Result run(String name, String domain, String problem, Path snapshotFile, Options opts) throws Exception {
    Result result = new Result(name);
    long deadline = System.currentTimeMillis() + TIMEOUT_MS;
    GraphLogger[] loggers = new GraphLogger[2];
    Runnable snapshot = () -> {
      if (loggers[0] != null && loggers[1] != null) {
        try { result.saveTo(snapshotFile, loggers[0], loggers[1]); } catch (Exception ignored) {}
      }
    };
    GraphLogger ltsLogger = new DeadlineGraphLogger("LTS", deadline, snapshot);
    GraphLogger khLogger = new DeadlineGraphLogger("KH Automaton", deadline, snapshot);
    loggers[0] = ltsLogger;
    loggers[1] = khLogger;

    try {
      PDDL pddl = new PDDL(domain, problem, opts.strategy.build());

      LTS<Integer, String> lts;
      try (var scope = LoggerContext.withLogger(ltsLogger)) {
        lts = pddl.buildLTS();
      }
      ltsLogger.setSize(lts.getSize());

      if (!opts.skipCheck) {
        Expression query = Expression.kh(pddl.getInitialExpression(), pddl.getGoalExpression());
        boolean passed;
        try (var scope = LoggerContext.withLogger(khLogger)) {
          passed = opts.checker.check(lts, pddl.getInitialState(), query);
        }
        result.passed = passed ? "true" : "false";
      }

      result.status = "OK";
    } catch (TimeoutSignal e) {
      result.status = "TIMEOUT";
    } catch (Throwable e) {
      e.printStackTrace(System.err);
    } finally {
      result.saveTo(snapshotFile, ltsLogger, khLogger);
      LoggerContext.clearLogger();
    }
    return result;
  }

  // ---------------------------------------------------------------------------

  enum StrategyMode {
    NONE("none") {
      @Override ActionSelectionStrategy build() { return new DefaultActionSelectionStrategy(); }
    },
    SSS("sss") {
      @Override ActionSelectionStrategy build() { return new StrongStubbornSetActionSelectionStrategy(); }
    },
    STRATIFIED("stratified") {
      @Override ActionSelectionStrategy build() { return new StratifiedActionSelectionStrategy(); }
    };

    final String cliName;
    StrategyMode(String cliName) { this.cliName = cliName; }
    abstract ActionSelectionStrategy build();
    static StrategyMode parse(String v) {
      if (v.equalsIgnoreCase("none")) return NONE;
      if (v.equalsIgnoreCase("stratified")) return STRATIFIED;
      return SSS;
    }
  }

  enum CheckerMode {
    DIRECT("direct") {
      @Override <S, A> boolean check(LTS<S, A> lts, S initial, Expression query) throws Exception {
        return new DirectAutomataModelChecker<>(lts, initial).check(query);
      }
    },
    CLASSIC("classic") {
      @Override <S, A> boolean check(LTS<S, A> lts, S initial, Expression query) throws Exception {
        return new ClassicAutomataModelChecker<>(lts, initial, false).check(query);
      }
    };

    final String cliName;
    CheckerMode(String cliName) { this.cliName = cliName; }
    abstract <S, A> boolean check(LTS<S, A> lts, S initial, Expression query) throws Exception;
    static CheckerMode parse(String v) { return v.equalsIgnoreCase("classic") ? CLASSIC : DIRECT; }
  }

  record Options(StrategyMode strategy, CheckerMode checker, boolean skipCheck) {
    static Options parse(String[] args, int from) {
      StrategyMode strategy = StrategyMode.SSS;
      CheckerMode checker = CheckerMode.DIRECT;
      boolean skipCheck = false;
      for (int i = from; i < args.length; i++) {
        if (args[i].equals("--skip-check")) skipCheck = true;
        else if (args[i].equals("--strategy") && i + 1 < args.length) strategy = StrategyMode.parse(args[++i]);
        else if (args[i].equals("--checker") && i + 1 < args.length) checker = CheckerMode.parse(args[++i]);
      }
      return new Options(strategy, checker, skipCheck);
    }
  }

  static final class Result {
    final String caseName;
    int ltsGenerationNodes, ltsGenerationEdges, ltsNodes, ltsEdges;
    int khGenerationNodes, khGenerationEdges, khFinalNodes, khFinalEdges;
    String passed = "";
    String status = "ERROR";
    long timeMs = 0;

    Result(String caseName) { this.caseName = caseName; }

    static String csvHeader() {
      return "case_name,lts_generation_nodes,lts_generation_edges,lts_nodes,lts_edges,"
          + "kh_generation_nodes,kh_generation_edges,kh_final_nodes,kh_final_edges,passed,time_ms,status";
    }

    String toCsv() {
      return q(caseName) + "," + ltsGenerationNodes + "," + ltsGenerationEdges + ","
          + ltsNodes + "," + ltsEdges + "," + khGenerationNodes + "," + khGenerationEdges + ","
          + khFinalNodes + "," + khFinalEdges + "," + q(passed) + "," + timeMs + "," + q(status);
    }

    private void syncFrom(GraphLogger lts, GraphLogger kh) {
      ltsGenerationNodes = lts.getGeneratedNodesCant();
      ltsGenerationEdges = lts.getGeneratedEdgesCant();
      ltsNodes = lts.getNodesCant();
      ltsEdges = lts.getEdgesCant();
      khGenerationNodes = kh.getGeneratedNodesCant();
      khGenerationEdges = kh.getGeneratedEdgesCant();
      khFinalNodes = kh.getNodesCant();
      khFinalEdges = kh.getEdgesCant();
    }

    void saveTo(Path path, GraphLogger lts, GraphLogger kh) throws Exception {
      syncFrom(lts, kh);
      Files.writeString(path, caseName + "\t" + ltsGenerationNodes + "\t" + ltsGenerationEdges + "\t"
          + ltsNodes + "\t" + ltsEdges + "\t" + khGenerationNodes + "\t" + khGenerationEdges + "\t"
          + khFinalNodes + "\t" + khFinalEdges + "\t" + passed + "\t" + status, StandardCharsets.UTF_8);
    }

    static Result readFrom(Path path, String caseName) throws Exception {
      Result r = new Result(caseName);
      String line = Files.readString(path, StandardCharsets.UTF_8).trim();
      if (line.isEmpty()) return r;
      String[] p = line.split("\t", -1);
      if (p.length < 11) return r;
      r.ltsGenerationNodes = Integer.parseInt(p[1]);
      r.ltsGenerationEdges = Integer.parseInt(p[2]);
      r.ltsNodes = Integer.parseInt(p[3]);
      r.ltsEdges = Integer.parseInt(p[4]);
      r.khGenerationNodes = Integer.parseInt(p[5]);
      r.khGenerationEdges = Integer.parseInt(p[6]);
      r.khFinalNodes = Integer.parseInt(p[7]);
      r.khFinalEdges = Integer.parseInt(p[8]);
      r.passed = p[9]; r.status = p[10];
      return r;
    }

    private static String q(String s) { return "\"" + s.replace("\"", "\"\"") + "\""; }
  }

}
