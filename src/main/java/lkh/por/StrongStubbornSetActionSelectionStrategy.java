package lkh.por;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lkh.lts.builder.ActionSelectionStrategy;
import lkh.planning.Action;
import lkh.planning.Condition;
import lkh.planning.Literal;
import lkh.planning.Problem;
import lkh.planning.State;

public class StrongStubbornSetActionSelectionStrategy implements ActionSelectionStrategy {
  private Problem problem;
  private Map<Action, Set<Literal>> preconditions;
  private Map<Action, Set<Literal>> effects;
  private Map<Action, Set<Action>> dependencies;
  private Map<Literal, Set<Action>> achievers;

  @Override
  public Collection<? extends Action> selectActions(Action previousAction, State state, Problem problem) {
    if (state == null) {
      throw new IllegalArgumentException("Null state");
    }
    if (problem == null) {
      throw new IllegalArgumentException("Null problem");
    }

    ensureInitialized(problem);

    if (goalSatisfied(state, problem.getGoalCondition())) {
      return problem.getApplicableActions(state);
    }

    Set<Action> stubbornSet = buildStrongStubbornSet(state, problem);
    return stubbornSet.stream()
        .filter(action -> action.isApplicable(state))
        .toList();
  }

  private void ensureInitialized(Problem problem) {
    if (this.problem == problem) {
      return;
    }

    this.problem = problem;
    preconditions = new HashMap<>();
    effects = new HashMap<>();
    dependencies = new HashMap<>();
    achievers = new HashMap<>();

    for (Action action : problem.getActions()) {
      preconditions.put(action, new LinkedHashSet<>(action.getPrecondition().getLiterals()));
      effects.put(action, new LinkedHashSet<>(action.getEffects().getLiterals()));
      dependencies.put(action, new LinkedHashSet<>());

      for (Literal literal : effects.get(action)) {
        achievers.computeIfAbsent(literal, ignored -> new LinkedHashSet<>()).add(action);
      }
    }

    for (Action first : problem.getActions()) {
      for (Action second : problem.getActions()) {
        if (!first.equals(second) && interfere(first, second)) {
          dependencies.get(first).add(second);
        }
      }
    }
  }

  private Set<Action> buildStrongStubbornSet(State state, Problem problem) {
    Set<Action> stubbornSet = new LinkedHashSet<>();
    Deque<Action> pending = new ArrayDeque<>();

    addAll(stubbornSet, pending, goalLandmark(problem.getGoalCondition(), state));

    while (!pending.isEmpty()) {
      Action action = pending.removeFirst();
      Collection<? extends Action> next = action.isApplicable(state)
          ? dependencies.getOrDefault(action, Set.of())
          : necessaryEnablingSet(action, state);
      addAll(stubbornSet, pending, next);
    }

    return stubbornSet;
  }

  private Collection<? extends Action> goalLandmark(Condition goal, State state) {
    for (Literal literal : goal.getLiterals()) {
      if (!state.holds(literal)) {
        return achievers.getOrDefault(literal, Set.of());
      }
    }
    return Set.of();
  }

  private Collection<? extends Action> necessaryEnablingSet(Action action, State state) {
    for (Literal literal : preconditions.get(action)) {
      if (!state.holds(literal)) {
        return achievers.getOrDefault(literal, Set.of());
      }
    }
    return Set.of();
  }

  private boolean goalSatisfied(State state, Condition goal) {
    for (Literal literal : goal.getLiterals()) {
      if (!state.holds(literal)) {
        return false;
      }
    }
    return true;
  }
  private boolean interfere(Action first, Action second) {
    return disables(first, second) || disables(second, first) || conflict(first, second);
  }

  private boolean disables(Action first, Action second) {
    return containsNegation(effects.get(first), preconditions.get(second));
  }

  private boolean conflict(Action first, Action second) {
    return containsNegation(effects.get(first), effects.get(second));
  }

  private boolean containsNegation(Set<Literal> first, Set<Literal> second) {
    for (Literal literal : first) {
      if (second.contains(literal.negate())) {
        return true;
      }
    }
    return false;
  }

  private static void addAll(Set<Action> stubbornSet, Deque<Action> pending, Collection<? extends Action> actions) {
    for (Action action : actions) {
      if (stubbornSet.add(action)) {
        pending.addLast(action);
      }
    }
  }
}
