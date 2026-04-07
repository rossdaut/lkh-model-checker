package lkh.cli;

import lkh.expression.Expression;
import lkh.lts.LTS;
import lkh.lts.builder.PDDL;
import lkh.modelchecker.AutomataModelChecker;
import logger.GraphLogger;
import logger.LoggerContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

public class PddlChecker {

  public static int run(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage: PddlChecker <domain.pddl> <problem.pddl>");
      return 1;
    }

    String domain = args[0].trim();
    String problem = args[1].trim();

    File fDomain = new File(domain);
    if (!fDomain.exists()) {
      System.err.println("Error: File \"" + domain + "\" does not exist.");
      return 2;
    }

    File fProblem = new File(problem);
    if (!fProblem.exists()) {
      System.err.println("Error: File \"" + problem + "\" does not exist.");
      return 2;
    }

    try {
      System.out.println("[1/4] Loading PDDL files...");
      PDDL pddl = new PDDL(domain, problem);

      System.out.println("[2/4] Building LTS...");
      // Build LTS while logging
      GraphLogger ltsLogger = new GraphLogger("LTS");
      LTS<Integer, String> lts;
      try (var scope = LoggerContext.withLogger(ltsLogger)) {
        lts = pddl.buildLTS();
      }

      ltsLogger.setSize(lts.getSize());
      ltsLogger.printLog();

      System.out.println("[3/4] Building KH automaton and checking expression...");
      // Prepare model checker
      AutomataModelChecker<Integer, String> mc = new AutomataModelChecker<>(lts, pddl.getInitialState());

      // Build and check KH expression while logging KH automaton generation
      Expression initial = pddl.getInitialExpression();
      Expression goal = pddl.getGoalExpression();
      Expression kh = Expression.kh(initial, goal);

      GraphLogger khLogger = new GraphLogger("KH Automaton");
      boolean result;
      try (var scope = LoggerContext.withLogger(khLogger)) {
        result = mc.check(kh);
      }

      khLogger.printLog();

      System.out.println("[4/4] Final result:");
      String message = result ? "KH-Expression holds (:" : "KH-Expression fails :(";
      System.out.println(message);

      if (result) {
        Iterator<List<String>> wit = mc.witnesses(initial, goal, 10);
        if (wit.hasNext()) {
          System.out.println("Witness (first): " + wit.next());
        }
      }

      return 0;
    } catch (FileNotFoundException e) {
      System.err.println("File not found: " + e.getMessage());
      return 3;
    } catch (Exception e) {
      e.printStackTrace();
      return 4;
    }
  }

  public static void main(String[] args) {
    System.exit(run(args));
  }
}
