package lkh.testutils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lkh.planning.Action;
import lkh.planning.Condition;
import lkh.planning.Effect;
import lkh.planning.Fluent;
import lkh.planning.Problem;
import lkh.planning.State;
import lkh.por.AnalyzableAction;

public final class TestPlanningObjects {
  private TestPlanningObjects() {
  }

  public static TestFluent fluent(String name) {
    return new TestFluent(name);
  }

  public static TestCondition condition(Fluent... positive) {
    return new TestCondition(Set.of(positive), Set.of());
  }

  public static TestCondition conditionNot(Fluent... negative) {
    return new TestCondition(Set.of(), Set.of(negative));
  }

  public static TestCondition condition(Set<Fluent> positive, Set<Fluent> negative) {
    return new TestCondition(positive, negative);
  }

  public static TestEffect effect(Fluent... positive) {
    return new TestEffect(Set.of(positive), Set.of());
  }

  public static TestEffect effectNot(Fluent... negative) {
    return new TestEffect(Set.of(), Set.of(negative));
  }

  public static TestEffect effect(Set<Fluent> positive, Set<Fluent> negative) {
    return new TestEffect(positive, negative);
  }

  public static TestState state(Fluent... fluents) {
    return new TestState(Set.of(fluents));
  }

  public static TestAction action(String name, Condition precondition, Effect effect) {
    return new TestAction(name, precondition, effect);
  }

  public static TestProblem problem(List<? extends Fluent> fluents, List<? extends Action> actions, State initialState,
                                    Condition goalCondition) {
    return new TestProblem(fluents, actions, initialState, goalCondition);
  }

  public record TestFluent(String name) implements Fluent {
    @Override
    public String toString() {
      return name;
    }
  }

  public record TestCondition(Set<Fluent> positiveFluents, Set<Fluent> negativeFluents) implements Condition {
    public TestCondition(Set<Fluent> positiveFluents, Set<Fluent> negativeFluents) {
      this.positiveFluents = Set.copyOf(positiveFluents);
      this.negativeFluents = Set.copyOf(negativeFluents);
    }

    @Override
    public Collection<Fluent> getPositiveFluents() {
      return positiveFluents;
    }

    @Override
    public Collection<Fluent> getNegativeFluents() {
      return negativeFluents;
    }
  }

  public record TestEffect(Set<Fluent> positiveFluents, Set<Fluent> negativeFluents) implements Effect {
    public TestEffect(Set<Fluent> positiveFluents, Set<Fluent> negativeFluents) {
      this.positiveFluents = Set.copyOf(positiveFluents);
      this.negativeFluents = Set.copyOf(negativeFluents);
    }

    @Override
    public Collection<Fluent> getPositiveFluents() {
      return positiveFluents;
    }

    @Override
    public Collection<Fluent> getNegativeFluents() {
      return negativeFluents;
    }
  }

  public static class TestAction implements AnalyzableAction {
    private final String name;
    private final Condition precondition;
    private final Effect effects;

    public TestAction(String name, Condition precondition, Effect effects) {
      this.name = name;
      this.precondition = precondition;
      this.effects = effects;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Condition getPrecondition() {
      return precondition;
    }

    @Override
    public Effect getEffects() {
      return effects;
    }

    @Override
    public boolean isApplicable(State state) {
      return precondition.getLiterals().stream().allMatch(state::holds);
    }

    @Override
    public Collection<Fluent> getDependentFluents() {
      LinkedHashSet<Fluent> dependent = new LinkedHashSet<>(precondition.getPositiveFluents());
      dependent.addAll(precondition.getNegativeFluents());
      return dependent;
    }

    @Override
    public Collection<Fluent> getAffectedFluents() {
      LinkedHashSet<Fluent> affected = new LinkedHashSet<>(effects.getPositiveFluents());
      affected.addAll(effects.getNegativeFluents());
      return affected;
    }

    @Override
    public Collection<Fluent> getTransitionFluents() {
      LinkedHashSet<Fluent> transition = new LinkedHashSet<>(getAffectedFluents());
      transition.retainAll(getDependentFluents());
      return transition;
    }
  }

  public static class TestState implements State {
    private final Set<Fluent> fluents;

    public TestState(Set<Fluent> fluents) {
      this.fluents = new LinkedHashSet<>(fluents);
    }

    @Override
    public Collection<Fluent> getFluents() {
      return Set.copyOf(fluents);
    }

    @Override
    public State copy() {
      return new TestState(fluents);
    }

    @Override
    public void apply(Action action) {
      fluents.addAll(action.getEffects().getPositiveFluents());
      fluents.removeAll(action.getEffects().getNegativeFluents());
    }
  }

  public static class TestProblem implements Problem {
    private final List<? extends Fluent> fluents;
    private final List<? extends Action> actions;
    private final State initialState;
    private final Condition goalCondition;

    public TestProblem(List<? extends Fluent> fluents, List<? extends Action> actions, State initialState,
                       Condition goalCondition) {
      this.fluents = List.copyOf(fluents);
      this.actions = List.copyOf(actions);
      this.initialState = initialState;
      this.goalCondition = goalCondition;
    }

    @Override
    public List<? extends Fluent> getFluents() {
      return fluents;
    }

    @Override
    public List<? extends Action> getActions() {
      return actions;
    }

    @Override
    public State getInitialState() {
      return initialState;
    }

    @Override
    public Condition getGoalCondition() {
      return goalCondition;
    }
  }
}
