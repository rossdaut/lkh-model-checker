package lkh.generator.impl;

import lkh.generator.LtsGeneratorConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class WitnessImplanter {
  private static final int MAX_ATTEMPTS = 1_000;

  private final LtsGeneratorConfig config;
  private final Random random;

  public WitnessImplanter(LtsGeneratorConfig config, Random random) {
    this.config = config;
    this.random = random;
  }

  public List<List<String>> implant(GenerationContext ctx) {
    LinkedHashSet<List<String>> witnesses = new LinkedHashSet<>();
    int attempts = 0;

    while (witnesses.size() < config.witnessCount()) {
      if (attempts++ >= MAX_ATTEMPTS * config.witnessCount()) {
        throw new IllegalArgumentException("Could not implant enough witness plans");
      }

      List<String> witness = randomWitness();
      if (!witnesses.contains(witness) && tryInsert(ctx, witness)) {
        witnesses.add(witness);
      }
    }

    return List.copyOf(witnesses);
  }

  private boolean tryInsert(GenerationContext ctx, List<String> witness) {
    WitnessPlan plan = new WitnessPlan(ctx.nextStateId, new ArrayList<>(ctx.neutralLabelPool));

    for (Integer initialState : ctx.initialStates) {
      if (!extendWitness(ctx, initialState, witness, plan)) {
        return false;
      }
    }

    commit(ctx, plan);
    protect(ctx, witness);
    return true;
  }

  private boolean extendWitness(GenerationContext ctx, int source, List<String> witness, WitnessPlan plan) {
    Set<Integer> frontier = Set.of(source);

    for (int i = 0; i < witness.size(); i++) {
      frontier = extendFrontier(ctx, frontier, witness, i, plan);
      if (frontier.isEmpty()) return false;
    }

    return true;
  }

  private Set<Integer> extendFrontier(GenerationContext ctx, Set<Integer> frontier, List<String> witness, int index, WitnessPlan plan) {
    LinkedHashSet<Integer> next = new LinkedHashSet<>();

    for (Integer source : frontier) {
      Set<Integer> targets = chooseTargets(ctx, source, witness, index, plan);
      if (targets.isEmpty()) return Set.of();
      targets.forEach(t -> plan.connect(source, witness.get(index), t));
      next.addAll(targets);
    }

    return next;
  }

  private Set<Integer> chooseTargets(GenerationContext ctx, int source, List<String> witness, int index, WitnessPlan plan) {
    Set<Integer> existing = existingTargets(ctx, source, witness, index, plan);
    if (existing != null) return existing;

    LinkedHashSet<Integer> chosen = new LinkedHashSet<>();
    Integer target = chooseOneTarget(ctx, witness, index, plan, chosen);
    if (target == null) return Set.of();
    chosen.add(target);

    double probability = 0.5;
    while (!config.deterministic() && random.nextDouble() < probability) {
      target = chooseOneTarget(ctx, witness, index, plan, chosen);
      if (target == null) break;
      chosen.add(target);
      probability /= 2.0;
    }

    return chosen;
  }

  private Set<Integer> existingTargets(GenerationContext ctx, int source, List<String> witness, int index, WitnessPlan plan) {
    Set<Integer> targets = targetsOf(ctx, source, witness.get(index), plan);
    if (targets.isEmpty()) return null;
    boolean isLast = index == witness.size() - 1;
    return isLast && !ctx.goalStates.containsAll(targets) ? Set.of() : targets;
  }

  private Integer chooseOneTarget(GenerationContext ctx, List<String> witness, int index, WitnessPlan plan, Set<Integer> excluded) {
    if (index == witness.size() - 1) {
      return randomGoalState(ctx, excluded);
    }

    Integer merged = findMergeTarget(ctx, witness, index + 1, plan, excluded);
    Integer created = plan.createNeutralState(random);

    if (merged != null && random.nextBoolean()) return merged;
    return created != null ? created : merged;
  }

  private Integer findMergeTarget(GenerationContext ctx, List<String> witness, int nextIndex, WitnessPlan plan, Set<Integer> excluded) {
    List<Integer> candidates = new ArrayList<>(ctx.lts.getStates());
    candidates.addAll(plan.newStates.keySet());
    Collections.shuffle(candidates, random);

    for (Integer candidate : candidates) {
      if (!excluded.contains(candidate) && supportsSuffix(ctx, candidate, witness, nextIndex, plan)) {
        return candidate;
      }
    }

    return null;
  }

  private boolean supportsSuffix(GenerationContext ctx, int state, List<String> witness, int index, WitnessPlan plan) {
    Set<Integer> frontier = Set.of(state);

    for (int i = index; i < witness.size(); i++) {
      frontier = targetsOf(ctx, frontier, witness.get(i), plan);
      if (frontier.isEmpty()) return false;
    }

    return ctx.goalStates.containsAll(frontier);
  }

  private Set<Integer> targetsOf(GenerationContext ctx, Set<Integer> states, String action, WitnessPlan plan) {
    LinkedHashSet<Integer> targets = new LinkedHashSet<>();

    for (Integer state : states) {
      Set<Integer> t = targetsOf(ctx, state, action, plan);
      if (t.isEmpty()) return Set.of();
      targets.addAll(t);
    }

    return targets;
  }

  private Set<Integer> targetsOf(GenerationContext ctx, int source, String action, WitnessPlan plan) {
    Set<Integer> planned = plan.transitions.get(new StateAction(source, action));
    if (planned != null) return planned;
    if (plan.newStates.containsKey(source)) return Set.of();
    return ctx.lts.targets(source, action);
  }

  private void commit(GenerationContext ctx, WitnessPlan plan) {
    plan.newStates.forEach((state, labels) -> ctx.lts.addState(state, labels));
    plan.transitions.forEach((sa, targets) ->
        targets.forEach(t -> ctx.lts.addTransition(sa.state(), t, sa.action()))
    );
    ctx.neutralLabelPool.removeAll(plan.usedNeutralLabels);
    ctx.nextStateId = plan.nextStateId;
  }

  private void protect(GenerationContext ctx, List<String> witness) {
    for (Integer initialState : ctx.initialStates) {
      Set<Integer> frontier = Set.of(initialState);

      for (String action : witness) {
        for (Integer state : frontier) {
          ctx.protectedActionsByState
              .computeIfAbsent(state, ignored -> new LinkedHashSet<>())
              .add(action);
        }
        frontier = ctx.lts.targets(frontier, action, true).orElse(Set.of());
      }
    }
  }

  private List<String> randomWitness() {
    List<String> witness = new ArrayList<>(config.minWitnessActionCount());
    List<String> actions = config.actions();
    for (int i = 0; i < config.minWitnessActionCount(); i++) {
      witness.add(actions.get(random.nextInt(actions.size())));
    }
    return List.copyOf(witness);
  }

  private Integer randomGoalState(GenerationContext ctx, Set<Integer> excluded) {
    List<Integer> available = new ArrayList<>(ctx.goalStates);
    available.removeAll(excluded);
    return available.isEmpty() ? null : available.get(random.nextInt(available.size()));
  }

  private static final class WitnessPlan {
    final Map<StateAction, Set<Integer>> transitions = new LinkedHashMap<>();
    final Map<Integer, Set<String>> newStates = new LinkedHashMap<>();
    final List<Set<String>> usedNeutralLabels = new ArrayList<>();
    private final List<Set<String>> availableNeutralLabels;
    int nextStateId;

    WitnessPlan(int nextStateId, List<Set<String>> availableNeutralLabels) {
      this.nextStateId = nextStateId;
      this.availableNeutralLabels = availableNeutralLabels;
    }

    Integer createNeutralState(Random random) {
      if (availableNeutralLabels.isEmpty()) return null;
      Set<String> labels = availableNeutralLabels.remove(random.nextInt(availableNeutralLabels.size()));
      usedNeutralLabels.add(labels);
      int state = nextStateId++;
      newStates.put(state, labels);
      return state;
    }

    void connect(int source, String action, int target) {
      transitions.computeIfAbsent(new StateAction(source, action), ignored -> new LinkedHashSet<>()).add(target);
    }
  }

  private record StateAction(int state, String action) {}
}
