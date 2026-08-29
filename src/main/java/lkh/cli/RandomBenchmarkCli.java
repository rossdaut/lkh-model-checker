package lkh.cli;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lkh.expression.Expression;
import lkh.generator.GeneratedLts;
import lkh.generator.LtsGeneratorConfig;
import lkh.generator.RandomLtsGenerator;
import lkh.modelchecker.ClassicAutomataModelChecker;
import lkh.modelchecker.DirectAutomataModelChecker;
import logger.DeadlineGraphLogger;
import logger.DeadlineGraphLogger.TimeoutSignal;
import logger.LoggerContext;

/**
 * Compares the Classic and Direct algorithms for building A^kh on randomly generated
 * LTSs. The experiment is organized in stages, each varying a SINGLE parameter relative
 * to the baseline configuration.
 *
 * Usage:
 *   java lkh.cli.RandomBenchmarkCli [output.csv] [--direct-only|--classic-only] [--stage A]
 *
 * NOTE: the LTSs are deterministic (deterministic=true). The Classic algorithm builds its
 * A_{t1,t2}^c automata as GraphDeterministicAutomaton, which silently drops extra
 * transitions on non-deterministic LTSs; restricting to deterministic LTSs keeps both
 * algorithms correct and the comparison fair.
 *
 * Baseline:
 *   minNodeCount=200, edgeDensity=3 (→ 600 edges), actionCount=8, propositionCount=14,
 *   initialCondition=p0&..&p7 (8-conj, P≈1/256), goalCondition=p8&p9 (2-conj, P≈1/4),
 *   initialStateCount=3, goalStateCount=2, witnessCount=3, minWitnessActionCount=8,
 *   deterministic=true.
 *
 * Stages:
 *   A — LTS size:         minNodeCount ∈ {100, 200, 400, 800, 1600}
 *   B — initial states:   initialStateCount ∈ {1, 2, 3, 4, 6}
 *   C — plan length:      minWitnessActionCount ∈ {3, 5, 8, 12}
 *   D — LTS density:      edgesPerNode ∈ {2, 3, 4, 6, 10}
 *   E — witness count:    witnessCount ∈ {1, 3, 6}
 */
public class RandomBenchmarkCli {

  // --- baseline ---
  static final int     BASE_MIN_NODES      = 200;
  static final int     BASE_EDGE_DENSITY   = 3;     // edges per state
  static final int     ACTION_COUNT        = 8;
  static final int     PROPOSITION_COUNT   = 14;
  static final int     BASE_INIT_COUNT     = 3;
  static final int     GOAL_STATE_COUNT    = 2;
  static final int     BASE_WITNESS_COUNT  = 3;
  static final int     BASE_WITNESS_LEN    = 8;
  static final boolean BASE_DETERMINISTIC  = true;

  // 8-conjunction over p0..p7: P≈1/256, strongly restrictive.
  // propositionCount=14 → 6 free props → 2^6=64 unique labels for initial states.
  static final Expression INITIAL_CONDITION = Expression.and(
      Expression.prop("p0"), Expression.prop("p1"), Expression.prop("p2"),
      Expression.prop("p3"), Expression.prop("p4"), Expression.prop("p5"),
      Expression.prop("p6"), Expression.prop("p7")
  );
  // 2-conjunction over p8, p9: P≈1/4. Leaves many non-goal states (stresses the Classic).
  static final Expression GOAL_CONDITION = Expression.and(
      Expression.prop("p8"), Expression.prop("p9")
  );
  static final Expression QUERY = Expression.kh(INITIAL_CONDITION, GOAL_CONDITION);

  static final long[] SEEDS      = {1L, 2L, 3L, 4L, 5L};
  static final long   TIMEOUT_MS = 60_000;
  static final Path   DEFAULT_OUTPUT = Path.of("extras/benchmarks/random.csv");

  // ---------------------------------------------------------------------------
  // Scenarios

  record Scenario(String stage, String paramName, String paramValue,
                  int minNodes, int edgeDensity, int initStates,
                  int witnessLen, int witnessCount, boolean deterministic) {

    LtsGeneratorConfig config(long seed) {
      return new LtsGeneratorConfig(
          deterministic,
          minNodes,
          minNodes * edgeDensity,
          ACTION_COUNT,
          PROPOSITION_COUNT,
          INITIAL_CONDITION,
          GOAL_CONDITION,
          initStates,
          GOAL_STATE_COUNT,
          witnessCount,
          witnessLen,
          seed
      );
    }
  }

