package lkh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import lkh.lts.LTS;
import lkh.lts.builder.ActionSelectionStrategy;
import lkh.lts.builder.DefaultActionSelectionStrategy;
import lkh.lts.builder.PDDL;
import lkh.modelchecker.DirectAutomataModelChecker;
import lkh.modelchecker.ClassicAutomataModelChecker;
import lkh.modelchecker.ModelChecker;
import lkh.por.StratifiedActionSelectionStrategy;
import lkh.por.StrongStubbornSetActionSelectionStrategy;
import lkh.utils.Pair;
import logger.GraphLogger;
import logger.LoggerContext;
import org.jline.builtins.Completers.FileNameCompleter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

public class App {
  private static final String TITLE = "LKH Model Checker";
  private static final String SEPARATOR = "------------------------------";
  private static final String PDDL_EXAMPLES_DIR = "src/main/resources/pddl-examples/";

  private final Scanner scanner = new Scanner(System.in);
  private final Terminal terminal;
  private final LineReader lineReader;
  private final Deque<Menu> menuHistory = new ArrayDeque<>();

  private Menu currentMenu;
  private Menu homeMenu;
  private Menu pddlSourcesMenu;
  private Menu pddlExamplesMenu;
  private Menu porMenu;
  private Menu checkerMenu;
  private Menu pddlSessionMenu;
  private Menu dotSessionMenu;

  private LTS<Object, String> lts;
  private PDDL pddlParser;
  private ModelChecker<Object, String> modelChecker;
  private ActionSelectionStrategy porStrategy;
  private Object pointedState;
  private String mainSource;
  private String secondarySource;
  private String[] pendingPddlFiles;
  private boolean running = true;

