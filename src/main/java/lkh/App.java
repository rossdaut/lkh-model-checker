package lkh;

import java.io.FileNotFoundException;
import java.util.*;

import lkh.dot.DotWriter;
import lkh.expression.Expression;
import lkh.expression.parser.ParseException;
import lkh.expression.ExpressionType;
import lkh.lts.LTS;
import lkh.modelchecker.ModelChecker;
import lkh.pddl.PDDL;

public class App {
  private LTS<Integer, String> lts;
  private PDDL pddlParser;
  private ModelChecker<Integer, String> modelChecker;
  private Scanner scanner = new Scanner(System.in);

  public void printMenu() {
    System.out.println("1. Load LTS from PDDL");
    System.out.println("2. Write LTS (to dot file)");
    System.out.println("3. Check KH expression");
    System.out.println("4. Check Goal expression");
    System.out.println("5. Start simulation");
    System.out.println("6. Toggle minimize");
    System.out.println("0. Exit");
  }

  public String getOption() {
    System.out.print("Enter option: ");
    return scanner.nextLine();
  }

  public void dispatchOption(String option) throws ParseException, FileNotFoundException {
    switch (option) {
      case "1":
        loadLTS();
        break;
      case "2":
        writeLTS();
        break;
      case "3":
        checkExpression();
        break;
      case "4":
        checkGoal();
        break;
      case "5":
        simulate();
        break;
      case "6":
        toggleMinimize();
        break;
      case "0":
        System.exit(0);
        break;
      default:
        System.out.println("Invalid option");
    }
  }

  private void loadLTS() throws FileNotFoundException {
    System.out.print("Enter domain filename: ");
    String domainFilename = scanner.nextLine();
    System.out.print("Enter problem filename: ");
    String problemFilename = scanner.nextLine();

    loadLTS(domainFilename, problemFilename);
  }

  protected void loadLTS(String domainFilename, String problemFilename) throws FileNotFoundException {
    pddlParser = new PDDL(domainFilename, problemFilename);
    System.out.println("Enable Partial Order Reduction? (y/n)");
    String input = scanner.nextLine();
    pddlParser.setReduce(input.toLowerCase().charAt(0) != 'n');

    lts = pddlParser.getLTS();
    modelChecker = new ModelChecker<>(lts, pddlParser.getInitialState());
  }

  private void writeLTS() {
    System.out.print("Enter output filename: ");
    String outputFilename = scanner.nextLine();

    writeLTS(outputFilename);
  }

  private void writeLTS(String outputFilename) {
    if (lts == null) {
      System.err.println("LTS not set (call loadLTS() first)");
      System.exit(1);
    }

    DotWriter.writeLTS(lts, outputFilename);
  }

  private void checkExpression() throws ParseException {
    if (lts == null) {
      System.err.println("LTS not set (call loadLTS() first)");
      System.exit(1);
    }

    System.out.print("Enter expression: ");
    String expression = scanner.nextLine();

    checkExpression(expression);
  }

  private void checkExpression(String expressionString) throws ParseException {
    Expression expression = Expression.of(expressionString);

    boolean result = modelChecker.check(expression);

    String message = result ? "KH-Expression holds (:" : "KH-Expression fails :(";
    System.out.println(message + "\n");

    if (result) {
      showWitnesses(expression);
    }
  }

  private void showWitnesses(Expression expression) {
    if (expression.getTokenType() != ExpressionType.KH)
      return;

    System.out.print("Get witnesses? (Y/n): ");
    String input = scanner.nextLine();
    if (input.toLowerCase().charAt(0) == 'n')
      return;

    System.out.println("Witnesses:");
    Iterator<List<String>> witnesses = modelChecker.witnesses(expression.getLeft(), expression.getRight(),100);
    boolean next = true;

    while (witnesses.hasNext() && next) {
      List<String> witness = witnesses.next();

      System.out.println(witness);
      System.out.print("Next? (y/n)");
      input = scanner.nextLine();
      if (!input.isEmpty() && input.toLowerCase().charAt(0) == 'n')
        next = false;
    }

  }

  private void checkGoal() throws ParseException {
    if (lts == null) {
      System.err.println("LTS not set (call loadLTS() first)");
      System.exit(1);
    }

    Expression initial = pddlParser.getInitialExpression();
    Expression goal = pddlParser.getGoalExpression();
    Expression kh = Expression.kh(initial, goal);

    boolean result = modelChecker.check(kh);

    String message = result ? "KH-Expression holds (:" : "KH-Expression fails :(";
    System.out.println(message + "\n");

    if (result) {
      showWitnesses(kh);
    }
  }

  private void simulate() {
    int currentState = pddlParser.getInitialState();
    int option = 0;

    while (true) {
      System.out.printf(
          "Current State: %s%s\n",
           isGoal(currentState) ? "[GOAL] - " : "",
          lts.toString(currentState));

      List<String> actions = new LinkedList<>(lts.getActions(currentState));
      System.out.println("Available actions: ");
      for (int i = 0; i < actions.size(); i++) {
        System.out.printf("\t%d: %s\n", i+1, actions.get(i));
      }
      System.out.println("\tX: Finish simulation");

      try {
        option = Integer.parseInt(getOption());
      } catch (NumberFormatException e) {
        break;
      }

      String action = actions.get(option-1);
      List<Integer> availableStates = new LinkedList<>(lts.targets(currentState, action));
      option = 1;
      if (availableStates.size() > 1) {
        System.out.println("Non determinism detected");
        System.out.println("Possible targets: ");
        for (int i = 0; i < availableStates.size(); i++) {
          System.out.printf("\t%d: %s -> %s\n", i+1, action, lts.toString(availableStates.get(i)));
        }

        option = Integer.parseInt(getOption());
      }

      currentState = availableStates.get(option-1);
    }
  }

  private void toggleMinimize() {
    modelChecker.setMinimize(!modelChecker.isMinimize());
  }

  private boolean isGoal(int state) {
    return modelChecker.check(pddlParser.getGoalExpression(), state);
  }

  public static void main(String[] args) {
    App app = new App();

    if (args.length != 0 && args.length != 2) {
      System.out.println("Usage: App [<domain filename> <problem filename>]");
      System.exit(1);
    }

    if (args.length == 2) {
      try {
        app.loadLTS(args[0], args[1]);
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    while (true) {
      app.printMenu();
      try {
        app.dispatchOption(app.getOption());
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
        break;
      }
    }
  }
}