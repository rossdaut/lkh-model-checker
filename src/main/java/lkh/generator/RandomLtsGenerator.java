package lkh.generator;

import lkh.expression.Expression;
import lkh.lts.HashMapLTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class RandomLtsGenerator {
  private static final int DEFAULT_MAX_ATTEMPTS = 1_000;

  private final LtsGeneratorConfig config;
  private final Random random;
  private final List<String> actions;
  private final List<String> propositions;

  private HashMapLTS<Integer, String> lts;
  private LabelPools labelPools;
  private final Set<Integer> initialStates = new LinkedHashSet<>();
  private final Set<Integer> goalStates = new LinkedHashSet<>();
  private final Map<Integer, Set<String>> protectedActionsByState = new LinkedHashMap<>();
  private int nextStateId;

  public RandomLtsGenerator(LtsGeneratorConfig config) {
    if (config == null) {
      throw new IllegalArgumentException("config must not be null");
    }

    this.config = config;
    this.random = new Random(config.seed());
    this.actions = actionNames(config.actionCount());
    this.propositions = propositionNames(config.propositionCount());
  }

  public GeneratedLts generate() {
    reset();

    buildLabelPools();
    createInitialStates();
    createRequiredGoalStates();

    List<List<String>> witnesses = implantWitnesses();
    addNoiseStates();
    addNoiseEdges();

    return new GeneratedLts(
        config,
        lts,
        witnesses.stream().map(List::copyOf).toList(),
        Set.copyOf(initialStates),
        Set.copyOf(goalStates)
    );
  }

  private void reset() {
    lts = new HashMapLTS<>();
    labelPools = null;
    initialStates.clear();
    goalStates.clear();
    protectedActionsByState.clear();
    nextStateId = 0;
  }

  private void buildLabelPools() {
    labelPools = new LabelPools();
    collectLabels(0, new LinkedHashSet<>());
  }

  private void collectLabels(int index, Set<String> labels) {
    if (index == propositions.size()) {
      classifyLabels(labels);
      return;
    }

    collectLabels(index + 1, labels);

    labels.add(propositions.get(index));
    collectLabels(index + 1, labels);
    labels.remove(propositions.get(index));
  }

  private void classifyLabels(Set<String> labels) {
    Set<String> copy = Set.copyOf(labels);
    if (holdsInitial(copy)) {
      labelPools.initialLabels.add(copy);
      return;
    }
    if (holdsGoal(copy)) {
      labelPools.goalLabels.add(copy);
      return;
    }
    labelPools.neutralLabels.add(copy);
  }

  private void createInitialStates() {
    for (int i = 0; i < config.initialStateCount(); i++) {
      registerState(removeRandomLabel(labelPools.initialLabels));
    }
  }

  private void createRequiredGoalStates() {
    while (goalStates.size() < config.goalStateCount()) {
      registerState(removeRandomLabel(labelPools.goalLabels));
    }
  }

  private int registerState(Set<String> labels) {
    int state = nextStateId++;
    lts.addState(state, labels);

    if (holdsInitial(labels)) {
      initialStates.add(state);
    }
    if (holdsGoal(labels)) {
      goalStates.add(state);
    }

    return state;
  }

  private List<List<String>> implantWitnesses() {
    LinkedHashSet<List<String>> witnesses = new LinkedHashSet<>();
    int attempts = 0;

    while (witnesses.size() < config.witnessCount()) {
      if (attempts++ >= DEFAULT_MAX_ATTEMPTS * config.witnessCount()) {
        throw new IllegalArgumentException("Could not implant enough witness plans");
      }

      List<String> witness = randomWitness();
      if (witnesses.contains(witness)) {
        continue;
      }
      if (tryInsertWitness(witness)) {
        witnesses.add(witness);
      }
    }

    return List.copyOf(witnesses);
  }

  private boolean tryInsertWitness(List<String> witness) {
    WitnessPlan plan = new WitnessPlan(nextStateId, new ArrayList<>(labelPools.neutralLabels));

    for (Integer initialState : initialStates) {
      if (!extendWitness(initialState, witness, plan)) {
        return false;
      }
    }

    commitPlan(plan);
    protectWitness(witness);
    return true;
  }

  private boolean extendWitness(int sourceState, List<String> witness, WitnessPlan plan) {
    Set<Integer> frontier = Set.of(sourceState);

    for (int index = 0; index < witness.size(); index++) {
      frontier = extendFrontier(frontier, witness, index, plan);
      if (frontier.isEmpty()) {
        return false;
      }
    }

    return true;
  }

  private Set<Integer> extendFrontier(Set<Integer> frontier, List<String> witness, int index, WitnessPlan plan) {
    LinkedHashSet<Integer> nextFrontier = new LinkedHashSet<>();

    for (Integer source : frontier) {
      Set<Integer> targets = chooseWitnessTargets(source, witness, index, plan);
      if (targets.isEmpty()) {
        return Set.of();
      }

      for (Integer target : targets) {
        plan.connect(source, witness.get(index), target);
      }
      nextFrontier.addAll(targets);
    }

    return nextFrontier;
  }

  private Set<Integer> chooseWitnessTargets(int source, List<String> witness, int index, WitnessPlan plan) {
    Set<Integer> existing = existingWitnessTargets(source, witness, index, plan);
    if (existing != null) {
      return existing;
    }

    LinkedHashSet<Integer> chosen = new LinkedHashSet<>();
    Integer target = chooseOneWitnessTarget(source, witness, index, plan, chosen);
    if (target == null) {
      return Set.of();
    }
    chosen.add(target);

    double probability = 0.5;
    while (shouldAddAnotherTarget(probability)) {
      target = chooseOneWitnessTarget(source, witness, index, plan, chosen);
      if (target == null) {
        break;
      }
      chosen.add(target);

      probability /= 2.0;
    }

    return chosen;
  }

  private Set<Integer> existingWitnessTargets(int source, List<String> witness, int index, WitnessPlan plan) {
    Set<Integer> targets = targetsOf(source, witness.get(index), plan);
    if (targets.isEmpty()) {
      return null;
    }
    return isLastStep(witness, index) && !goalStates.containsAll(targets) ? Set.of() : targets;
  }

  private boolean shouldAddAnotherTarget(double probability) {
    return !config.deterministic() && random.nextDouble() < probability;
  }

  private boolean isLastStep(List<String> witness, int index) {
    return index == witness.size() - 1;
  }

  private Integer chooseOneWitnessTarget(
      int source,
      List<String> witness,
      int index,
      WitnessPlan plan,
      Set<Integer> excluded
  ) {
    if (isLastStep(witness, index)) {
      return randomGoalState(excluded);
    }

    Integer merged = findMergeTarget(source, witness, index + 1, plan, excluded);
    Integer created = plan.createNeutralState(random);

    if (merged != null && random.nextBoolean()) {
      return merged;
    }
    return created != null ? created : merged;
  }

  private Integer findMergeTarget(
      int source,
      List<String> witness,
      int nextIndex,
      WitnessPlan plan,
      Set<Integer> excluded
  ) {
    List<Integer> candidates = new ArrayList<>(lts.getStates());
    candidates.addAll(plan.newStates.keySet());
    Collections.shuffle(candidates, random);

    for (Integer candidate : candidates) {
      if (candidate != source && !excluded.contains(candidate) && supportsSuffix(candidate, witness, nextIndex, plan)) {
        return candidate;
      }
    }

    return null;
  }

  private boolean supportsSuffix(int state, List<String> witness, int index, WitnessPlan plan) {
    Set<Integer> frontier = Set.of(state);

    for (int i = index; i < witness.size(); i++) {
      frontier = targetsOf(frontier, witness.get(i), plan);
      if (frontier.isEmpty()) {
        return false;
      }
    }

    return goalStates.containsAll(frontier);
  }

  private Set<Integer> targetsOf(Set<Integer> states, String action, WitnessPlan plan) {
    LinkedHashSet<Integer> targets = new LinkedHashSet<>();

    for (Integer state : states) {
      Set<Integer> sourceTargets = targetsOf(state, action, plan);
      if (sourceTargets.isEmpty()) {
        return Set.of();
      }
      targets.addAll(sourceTargets);
    }

    return targets;
  }

  private Set<Integer> targetsOf(int source, String action, WitnessPlan plan) {
    Set<Integer> planned = plan.transitions.get(new StateAction(source, action));
    if (planned != null) {
      return planned;
    }
    if (plan.newStates.containsKey(source)) {
      return Set.of();
    }

    return lts.targets(source, action);
  }

  private void commitPlan(WitnessPlan plan) {
    for (Map.Entry<Integer, Set<String>> state : plan.newStates.entrySet()) {
      lts.addState(state.getKey(), state.getValue());
    }
    for (Map.Entry<StateAction, Set<Integer>> transition : plan.transitions.entrySet()) {
      for (Integer target : transition.getValue()) {
        lts.addTransition(transition.getKey().state(), target, transition.getKey().action());
      }
    }

    labelPools.neutralLabels.removeAll(plan.usedNeutralLabels);
    nextStateId = plan.nextStateId;
  }

  private void protectWitness(List<String> witness) {
    for (Integer initialState : initialStates) {
      Set<Integer> frontier = Set.of(initialState);

      for (String action : witness) {
        for (Integer state : frontier) {
          protectedActionsByState.computeIfAbsent(state, ignored -> new LinkedHashSet<>()).add(action);
        }
        frontier = lts.targets(frontier, action, true).orElse(Set.of());
      }
    }
  }

  private void addNoiseStates() {
    while (nodeCount() < config.minNodeCount()) {
      createNoiseState();
    }
  }

  private void addNoiseEdges() {
    while (edgeCount() < config.minEdgeCount()) {
      if (!tryAddNoiseEdge()) {
        createNoiseState();
      }
    }
  }

  private int createNoiseState() {
    int noiseState = registerState(removeRandomLabel(labelPools.neutralLabels));
    attachNoiseState(noiseState);
    return noiseState;
  }

  private void attachNoiseState(int noiseState) {
    if (lts.getStates().size() <= 1) {
      return;
    }

    for (int attempt = 0; attempt < DEFAULT_MAX_ATTEMPTS; attempt++) {
      int anchor = randomState();
      if (anchor == noiseState) {
        continue;
      }

      if (random.nextBoolean() && tryAddNoiseEdge(anchor, noiseState)) {
        return;
      }
      if (tryAddNoiseEdge(noiseState, anchor)) {
        return;
      }
      if (tryAddNoiseEdge(anchor, noiseState)) {
        return;
      }
    }
  }

  private boolean tryAddNoiseEdge() {
    if (lts.getStates().isEmpty()) {
      return false;
    }

    for (int attempt = 0; attempt < DEFAULT_MAX_ATTEMPTS; attempt++) {
      int source = randomState();
      int target = randomState();
      if (tryAddNoiseEdge(source, target)) {
        return true;
      }
    }

    return false;
  }

  private boolean tryAddNoiseEdge(int source, int target) {
    if (source == target && lts.getStates().size() > 1 && random.nextBoolean()) {
      target = randomState();
    }

    List<String> availableActions = availableNoiseActions(source);
    if (availableActions.isEmpty()) {
      return false;
    }

    String action = randomElement(availableActions);
    if (lts.targets(source, action).contains(target)) {
      return false;
    }

    lts.addTransition(source, target, action);
    return true;
  }

  private List<String> availableNoiseActions(int source) {
    List<String> available = new ArrayList<>(actions);
    available.removeIf(action -> isProtected(source, action));

    if (config.deterministic()) {
      available.removeIf(action -> !lts.targets(source, action).isEmpty());
    }

    return available;
  }

  private boolean isProtected(int state, String action) {
    return protectedActionsByState.getOrDefault(state, Set.of()).contains(action);
  }

  private boolean holdsInitial(Set<String> labels) {
    return holds(config.initialCondition(), labels);
  }

  private boolean holdsGoal(Set<String> labels) {
    return holds(config.goalCondition(), labels);
  }

  private boolean holds(Expression expression, Set<String> labels) {
    if (expression == null) {
      throw new IllegalArgumentException("expression must not be null");
    }
    if (labels == null) {
      throw new IllegalArgumentException("labels must not be null");
    }

    return switch (expression.getTokenType()) {
      case PROP -> labels.contains(expression.getName());
      case NOT -> !holds(expression.getRight(), labels);
      case AND -> holds(expression.getLeft(), labels) && holds(expression.getRight(), labels);
      case OR -> holds(expression.getLeft(), labels) || holds(expression.getRight(), labels);
      case IMPLIES -> !holds(expression.getLeft(), labels) || holds(expression.getRight(), labels);
      case KH -> throw new IllegalArgumentException("KH expressions are not supported for generation conditions");
    };
  }

  private Set<String> removeRandomLabel(List<Set<String>> pool) {
    if (pool.isEmpty()) {
      throw new IllegalArgumentException("Not enough unique labels for " + poolContextName(pool));
    }

    return pool.remove(random.nextInt(pool.size()));
  }

  private String poolContextName(List<Set<String>> pool) {
    if (pool == labelPools.initialLabels) {
      return "initial state";
    }
    if (pool == labelPools.goalLabels) {
      return "goal state";
    }
    if (pool == labelPools.neutralLabels) {
      return "noise state";
    }

    throw new IllegalArgumentException("Unknown label pool");
  }

  private int randomGoalState() {
    return randomState(goalStates);
  }

  private Integer randomGoalState(Set<Integer> excluded) {
    List<Integer> availableGoals = new ArrayList<>(goalStates);
    availableGoals.removeAll(excluded);
    return availableGoals.isEmpty() ? null : randomElement(availableGoals);
  }

  private List<String> randomWitness() {
    List<String> witness = new ArrayList<>(config.minWitnessActionCount());
    for (int i = 0; i < config.minWitnessActionCount(); i++) {
      witness.add(randomAction());
    }
    return List.copyOf(witness);
  }

  private int randomState() {
    return randomState(lts.getStates());
  }

  private int randomState(Set<Integer> states) {
    return randomElement(new ArrayList<>(states));
  }

  private String randomAction() {
    return randomElement(actions);
  }

  private <T> T randomElement(List<T> values) {
    return values.get(random.nextInt(values.size()));
  }

  private int nodeCount() {
    return lts.getSize().key();
  }

  private int edgeCount() {
    return lts.getSize().value();
  }

  private List<String> actionNames(int actionCount) {
    List<String> generatedActions = new ArrayList<>(actionCount);
    for (int i = 0; i < actionCount; i++) {
      generatedActions.add("a" + i);
    }
    return List.copyOf(generatedActions);
  }

  private List<String> propositionNames(int propositionCount) {
    List<String> generatedPropositions = new ArrayList<>(propositionCount);
    for (int i = 0; i < propositionCount; i++) {
      generatedPropositions.add("p" + i);
    }
    return List.copyOf(generatedPropositions);
  }

  private static final class LabelPools {
    private final List<Set<String>> initialLabels = new ArrayList<>();
    private final List<Set<String>> goalLabels = new ArrayList<>();
    private final List<Set<String>> neutralLabels = new ArrayList<>();
  }

  private static final class WitnessPlan {
    private final Map<StateAction, Set<Integer>> transitions = new LinkedHashMap<>();
    private final Map<Integer, Set<String>> newStates = new LinkedHashMap<>();
    private final List<Set<String>> availableNeutralLabels;
    private final List<Set<String>> usedNeutralLabels = new ArrayList<>();
    private int nextStateId;

    private WitnessPlan(int nextStateId, List<Set<String>> availableNeutralLabels) {
      this.nextStateId = nextStateId;
      this.availableNeutralLabels = availableNeutralLabels;
    }

    private Integer createNeutralState(Random random) {
      if (availableNeutralLabels.isEmpty()) {
        return null;
      }

      Set<String> labels = availableNeutralLabels.remove(random.nextInt(availableNeutralLabels.size()));
      usedNeutralLabels.add(labels);
      int state = nextStateId++;
      newStates.put(state, labels);
      return state;
    }

    private void connect(int source, String action, int target) {
      transitions.computeIfAbsent(new StateAction(source, action), ignored -> new LinkedHashSet<>()).add(target);
    }
  }

  private record StateAction(int state, String action) {
  }
}
