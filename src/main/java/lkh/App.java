package lkh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;
import lkh.dot.DotReader;
import lkh.dot.DotWriter;
import lkh.expression.Expression;
import lkh.expression.ExpressionType;
import lkh.expression.parser.ParseException;
import lkh.generator.GeneratedLts;
import lkh.generator.GeneratorCli;
import lkh.lts.LTS;
import lkh.lts.builder.pddl.ActionSelectionStrategy;
import lkh.lts.builder.pddl.DefaultActionSelectionStrategy;
import lkh.lts.builder.pddl.PDDL;
import lkh.modelchecker.ClassicAutomataModelChecker;
import lkh.modelchecker.DirectAutomataModelChecker;
import lkh.modelchecker.ModelChecker;
import lkh.por.StratifiedActionSelectionStrategy;
import lkh.por.StrongStubbornSetActionSelectionStrategy;
import lkh.utils.Pair;
import logger.GraphLogger;
import logger.LoggerContext;

public class App {
  private static final String PDDL_EXAMPLES_DIR = "src/main/resources/pddl-examples/";

  private final ConsoleUi ui = new ConsoleUi();
  private final AppSession session = new AppSession();
  private final Deque<Menu> menuHistory = new ArrayDeque<>();

  private Menu currentMenu;
  private Menu homeMenu;
  private Menu pddlSourcesMenu;
  private Menu pddlExamplesMenu;
  private Menu dotSourcesMenu;
  private Menu porMenu;
  private Menu checkerMenu;
  private Menu classicMenu;
  private Menu pddlSessionMenu;
  private Menu dotSessionMenu;

  private String[] pendingPddlFiles;
  private boolean running = true;

  public App() {
    initializeMenus();
    currentMenu = homeMenu;
  }

  public static void main(String[] args) {
    new App().run();
  }

  public void run() {
    while (running) {
      try {
        handleMenu(currentMenu);
      } catch (Throwable t) {
        ui.showErrorAndPause(t);
      }
    }
  }

  private void initializeMenus() {
    homeMenu = new Menu(
        "Main menu",
        "Option: ",
        new MenuOption("Exit", this::exitApp),
        new MenuOption("Load LTS from PDDL", () -> openMenu(pddlSourcesMenu)),
        new MenuOption("Load LTS from DOT", () -> openMenu(dotSourcesMenu)));

    pddlSourcesMenu = new Menu(
        "Load PDDL",
        "PDDL source: ",
        new MenuOption("Back", this::goBack),
        new MenuOption("Manual (domain/problem files)", this::selectManualPddlFiles),
        new MenuOption("Included PDDL examples", () -> openMenu(pddlExamplesMenu)));

    pddlExamplesMenu = new Menu(
        "PDDL examples",
        "Example: ",
        new MenuOption("Back", this::goBack),
        new MenuOption("Tire", () -> selectPddlFiles(PDDL_EXAMPLES_DIR + "ia-modern-aproach/tire/domain.pddl", PDDL_EXAMPLES_DIR + "ia-modern-aproach/tire/problem.pddl")),
        new MenuOption("Logistics", () -> selectPddlFiles(PDDL_EXAMPLES_DIR + "ia-modern-aproach/logistics/domain.pddl", PDDL_EXAMPLES_DIR + "ia-modern-aproach/logistics/problem.pddl")));

    dotSourcesMenu = new Menu(
        "Load DOT",
        "DOT source: ",
        new MenuOption("Back", this::goBack),
        new MenuOption(".dot file", this::loadDot),
        new MenuOption("Generate from configuration", this::loadGeneratedDot));

    porMenu = new Menu(
        "Select POR",
        "POR: ",
        new MenuOption("Back", this::goBack),
        new MenuOption(PorMode.NONE.label(), () -> loadPddlLts(PorMode.NONE)),
        new MenuOption(PorMode.STRATIFIED.label(), () -> loadPddlLts(PorMode.STRATIFIED)),
        new MenuOption(PorMode.STRONG_STUBBORN_SETS.label(), () -> loadPddlLts(PorMode.STRONG_STUBBORN_SETS)));

    checkerMenu = new Menu(
        "Change checker",
        "Checker: ",
        new MenuOption("Back", this::goBack),
        new MenuOption( ModelCheckerMode.DIRECT.label(), () -> updateChecker(ModelCheckerMode.DIRECT)),
        new MenuOption("Classic (Fervari)", () -> openMenu(classicMenu)));

    classicMenu = new Menu(
        "Classic (Fervari)",
        "Mode: ",
        new MenuOption("Back", this::goBack),
        new MenuOption(ModelCheckerMode.CLASSIC.label(), () -> updateChecker(ModelCheckerMode.CLASSIC)),
        new MenuOption(ModelCheckerMode.CLASSIC_MINIMIZED.label(), () -> updateChecker(ModelCheckerMode.CLASSIC_MINIMIZED)));

    pddlSessionMenu = sessionMenu(true);
    dotSessionMenu = sessionMenu(false);
  }

