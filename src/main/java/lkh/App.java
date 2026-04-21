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
import lkh.lts.builder.ActionSelectionStrategy;
import lkh.lts.builder.DefaultActionSelectionStrategy;
import lkh.lts.builder.PDDL;
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
        "Menu principal",
        "Opcion: ",
        new MenuOption("Salir", this::exitApp),
        new MenuOption("Cargar LTS desde PDDL", () -> openMenu(pddlSourcesMenu)),
        new MenuOption("Cargar LTS desde DOT", () -> openMenu(dotSourcesMenu)));

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
        new MenuOption("Tire", () -> selectPddlFiles(PDDL_EXAMPLES_DIR + "tire-domain.pddl", PDDL_EXAMPLES_DIR + "tire-problem.pddl")),
        new MenuOption("Logistics", () -> selectPddlFiles(PDDL_EXAMPLES_DIR + "logistics-domain.pddl", PDDL_EXAMPLES_DIR + "logistics-problem.pddl")));

    dotSourcesMenu = new Menu(
        "Cargar DOT",
        "Fuente DOT: ",
        new MenuOption("Volver", this::goBack),
        new MenuOption("Archivo .dot", this::loadDot),
        new MenuOption("Generar desde configuracion", this::loadGeneratedDot));

    porMenu = new Menu(
        "Seleccionar POR",
        "POR: ",
        new MenuOption("Volver", this::goBack),
        new MenuOption(PorMode.NONE.label(), () -> loadPddlLts(PorMode.NONE)),
        new MenuOption(PorMode.STRATIFIED.label(), () -> loadPddlLts(PorMode.STRATIFIED)),
        new MenuOption(PorMode.STRONG_STUBBORN_SETS.label(), () -> loadPddlLts(PorMode.STRONG_STUBBORN_SETS)));

    checkerMenu = new Menu(
        "Cambiar checker",
        "Checker: ",
        new MenuOption("Volver", this::goBack),
        new MenuOption( ModelCheckerMode.DIRECT.label(), () -> updateChecker(ModelCheckerMode.DIRECT)),
        new MenuOption("Classic (Fervari)", () -> openMenu(classicMenu)));

    classicMenu = new Menu(
        "Classic (Fervari)",
        "Modo: ",
        new MenuOption("Volver", this::goBack),
        new MenuOption(ModelCheckerMode.CLASSIC.label(), () -> updateChecker(ModelCheckerMode.CLASSIC)),
        new MenuOption(ModelCheckerMode.CLASSIC_MINIMIZED.label(), () -> updateChecker(ModelCheckerMode.CLASSIC_MINIMIZED)));

    pddlSessionMenu = sessionMenu(true);
    dotSessionMenu = sessionMenu(false);
  }

  private Menu sessionMenu(boolean includeGoalCheck) {
    List<MenuOption> options = new ArrayList<>();
    options.add(new MenuOption("Salir", this::exitApp));
    if (includeGoalCheck) {
      options.add(new MenuOption("Chequear goal del problema", this::checkGoal));
    }
    options.add(new MenuOption("Chequear expresion", this::checkExpression));
    options.add(new MenuOption("Cambiar checker", () -> openMenu(checkerMenu)));
    options.add(new MenuOption("Exportar LTS a .dot", this::exportLts));
    options.add(new MenuOption("Simular", this::simulateCurrent));
    options.add(new MenuOption("Limpiar LTS y volver al menu principal", this::clearLoadedData));
    return new Menu("Menu principal", "Opcion: ", options.toArray(MenuOption[]::new));
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
    showPage("Cargar PDDL", "Carga manual de archivos");
    selectPddlFiles(
        ui.readExistingFilename("Archivo de dominio: "),
        ui.readExistingFilename("Archivo de problema: "));
  }

  private void selectPddlFiles(String domain, String problem) {
    pendingPddlFiles = new String[] {domain, problem};
    openMenu(porMenu);
  }

  private void loadPddlLts(PorMode porMode) throws FileNotFoundException {
    if (pendingPddlFiles == null) {
      throw new IllegalStateException("No hay archivos PDDL seleccionados.");
    }

    try {
      showPage("Cargando PDDL...");
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
      showPage("Cargar DOT");
      String filename = ui.readExistingFilename("Archivo .dot del LTS: ");

      showPage("Cargando LTS desde DOT...");
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
      System.out.println("Estado apuntado por defecto: " + session.pointedState());
    } catch (Throwable t) {
      clearLoadedData();
      throw t;
    }
  }

  private void loadGeneratedDot() throws IOException, ParseException {
    try {
      showPage("Generar DOT");
      String configFilename = ui.readExistingFilename("Archivo de configuracion: ");
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
    showPage("Checker actualizado a: " + mode.label());
    ui.pause();
    setRootMenu(currentSessionMenu());
  }

  private void exportLts() {
    showPage("Exportar LTS a .dot");
    String filename = ui.readFilename("Archivo de salida .dot: ");
    DotWriter.writeLTS(session.lts(), filename);
    showPage("LTS exportado a: " + filename);
  }

  private void checkExpression() throws ParseException {
    showPage("Chequear expresion");
    String expressionText = ui.readInput("Expresion: ");
    Expression expression = Expression.of(expressionText);

    showPage("Chequeando expresion: " + expressionText);
    boolean result = check(expression);
    if (!containsKh(expression)) {
      System.out.println("Chequeo sobre el estado apuntado " + session.pointedState() + ".");
    }
    System.out.println(result ? "La expresion vale :)" : "La expresion no vale :(");

    if (result && expression.getTokenType() == ExpressionType.KH) {
      showWitnesses(expression.getLeft(), expression.getRight());
    }
    ui.pause();
  }

  private void checkGoal() {
    showPage("Chequeando goal del problema...");
    System.out.println("Goal: " + session.pddlParser().getGoalExpression());

    Expression initial = session.pddlParser().getInitialExpression();
    Expression goal = session.pddlParser().getGoalExpression();
    boolean result = check(Expression.kh(initial, goal));

    System.out.println();
    System.out.println(result ? "El goal es alcanzable via KH." : "El goal no es alcanzable via KH.");
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
    if (startsWithIgnoreCase(ui.readInput("Mostrar witnesses? (Y/n): "), "n")) {
      return;
    }

    Iterator<List<String>> witnesses = session.modelChecker().witnesses(init, end, 100);
    while (witnesses.hasNext()) {
      System.out.println(witnesses.next());
      System.out.println();
      if (startsWithIgnoreCase(ui.readInput("Siguiente? (y/n): "), "n")) {
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
    simulate("Simulacion", session.pointedState(), session.lts(), session.goalPredicate());
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
      String input = ui.readInput("Elegi una accion: ");
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
    ui.pause();
  }

  private void clearLoadedData() {
    session.clear();
    pendingPddlFiles = null;
    setRootMenu(homeMenu);
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
    return parseMenuIndex(ui.readInput("Elegi el estado destino: "), targets.size());
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
      lines[initialStates.size()] = "0. Volver al menu principal";
      showPage("Seleccionar estado inicial", lines);

      String input = ui.readInput("Estado inicial: ");
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
    DIRECT("Direct (algoritmo propio)") {
      @Override
      ModelChecker<Object, String> create(LTS<Object, String> lts, Object pointedState) {
        return new DirectAutomataModelChecker<>(lts, pointedState);
      }
    },
    CLASSIC("Classic (Fervari, sin minimizacion)") {
      @Override
      ModelChecker<Object, String> create(LTS<Object, String> lts, Object pointedState) {
        return new ClassicAutomataModelChecker<>(lts, pointedState, false);
      }
    },
    CLASSIC_MINIMIZED("Classic (Fervari, con minimizacion)") {
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
    NONE("Ninguno") {
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
        return List.of("Sesion actual: sin LTS cargado");
      }

      Pair<Integer, Integer> size = lts.getSize();
      List<String> lines = new ArrayList<>();
      lines.add("LTS cargado");
      lines.add("Fuente: " + sourceSummary());
      lines.add("Checker: " + checkerMode.label());
      lines.add("Tamanio: " + size.key() + " estados y " + size.value() + " transiciones");
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
        System.out.println("El archivo no existe: " + filename);
      }
    }

    private String readFilename(String prompt) {
      return readLine(prompt);
    }

    private void pause() {
      System.out.println();
      readLine("Presiona Enter para continuar...");
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