  /** Factory: every scenario starts from the baseline; each helper varies ONE parameter. */
  private static Scenario stageA(int minNodes) {
    return new Scenario("A", "min_nodes", String.valueOf(minNodes),
        minNodes, BASE_EDGE_DENSITY, BASE_INIT_COUNT,
        BASE_WITNESS_LEN, BASE_WITNESS_COUNT, BASE_DETERMINISTIC);
  }
  private static Scenario stageB(int initStates) {
    return new Scenario("B", "init_states", String.valueOf(initStates),
        BASE_MIN_NODES, BASE_EDGE_DENSITY, initStates,
        BASE_WITNESS_LEN, BASE_WITNESS_COUNT, BASE_DETERMINISTIC);
  }
  private static Scenario stageC(int witnessLen) {
    return new Scenario("C", "witness_len", String.valueOf(witnessLen),
        BASE_MIN_NODES, BASE_EDGE_DENSITY, BASE_INIT_COUNT,
        witnessLen, BASE_WITNESS_COUNT, BASE_DETERMINISTIC);
  }
  private static Scenario stageD(int density) {
    return new Scenario("D", "edge_density", String.valueOf(density),
        BASE_MIN_NODES, density, BASE_INIT_COUNT,
        BASE_WITNESS_LEN, BASE_WITNESS_COUNT, BASE_DETERMINISTIC);
  }
  private static Scenario stageE(int witnessCount) {
    return new Scenario("E", "witness_count", String.valueOf(witnessCount),
        BASE_MIN_NODES, BASE_EDGE_DENSITY, BASE_INIT_COUNT,
        BASE_WITNESS_LEN, witnessCount, BASE_DETERMINISTIC);
  }
  static List<Scenario> scenarios() {
    List<Scenario> list = new ArrayList<>();

    // Stage A — LTS size (constant density = 3)
    for (int n : new int[]{100, 200, 400, 800, 1600}) list.add(stageA(n));

    // Stage B — initial states (the main hypothesis)
    for (int k : new int[]{1, 2, 3, 4, 6, 8}) list.add(stageB(k));

    // Stage C — witness plan length
    for (int l : new int[]{3, 5, 8, 12}) list.add(stageC(l));

    // Stage D — LTS density (edges per state)
    for (int d : new int[]{2, 3, 4, 6, 10}) list.add(stageD(d));

    // Stage E — number of implanted witnesses
    for (int w : new int[]{1, 3, 6}) list.add(stageE(w));

    return list;
  }

  // ---------------------------------------------------------------------------