  private Menu sessionMenu(boolean includeGoalCheck) {
    List<MenuOption> options = new ArrayList<>();
    options.add(new MenuOption("Exit", this::exitApp));
    if (includeGoalCheck) {
      options.add(new MenuOption("Check problem goal", this::checkGoal));
    }
    options.add(new MenuOption("Check expression", this::checkExpression));
    options.add(new MenuOption("Change checker", () -> openMenu(checkerMenu)));
    options.add(new MenuOption("Export LTS to .dot", this::exportLts));
    options.add(new MenuOption("Simulate", this::simulateCurrent));
    options.add(new MenuOption("Clear LTS and return to main menu", this::clearLoadedData));
    return new Menu("Main menu", "Option: ", options.toArray(MenuOption[]::new));
  }

  private void handleMenu(Menu menu) throws Exception {
    while (true) {
      showPage(menu.title(), menu.displayLines());
      MenuOption selectedOption = menu.optionFromInput(ui.readInput(menu.prompt()));
      if (selectedOption != null) {
        selectedOption.action().run();
        return;
      }
    }
  }

  private void openMenu(Menu menu) {
    if (currentMenu != null) {
      menuHistory.push(currentMenu);
    }
    currentMenu = menu;
  }

  private void setRootMenu(Menu menu) {
    menuHistory.clear();
    currentMenu = menu;
  }

  private void goBack() {
    if (!menuHistory.isEmpty()) {
      currentMenu = menuHistory.pop();
    }
  }

  private void exitApp() {
    running = false;
  }

  private void selectManualPddlFiles() {
    showPage("Load PDDL", "Load files manually");
    selectPddlFiles(
        ui.readExistingFilename("Domain file: "),
        ui.readExistingFilename("Problem file: "));
  }

  private void selectPddlFiles(String domain, String problem) {
    pendingPddlFiles = new String[] {domain, problem};
    openMenu(porMenu);
  }

  private void loadPddlLts(PorMode porMode) throws FileNotFoundException {
    if (pendingPddlFiles == null) {
      throw new IllegalStateException("No PDDL files selected.");
    }

    try {
      showPage("Loading PDDL...");
      ActionSelectionStrategy strategy = porMode.create();
      PDDL parser = new PDDL(pendingPddlFiles[0], pendingPddlFiles[1], strategy);
      GraphLogger logger = new GraphLogger("LTS");
      LTS<Integer, String> builtLts;
      try (var scope = LoggerContext.withLogger(logger)) {
        builtLts = parser.buildLTS();
      }
      logger.setSize(builtLts.getSize());
      System.out.println();
      logger.printLog();

      session.activate(
          builtLts,
          parser,
          null,
          porMode,
          parser.getInitialState(),
          pendingPddlFiles[0],
          pendingPddlFiles[1]);
      setRootMenu(pddlSessionMenu);
      pendingPddlFiles = null;
      ui.pause();
    } catch (Throwable t) {
      clearLoadedData();
      throw t;
    }
  }