  public App() {
    Terminal builtTerminal = null;
    LineReader builtLineReader = null;
    try {
      builtTerminal = TerminalBuilder.builder().system(true).build();
      builtLineReader = LineReaderBuilder.builder()
          .terminal(builtTerminal)
          .completer(new FileNameCompleter())
          .option(LineReader.Option.AUTO_FRESH_LINE, true)
          .option(LineReader.Option.ERASE_LINE_ON_FINISH, true)
          .build();
    } catch (IOException e) {
      System.err.println("Warning: File autocompletion not available: " + e.getMessage());
    }
    terminal = builtTerminal;
    lineReader = builtLineReader;
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
      } catch (Exception e) {
        System.out.println("Error: " + e.getMessage());
        pause();
      }
    }
  }

  private void initializeMenus() {
    homeMenu = new Menu(
        "Menu principal",
        "Opcion: ",
        new MenuOption("Salir", this::exitApp),
        new MenuOption("Cargar LTS desde PDDL", () -> openMenu(pddlSourcesMenu)),
        new MenuOption("Cargar LTS desde DOT", this::loadDot));

    pddlSourcesMenu = new Menu(
        "Cargar PDDL",
        "Fuente PDDL: ",
        new MenuOption("Volver", this::goBack),
        new MenuOption("Manual (archivos domain/problem)", this::selectManualPddlFiles),
        new MenuOption("Ejemplos PDDL incluidos", () -> openMenu(pddlExamplesMenu)));

    pddlExamplesMenu = new Menu(
        "Ejemplos PDDL",
        "Ejemplo: ",
        new MenuOption("Volver", this::goBack),
        new MenuOption(
            "Tire",
            () -> selectPddlFiles(
                PDDL_EXAMPLES_DIR + "tire-domain.pddl",
                PDDL_EXAMPLES_DIR + "tire-problem.pddl")),
        new MenuOption(
            "Logistics",
            () -> selectPddlFiles(
                PDDL_EXAMPLES_DIR + "logistics-domain.pddl",
                PDDL_EXAMPLES_DIR + "logistics-problem.pddl")));

    porMenu = new Menu(
        "Seleccionar POR",
        "POR: ",
        new MenuOption("Volver", this::goBack),
        new MenuOption("Ninguno", () -> loadPddlLts(new DefaultActionSelectionStrategy())),
        new MenuOption("Stratified", () -> loadPddlLts(new StratifiedActionSelectionStrategy())),
        new MenuOption("Strong Stubborn Sets (SSS)", () -> loadPddlLts(new StrongStubbornSetActionSelectionStrategy())));

    checkerMenu = new Menu(
        "Cambiar checker",
        "Checker: ",
        new MenuOption("Volver", this::goBack),
        new MenuOption("Direct (algoritmo propio)", this::selectDirectChecker),
        new MenuOption("Classic (Fervari)", this::selectClassicChecker));

    pddlSessionMenu = new Menu(
        "Menu principal",
        "Opcion: ",
        new MenuOption("Salir", this::exitApp),
        new MenuOption("Chequear goal del problema", this::checkGoal),
        new MenuOption("Chequear expresion", this::checkExpression),
        new MenuOption("Cambiar checker", () -> openMenu(checkerMenu)),
        new MenuOption("Exportar LTS a .dot", this::exportLts),
        new MenuOption("Simular", this::simulateCurrent),
        new MenuOption("Cargar otro LTS", this::clearLoadedData),
        new MenuOption("Limpiar LTS y volver al menu principal", this::clearLoadedData));

    dotSessionMenu = new Menu(
        "Menu principal",
        "Opcion: ",
        new MenuOption("Salir", this::exitApp),
        new MenuOption("Chequear expresion", this::checkExpression),
        new MenuOption("Cambiar checker", () -> openMenu(checkerMenu)),
        new MenuOption("Exportar LTS a .dot", this::exportLts),
        new MenuOption("Simular", this::simulateCurrent),
        new MenuOption("Cargar otro LTS", this::clearLoadedData),
        new MenuOption("Limpiar LTS y volver al menu principal", this::clearLoadedData));
  }

  private void handleMenu(Menu menu) throws Exception {
    while (true) {
      showPage(menu.title(), menu.displayLines());

      MenuOption selectedOption = menu.optionFromInput(readInput(menu.prompt()));
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
    if (menuHistory.isEmpty()) {
      return;
    }
    currentMenu = menuHistory.pop();
  }

  private void exitApp() {
    running = false;
  }

  private void selectManualPddlFiles() {
    showPage("Cargar PDDL", "Carga manual de archivos");

    selectPddlFiles(
        readExistingFilename("Archivo de dominio: "),
        readExistingFilename("Archivo de problema: "));
  }

  private void selectPddlFiles(String domain, String problem) {
    pendingPddlFiles = new String[] {domain, problem};
    openMenu(porMenu);
  }

  private void loadPddlLts(ActionSelectionStrategy selectedPorStrategy) throws FileNotFoundException {
    if (pendingPddlFiles == null) {
      throw new IllegalStateException("No hay archivos PDDL seleccionados.");
    }

    try {
      showPage("Cargando PDDL...");

      PDDL parser = new PDDL(pendingPddlFiles[0], pendingPddlFiles[1], selectedPorStrategy);
      GraphLogger logger = new GraphLogger("LTS");
      LTS<Integer, String> builtLts;
      try (var scope = LoggerContext.withLogger(logger)) {
        builtLts = parser.buildLTS();
      }
      logger.setSize(builtLts.getSize());
      logger.printLog();

      setPddl(parser, builtLts, selectedPorStrategy, pendingPddlFiles[0], pendingPddlFiles[1]);
      pendingPddlFiles = null;
      pause();
    } catch (Exception e) {
      pendingPddlFiles = null;
      setRootMenu(homeMenu);
      throw e;
    }
  }

  private void loadDot() throws FileNotFoundException {
    try {
      showPage("Cargar DOT");
      String filename = readExistingFilename("Archivo .dot del LTS: ");

      showPage("Cargando LTS desde DOT...");

      LTS<String, String> loadedLts = DotReader.readLTS(filename);
      setDot(loadedLts, filename, defaultPointedState(loadedLts));
      System.out.println("Estado apuntado por defecto: " + pointedState);
    } catch (Exception e) {
      setRootMenu(homeMenu);
      throw e;
    }
  }

  private void selectDirectChecker() {
    useDirectChecker();
    showPage("Checker actualizado a: " + checkerLabel());
    pause();
    goBack();
  }

  private void selectClassicChecker() {
    useClassicChecker();
    showPage("Checker actualizado a: " + checkerLabel());
    pause();
    goBack();
  }

  private void exportLts() {
    showPage("Exportar LTS a .dot");
    String filename = readFilename("Archivo de salida .dot: ");
    DotWriter.writeLTS(lts, filename);
    showPage("LTS exportado a: " + filename);
  }

  private void checkExpression() throws ParseException {
    List<String> lines = new ArrayList<>();
    showPage("Chequear expresion", lines.toArray(String[]::new));

    String expressionText = readInput("Expresion: ");
    Expression expression = Expression.of(expressionText);

    showPage("Chequeando expresion: " + expressionText);

    GraphLogger logger = new GraphLogger("KH Automaton");
    boolean result;
    try (var scope = LoggerContext.withLogger(logger)) {
      result = modelChecker.check(expression);
    }
    logger.printLog();

    if (!containsKh(expression)) {
      System.out.println("Chequeo sobre el estado apuntado " + pointedState + ".");
    }
    System.out.println(result ? "La expresion vale." : "La expresion no vale.");

    if (result && expression.getTokenType() == ExpressionType.KH) {
      showWitnesses(expression.getLeft(), expression.getRight());
    }

    pause();
  }

  private void checkGoal() {
    showPage("Chequeando goal del problema...");
    System.out.println("Goal: " + pddlParser.getGoalExpression());

    Expression initial = pddlParser.getInitialExpression();
    Expression goal = pddlParser.getGoalExpression();

    GraphLogger logger = new GraphLogger("KH Automaton");
    boolean result;
    try (var scope = LoggerContext.withLogger(logger)) {
      result = modelChecker.check(Expression.kh(initial, goal));
    }
    logger.printLog();

    System.out.println(result ? "El goal es alcanzable via KH." : "El goal no es alcanzable via KH.");
    if (result) {
      showWitnesses(initial, goal);
    }

    pause();
  }

  private void showWitnesses(Expression init, Expression end) {
    if (startsWithIgnoreCase(readInput("Mostrar witnesses? (Y/n): "), "n")) {
      return;
    }

    Iterator<List<String>> witnesses = modelChecker.witnesses(init, end, 100);
    while (witnesses.hasNext()) {
      System.out.println(witnesses.next());
      if (startsWithIgnoreCase(readInput("Siguiente? (y/n): "), "n")) {
        return;
      }
    }
  }

  private void simulateCurrent() {
    simulate("Simulacion", pointedState, lts, currentGoalPredicate());
  }

  private <S> void simulate(String title, S currentState, LTS<S, String> currentLts, Predicate<S> isGoal) {
    showPage(title);

    while (true) {
      String prefix = isGoal.test(currentState) ? "[GOAL] - " : "";
      System.out.println("Estado actual: " + prefix + currentLts.toString(currentState));

      List<String> actions = sorted(currentLts.getActions(currentState));
      if (actions.isEmpty()) {
        System.out.println("No hay acciones disponibles. Fin de la simulacion.");
        break;
      }

      printActions(actions);
      String input = readInput("Elegi una accion: ");
      if (startsWithIgnoreCase(input, "x")) {
        break;
      }

      Integer action = parseMenuIndex(input, actions.size());
      if (action == null) {
        System.out.println("Opcion invalida.");
        continue;
      }

      List<S> targets = sorted(currentLts.targets(currentState, actions.get(action)));
      Integer target = chooseTarget(actions.get(action), targets, currentLts);
      if (target != null) {
        currentState = targets.get(target);
      }
    }

    pause();
  }

  private void setPddl(
      PDDL parser,
      LTS<Integer, String> builtLts,
      ActionSelectionStrategy selectedPorStrategy,
      String domain,
      String problem) {
    lts = castLts(builtLts);
    pddlParser = parser;
    porStrategy = selectedPorStrategy;
    pointedState = parser.getInitialState();
    mainSource = domain;
    secondarySource = problem;
    useDirectChecker();
    setRootMenu(pddlSessionMenu);
  }

  private void setDot(LTS<String, String> loadedLts, String filename, String defaultPointedState) {
    lts = castLts(loadedLts);
    pddlParser = null;
    porStrategy = null;
    pointedState = defaultPointedState;
    mainSource = filename;
    secondarySource = null;
    useDirectChecker();
    setRootMenu(dotSessionMenu);
  }

  private void clearLoadedData() {
    lts = null;
    pddlParser = null;
    modelChecker = null;
    porStrategy = null;
    pointedState = null;
    mainSource = null;
    secondarySource = null;
    pendingPddlFiles = null;
    setRootMenu(homeMenu);
  }

  private void useDirectChecker() {
    modelChecker = hasLts() ? new DirectAutomataModelChecker<>(lts, pointedState) : null;
  }

  private void useClassicChecker() {
    modelChecker = hasLts() ? new ClassicAutomataModelChecker<>(lts, pointedState) : null;
  }

  private void showPage(String sectionTitle, String... lines) {
    clearScreen();
    System.out.println("=== " + TITLE + " ===");
    System.out.println(SEPARATOR);
    printSessionSummary();
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

  private void printSessionSummary() {
    if (!hasLts()) {
      System.out.println("Sesion actual: sin LTS cargado");
      return;
    }

    Pair<Integer, Integer> size = lts.getSize();
    System.out.println("LTS cargado");
    System.out.println("Fuente: " + sourceSummary());
    System.out.println("Checker: " + checkerLabel());
    System.out.println("Tamanio: " + size.key() + " estados y " + size.value() + " transiciones");
    if (pddlWasLoaded()) {
      System.out.println("POR: " + porStrategyLabel());
    }
  }

  private String sourceSummary() {
    return secondarySource == null ? shortName(mainSource) : shortName(mainSource) + " / " + shortName(secondarySource);
  }

  private String defaultPointedState(LTS<String, String> currentLts) {
    if (currentLts.getStates().isEmpty()) {
      throw new IllegalArgumentException("El LTS cargado no tiene estados.");
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
    System.out.println("Acciones disponibles:");
    for (int i = 0; i < actions.size(); i++) {
      System.out.printf("\t%d. %s%n", i + 1, actions.get(i));
    }
    System.out.println("\tX. Terminar simulacion");
  }

  private <S> Integer chooseTarget(String action, List<S> targets, LTS<S, String> currentLts) {
    if (targets.isEmpty()) {
      System.out.println("La accion no tiene destinos.");
      return null;
    }
    if (targets.size() == 1) {
      return 0;
    }

    System.out.println("No determinismo detectado:");
    for (int i = 0; i < targets.size(); i++) {
      System.out.printf("\t%d. %s -> %s%n", i + 1, action, currentLts.toString(targets.get(i)));
    }
    return parseMenuIndex(readInput("Elegi el estado destino: "), targets.size());
  }

  private String readInput(String prompt) {
    System.out.print(prompt);
    return scanner.nextLine().trim();
  }

  private String readExistingFilename(String prompt) {
    while (true) {
      String filename = readFilename(prompt);
      if (new File(filename).exists()) {
        return filename;
      }
      System.out.println("El archivo no existe: " + filename);
    }
  }

  private String readFilename(String prompt) {
    if (lineReader != null) {
      try {
        return lineReader.readLine(prompt).trim();
      } catch (Exception e) {
        // Fall back to scanner
      }
    }
    return readInput(prompt);
  }

  private void pause() {
    System.out.print("Presiona Enter para continuar...");
    scanner.nextLine();
  }

  private void clearScreen() {
    try {
      if (terminal != null) {
        terminal.puts(InfoCmp.Capability.cursor_home);
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.writer().print("\033[3J");
        terminal.flush();
        return;
      }
    } catch (Exception e) {
      // Fall through to ANSI fallback
    }
    System.out.print("\033[3J\033[H\033[2J");
    System.out.flush();
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

  private boolean hasLts() {
    return lts != null;
  }

  private boolean pddlWasLoaded() {
    return pddlParser != null;
  }

  @SuppressWarnings("unchecked")
  private <State> LTS<Object, String> castLts(LTS<State, String> sourceLts) {
    return (LTS<Object, String>) sourceLts;
  }

  private String checkerLabel() {
    return modelChecker instanceof ClassicAutomataModelChecker<?, ?>
        ? "Classic (Fervari)"
        : "Direct (algoritmo propio)";
  }

  private String porStrategyLabel() {
    if (porStrategy instanceof StratifiedActionSelectionStrategy) {
      return "Stratified";
    }
    if (porStrategy instanceof StrongStubbornSetActionSelectionStrategy) {
      return "Strong Stubborn Sets (SSS)";
    }
    return "Ninguno";
  }

  private Predicate<Object> currentGoalPredicate() {
    return pddlWasLoaded()
        ? state -> modelChecker.check(pddlParser.getGoalExpression(), state)
        : state -> false;
  }

  private static String shortName(String path) {
    return path == null || path.isBlank() ? "-" : new File(path).getName();
  }

  @FunctionalInterface
  private interface MenuAction {
    void run() throws Exception;
  }

  private record Menu(
      String title,
      String prompt,
      MenuOption... options) {
    MenuOption zeroOption() {
      return options[0];
    }

    int numberedOptionCount() {
      return options.length - 1;
    }

    MenuOption numberedOption(int index) {
      return options[index + 1];
    }

    MenuOption optionFromInput(String input) {
      if ("0".equals(input)) {
        return zeroOption();
      }

      try {
        int option = Integer.parseInt(input);
        return option >= 1 && option <= numberedOptionCount() ? numberedOption(option - 1) : null;
      } catch (NumberFormatException e) {
        return null;
      }
    }

    String[] displayLines() {
      String[] lines = new String[options.length];
      for (int i = 1; i < options.length; i++) {
        lines[i - 1] = i + ". " + options[i].label();
      }
      lines[options.length - 1] = "0. " + zeroOption().label();
      return lines;
    }
  }

  private record MenuOption(String label, MenuAction action) {}
}