  public static void main(String[] args) throws Exception {
    Path output = DEFAULT_OUTPUT;
    boolean directOnly  = false;
    boolean classicOnly = false;
    String  stage = null;  // if non-null, restrict to this single stage
    for (int i = 0; i < args.length; i++) {
      String arg = args[i];
      if (arg.equals("--direct-only"))  directOnly  = true;
      else if (arg.equals("--classic-only")) classicOnly = true;
      else if (arg.equals("--stage") && i + 1 < args.length) stage = args[++i];
      else output = Path.of(arg);
    }
    String[] checkers = directOnly  ? new String[]{"direct"}
                      : classicOnly ? new String[]{"classic"}
                      :               new String[]{"classic", "direct"};
    final String stageFinal = stage;
    List<Scenario> scs = scenarios().stream()
        .filter(sc -> stageFinal == null || sc.stage.equals(stageFinal))
        .collect(java.util.stream.Collectors.toList());
    System.err.println("Running " + scs.size() + " scenarios"
        + (stageFinal != null ? " (stage " + stageFinal + ")" : "")
        + " with checkers " + java.util.Arrays.toString(checkers));

    Files.createDirectories(output.toAbsolutePath().getParent());

    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(output, StandardCharsets.UTF_8))) {
      writer.println(csvHeader());
      for (Scenario sc : scs) {
        for (long seed : SEEDS) {
          runPair(writer, sc, seed, checkers);
        }
      }
    }
    System.err.println("Done.");
  }

  private static void runPair(PrintWriter writer, Scenario sc, long seed, String[] checkers) {
    GeneratedLts gen;
    try {
      gen = new RandomLtsGenerator(sc.config(seed)).generate();
    } catch (Exception e) {
      System.err.printf("[ERROR] generate(stage=%s %s=%s seed=%d): %s%n",
          sc.stage, sc.paramName, sc.paramValue, seed, e.getMessage());
      return;
    }

    int ltsNodes      = gen.lts().getSize().key();
    int ltsEdges      = gen.lts().getSize().value();
    int actualInitial = gen.initialStates().size();
    Integer pointed   = gen.lts().getStates().iterator().next();

    for (String checker : checkers) {
      Row row = runChecker(checker, gen, pointed, sc, seed,
                           ltsNodes, ltsEdges, actualInitial);
      writer.println(row.toCsv());
      writer.flush();
      System.err.printf("[%s] stage=%s %s=%-5s seed=%d %-7s -> %s (gen=%d, t=%dms)%n",
          now(), sc.stage, sc.paramName, sc.paramValue, seed, checker,
          row.status, row.khGenNodes, row.timeMs);
    }
  }

  private static Row runChecker(
      String name, GeneratedLts gen, Integer pointed, Scenario sc, long seed,
      int ltsNodes, int ltsEdges, int actualInitial) {

    Row row = new Row(sc, seed, ltsNodes, ltsEdges, actualInitial, name);
    long deadline = System.currentTimeMillis() + TIMEOUT_MS;
    DeadlineGraphLogger khLog = new DeadlineGraphLogger("KH", deadline);
    long start = System.currentTimeMillis();

    try {
      boolean passed;
      try (var scope = LoggerContext.withLogger(khLog)) {
        passed = name.equals("classic")
            ? new ClassicAutomataModelChecker<>(gen.lts(), pointed, false).check(QUERY)
            : new DirectAutomataModelChecker<>(gen.lts(), pointed).check(QUERY);
      }
      row.khGenNodes   = khLog.getGeneratedNodesCant();
      row.khGenEdges   = khLog.getGeneratedEdgesCant();
      row.khFinalNodes = khLog.getNodesCant();
      row.khFinalEdges = khLog.getEdgesCant();
      row.passed       = passed ? "true" : "false";
      row.status       = "OK";
    } catch (TimeoutSignal e) {
      row.khGenNodes = khLog.getGeneratedNodesCant();
      row.khGenEdges = khLog.getGeneratedEdgesCant();
      row.status     = "TIMEOUT";
    } catch (Throwable e) {
      System.err.println("ERROR: " + e);
      row.status = "ERROR";
    } finally {
      row.timeMs = Math.min(System.currentTimeMillis() - start, TIMEOUT_MS);
    }
    return row;
  }

  // ---------------------------------------------------------------------------

  static String csvHeader() {
    return "stage,varied_param,varied_value,seed,lts_nodes,lts_edges,actual_initial_states,"
        + "checker,kh_gen_nodes,kh_gen_edges,kh_final_nodes,kh_final_edges,passed,time_ms,status";
  }

  static final class Row {
    final Scenario sc;
    final long   seed;
    final int    ltsNodes, ltsEdges, actualInitial;
    final String checker;
    int    khGenNodes, khGenEdges, khFinalNodes, khFinalEdges;
    String passed = "";
    String status = "ERROR";
    long   timeMs = 0;

    Row(Scenario sc, long seed, int ltsNodes, int ltsEdges, int actualInitial, String checker) {
      this.sc            = sc;
      this.seed          = seed;
      this.ltsNodes      = ltsNodes;
      this.ltsEdges      = ltsEdges;
      this.actualInitial = actualInitial;
      this.checker       = checker;
    }

    String toCsv() {
      return sc.stage + "," + sc.paramName + "," + sc.paramValue + "," + seed + ","
          + ltsNodes + "," + ltsEdges + "," + actualInitial + ","
          + checker + "," + khGenNodes + "," + khGenEdges + ","
          + khFinalNodes + "," + khFinalEdges + ","
          + "\"" + passed + "\"," + timeMs + ",\"" + status + "\"";
    }
  }

  private static String now() {
    return java.time.LocalTime.now().toString().substring(0, 8);
  }
}