  private void loadDot() throws FileNotFoundException {
    try {
      showPage("Load DOT");
      String filename = ui.readExistingFilename("LTS .dot file: ");

      showPage("Loading LTS from DOT...");
      GraphLogger logger = new GraphLogger("LTS");
      LTS<String, String> loadedLts;
      try (var scope = LoggerContext.withLogger(logger)) {
        loadedLts = DotReader.readLTS(filename);
      }
      logger.setSize(loadedLts.getSize());
      System.out.println();
      logger.printLog();

      session.activate(
          loadedLts,
          null,
          null,
          null,
          defaultPointedState(loadedLts),
          filename,
          null);
      setRootMenu(dotSessionMenu);
      System.out.println("Default pointed state: " + session.pointedState());
    } catch (Throwable t) {
      clearLoadedData();
      throw t;
    }
  }

  private void loadGeneratedDot() throws IOException, ParseException {
    try {
      showPage("Generate DOT");
      String configFilename = ui.readExistingFilename("Configuration file: ");
      GeneratedLts generated = GeneratorCli.run(Path.of(configFilename));
      session.activate(
          generated.lts(),
          null,
          generated,
          null,
          generated.initialStates().iterator().next(),
          configFilename,
          null);
      setRootMenu(dotSessionMenu);
    } catch (Throwable t) {
      clearLoadedData();
      throw t;
    }
  }

  private void updateChecker(ModelCheckerMode mode) {
    session.selectChecker(mode);
    showPage("Checker changed to: " + mode.label());
    ui.pause();
    setRootMenu(currentSessionMenu());
  }

  private void exportLts() {
    showPage("Export LTS to .dot");
    String filename = ui.readFilename("Output .dot file: ");
    DotWriter.writeLTS(session.lts(), filename);
    showPage("LTS exported to: " + filename);
  }

  private void checkExpression() throws ParseException {
    showPage("Check expression");
    String expressionText = ui.readInput("Expression: ");
    Expression expression = Expression.of(expressionText);

    showPage("Checking expression: " + expressionText);
    boolean result = check(expression);
    if (!containsKh(expression)) {
      System.out.println("Checked at pointed state " + session.pointedState() + ".");
    }
    System.out.println(result ? "The expression holds :)" : "The expression does not hold :(");

    if (result && expression.getTokenType() == ExpressionType.KH) {
      showWitnesses(expression.getLeft(), expression.getRight());
    }
    ui.pause();
  }

  private void checkGoal() {
    showPage("Checking problem goal...");
    System.out.println("Goal: " + session.pddlParser().getGoalExpression());

    Expression initial = session.pddlParser().getInitialExpression();
    Expression goal = session.pddlParser().getGoalExpression();
    boolean result = check(Expression.kh(initial, goal));

    System.out.println();
    System.out.println(result ? "The goal is reachable via KH." : "The goal is not reachable via KH.");
    if (result) {
      showWitnesses(initial, goal);
    }
    ui.pause();
  }

  private boolean check(Expression expression) {
    GraphLogger logger = new GraphLogger("KH Automaton");
    boolean result;
    try (var scope = LoggerContext.withLogger(logger)) {
      result = session.modelChecker().check(expression);
    }
    System.out.println();
    logger.printLog();
    return result;
  }

  private void showWitnesses(Expression init, Expression end) {
    System.out.println();
    if (startsWithIgnoreCase(ui.readInput("Show witnesses? (Y/n): "), "n")) {
      return;
    }

    Iterator<List<String>> witnesses = session.modelChecker().witnesses(init, end, 100);
    while (witnesses.hasNext()) {
      System.out.println(witnesses.next());
      System.out.println();
      if (startsWithIgnoreCase(ui.readInput("Next? (y/n): "), "n")) {
        return;
      }
    }
  }

  private void simulateCurrent() {
    if (session.generatedLts() != null) {
      Integer selectedInitialState = chooseGeneratedInitialState(session.generatedLts());
      if (selectedInitialState == null) {
        return;
      }
      session.setPointedState(selectedInitialState);
      session.selectChecker(ModelCheckerMode.DIRECT);
    }
    simulate("Simulation", session.pointedState(), session.lts(), session.goalPredicate());
  }

  private <S> void simulate(String title, S currentState, LTS<S, String> currentLts, Predicate<S> isGoal) {
    showPage(title);

    while (true) {
      String prefix = isGoal.test(currentState) ? "[GOAL] - " : "";
      System.out.println("Current state: " + prefix + currentLts.toString(currentState));

      List<String> actions = sorted(currentLts.getActions(currentState));
      if (actions.isEmpty()) {
        System.out.println("No actions available. Simulation finished.");
        break;
      }

      printActions(actions);
      String input = ui.readInput("Choose an action: ");
      if (startsWithIgnoreCase(input, "x")) {
        break;
      }

      Integer action = parseMenuIndex(input, actions.size());
      if (action == null) {
        System.out.println("Invalid option.");
        continue;
      }

      List<S> targets = sorted(currentLts.targets(currentState, actions.get(action)));
      Integer target = chooseTarget(actions.get(action), targets, currentLts);
      if (target != null) {
        currentState = targets.get(target);
      }
    }
    ui.pause();
  }

  private void clearLoadedData() {
    session.clear();
    pendingPddlFiles = null;
    setRootMenu(homeMenu);
  }

  private String defaultPointedState(LTS<String, String> currentLts) {
    if (currentLts.getStates().isEmpty()) {
      throw new IllegalArgumentException("The loaded LTS has no states.");
    }
    return currentLts.containsState("0") ? "0" : sorted(currentLts.getStates()).get(0);
  }

  private boolean containsKh(Expression expression) {
    return expression != null
        && (expression.getTokenType() == ExpressionType.KH
            || containsKh(expression.getLeft())
            || containsKh(expression.getRight()));
  }

  private void printActions(List<String> actions) {
    System.out.println("Available actions:");
    for (int i = 0; i < actions.size(); i++) {
      System.out.printf("\t%d. %s%n", i + 1, actions.get(i));
    }
    System.out.println("\tX. End simulation");
  }

  private <S> Integer chooseTarget(String action, List<S> targets, LTS<S, String> currentLts) {
    if (targets.isEmpty()) {
      System.out.println("The action has no targets.");
      return null;
    }
    if (targets.size() == 1) {
      return 0;
    }

    System.out.println("Nondeterminism detected:");
    for (int i = 0; i < targets.size(); i++) {
      System.out.printf("\t%d. %s -> %s%n", i + 1, action, currentLts.toString(targets.get(i)));
    }
    return parseMenuIndex(ui.readInput("Choose the target state: "), targets.size());
  }

  private Integer parseMenuIndex(String input, int size) {
    try {
      int option = Integer.parseInt(input);
      return option >= 1 && option <= size ? option - 1 : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private boolean startsWithIgnoreCase(String value, String prefix) {
    return value != null && !value.isEmpty() && value.toLowerCase().startsWith(prefix.toLowerCase());
  }

  private <T> List<T> sorted(Iterable<T> values) {
    List<T> sorted = new ArrayList<>();
    for (T value : values) {
      sorted.add(value);
    }
    sorted.sort(Comparator.comparing(Object::toString));
    return sorted;
  }

  private Menu currentSessionMenu() {
    return session.pddlWasLoaded() ? pddlSessionMenu : dotSessionMenu;
  }

  private Integer chooseGeneratedInitialState(GeneratedLts generated) {
    List<Integer> initialStates = sorted(generated.initialStates());
    while (true) {
      String[] lines = new String[initialStates.size() + 1];
      for (int i = 0; i < initialStates.size(); i++) {
        lines[i] = (i + 1) + ". " + generated.lts().toString(initialStates.get(i));
      }
      lines[initialStates.size()] = "0. Return to main menu";
      showPage("Select initial state", lines);

      String input = ui.readInput("Initial state: ");
      if ("0".equals(input)) {
        return null;
      }

      Integer option = parseMenuIndex(input, initialStates.size());
      if (option != null) {
        return initialStates.get(option);
      }
    }
  }

  private enum ModelCheckerMode {
    DIRECT("Direct (custom algorithm)") {
      @Override
      ModelChecker<Object, String> create(LTS<Object, String> lts, Object pointedState) {
        return new DirectAutomataModelChecker<>(lts, pointedState);
      }
    },
    CLASSIC("Classic (Fervari, without minimization)") {
      @Override
      ModelChecker<Object, String> create(LTS<Object, String> lts, Object pointedState) {
        return new ClassicAutomataModelChecker<>(lts, pointedState, false);
      }
    },
    CLASSIC_MINIMIZED("Classic (Fervari, with minimization)") {
      @Override
      ModelChecker<Object, String> create(LTS<Object, String> lts, Object pointedState) {
        return new ClassicAutomataModelChecker<>(lts, pointedState, true);
      }
    };

    private final String label;

    ModelCheckerMode(String label) {
      this.label = label;
    }

    abstract ModelChecker<Object, String> create(LTS<Object, String> lts, Object pointedState);

    private String label() {
      return label;
    }
  }

  private enum PorMode {
    NONE("None") {
      @Override
      ActionSelectionStrategy create() {
        return new DefaultActionSelectionStrategy();
      }
    },
    STRATIFIED("Stratified") {
      @Override
      ActionSelectionStrategy create() {
        return new StratifiedActionSelectionStrategy();
      }
    },
    STRONG_STUBBORN_SETS("Strong Stubborn Sets (SSS)") {
      @Override
      ActionSelectionStrategy create() {
        return new StrongStubbornSetActionSelectionStrategy();
      }
    };

    private final String label;

    PorMode(String label) {
      this.label = label;
    }

    abstract ActionSelectionStrategy create();

    private String label() {
      return label;
    }
  }

  @FunctionalInterface
  private interface MenuAction {
    void run() throws Exception;
  }

  private record Menu(String title, String prompt, MenuOption... options) {
    private MenuOption optionFromInput(String input) {
      if ("0".equals(input)) {
        return options[0];
      }

      try {
        int option = Integer.parseInt(input);
        return option >= 1 && option < options.length ? options[option] : null;
      } catch (NumberFormatException e) {
        return null;
      }
    }

    private String[] displayLines() {
      String[] lines = new String[options.length];
      for (int i = 1; i < options.length; i++) {
        lines[i - 1] = i + ". " + options[i].label();
      }
      lines[options.length - 1] = "0. " + options[0].label();
      return lines;
    }
  }

  private record MenuOption(String label, MenuAction action) {}

  private static final class AppSession {
    private LTS<Object, String> lts;
    private PDDL pddlParser;
    private GeneratedLts generatedLts;
    private ModelChecker<Object, String> modelChecker;
    private ModelCheckerMode checkerMode = ModelCheckerMode.DIRECT;
    private PorMode porMode;
    private Object pointedState;
    private String mainSource;
    private String secondarySource;

    private <State> void activate(
        LTS<State, String> loadedLts,
        PDDL parser,
        GeneratedLts generated,
        PorMode selectedPorMode,
        Object initialPointedState,
        String primarySource,
        String otherSource) {
      lts = castLts(loadedLts);
      pddlParser = parser;
      generatedLts = generated;
      porMode = selectedPorMode;
      pointedState = initialPointedState;
      mainSource = primarySource;
      secondarySource = otherSource;
      selectChecker(ModelCheckerMode.DIRECT);
    }

    private void clear() {
      lts = null;
      pddlParser = null;
      generatedLts = null;
      modelChecker = null;
      porMode = null;
      pointedState = null;
      mainSource = null;
      secondarySource = null;
      checkerMode = ModelCheckerMode.DIRECT;
    }

    private void selectChecker(ModelCheckerMode mode) {
      checkerMode = mode;
      modelChecker = hasLts() ? mode.create(lts, pointedState) : null;
    }

    private boolean hasLts() {
      return lts != null;
    }

    private boolean pddlWasLoaded() {
      return pddlParser != null;
    }

    private LTS<Object, String> lts() {
      return lts;
    }

    private PDDL pddlParser() {
      return pddlParser;
    }

    private GeneratedLts generatedLts() {
      return generatedLts;
    }

    private ModelChecker<Object, String> modelChecker() {
      return modelChecker;
    }

    private Object pointedState() {
      return pointedState;
    }

    private void setPointedState(Object pointedState) {
      this.pointedState = pointedState;
    }

    private String sourceSummary() {
      return secondarySource == null
          ? shortName(mainSource)
          : shortName(mainSource) + " / " + shortName(secondarySource);
    }

    private List<String> summaryLines() {
      if (!hasLts()) {
        return List.of("Current session: no LTS loaded");
      }

      Pair<Integer, Integer> size = lts.getSize();
      List<String> lines = new ArrayList<>();
      lines.add("LTS loaded");
      lines.add("Source: " + sourceSummary());
      lines.add("Checker: " + checkerMode.label());
      lines.add("Size: " + size.key() + " states and " + size.value() + " transitions");
      if (pddlWasLoaded()) {
        lines.add("POR: " + (porMode == null ? "-" : porMode.label()));
      }
      return lines;
    }

    private Predicate<Object> goalPredicate() {
      if (pddlWasLoaded()) {
        return state -> modelChecker.check(pddlParser.getGoalExpression(), state);
      }
      if (generatedLts != null) {
        return state -> generatedLts.goalStates().contains(state);
      }
      return state -> false;
    }

    @SuppressWarnings("unchecked")
    private static <State> LTS<Object, String> castLts(LTS<State, String> sourceLts) {
      return (LTS<Object, String>) sourceLts;
    }

    private static String shortName(String path) {
      return path == null || path.isBlank() ? "-" : new File(path).getName();
    }
  }

  private void showPage(String sectionTitle, String... lines) {
    ui.showPage(session.summaryLines(), sectionTitle, lines);
  }

  private static final class ConsoleUi {
    private static final String TITLE = "LKH Model Checker";
    private static final String SEPARATOR = "------------------------------";

    private final Scanner scanner = new Scanner(System.in);

    private void showPage(List<String> sessionLines, String sectionTitle, String... lines) {
      clearScreen();
      System.out.println("=== " + TITLE + " ===");
      System.out.println(SEPARATOR);
      for (String line : sessionLines) {
        System.out.println(line);
      }
      System.out.println();
      System.out.println(SEPARATOR);
      if (sectionTitle != null && !sectionTitle.isBlank()) {
        System.out.println(sectionTitle);
        System.out.println(SEPARATOR);
      }
      for (String line : lines) {
        System.out.println(line);
      }
      System.out.println();
    }

    private String readInput(String prompt) {
      return readLine(prompt);
    }

    private String readExistingFilename(String prompt) {
      while (true) {
        String filename = readFilename(prompt);
        if (new File(filename).exists()) {
          return filename;
        }
        System.out.println("File not found: " + filename);
      }
    }

    private String readFilename(String prompt) {
      return readLine(prompt);
    }

    private void pause() {
      System.out.println();
      readLine("Press Enter to continue...");
    }

    private void showErrorAndPause(Throwable t) {
      System.out.println("Error: " + t.getMessage());
      pause();
    }

    private void clearScreen() {
      System.out.print("\033[3J\033[H\033[2J");
      System.out.flush();
    }

    private String readLine(String prompt) {
      System.out.print(prompt);
      return scanner.nextLine().trim();
    }
  }
}
