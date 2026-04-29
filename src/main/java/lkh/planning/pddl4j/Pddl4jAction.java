package lkh.planning.pddl4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import lkh.planning.Condition;
import lkh.planning.Effect;
import lkh.planning.Fluent;
import lkh.planning.State;
import lkh.por.AnalyzableAction;

final class Pddl4jAction implements AnalyzableAction {
  private final fr.uga.pddl4j.problem.operator.Action delegate;
  private final Pddl4jFluentRegistry fluents;
  private final String name;
  private Collection<Fluent> dependentFluents;
  private Collection<Fluent> affectedFluents;
  private Collection<Fluent> transitionFluents;

  Pddl4jAction(
      fr.uga.pddl4j.problem.operator.Action delegate,
      fr.uga.pddl4j.problem.Problem problem,
      Pddl4jFluentRegistry fluents) {
    this.delegate = delegate;
    this.fluents = fluents;
    this.name = buildName(delegate, problem);
  }

  private static String buildName(fr.uga.pddl4j.problem.operator.Action action, fr.uga.pddl4j.problem.Problem problem) {
    List<String> arguments = new ArrayList<>();
    for (int id : action.getInstantiations()) {
      arguments.add(problem.getConstantSymbols().get(id));
    }
    if (arguments.isEmpty()) {
      return action.getName();
    }
    return action.getName() + "(" + String.join(", ", arguments) + ")";
  }

  fr.uga.pddl4j.problem.operator.Action unwrap() {
    return delegate;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Condition getPrecondition() {
    return new Pddl4jLiteralSet(
        fluents.wrap(delegate.getPrecondition().getPositiveFluents()),
        fluents.wrap(delegate.getPrecondition().getNegativeFluents()));
  }

  @Override
  public Effect getEffects() {
    return new Pddl4jLiteralSet(
        fluents.wrap(delegate.getUnconditionalEffect().getPositiveFluents()),
        fluents.wrap(delegate.getUnconditionalEffect().getNegativeFluents()));
  }

  @Override
  public boolean isApplicable(State state) {
    if (!(state instanceof Pddl4jState pddl4jState)) {
      throw new IllegalArgumentException("State must be a Pddl4jState");
    }
    return delegate.isApplicable(pddl4jState.unwrap());
  }

  @Override
  public Collection<Fluent> getDependentFluents() {
    ensureAnalysis();
    return dependentFluents;
  }

  @Override
  public Collection<Fluent> getAffectedFluents() {
    ensureAnalysis();
    return affectedFluents;
  }

  @Override
  public Collection<Fluent> getTransitionFluents() {
    ensureAnalysis();
    return transitionFluents;
  }

  private void ensureAnalysis() {
    if (dependentFluents != null) {
      return;
    }
    LinkedHashSet<Fluent> dependent = new LinkedHashSet<>();
    dependent.addAll(getPrecondition().getPositiveFluents());
    dependent.addAll(getPrecondition().getNegativeFluents());

    LinkedHashSet<Fluent> affected = new LinkedHashSet<>();
    affected.addAll(getEffects().getPositiveFluents());
    affected.addAll(getEffects().getNegativeFluents());

    LinkedHashSet<Fluent> transition = new LinkedHashSet<>(affected);
    transition.retainAll(dependent);

    dependentFluents = dependent;
    affectedFluents = affected;
    transitionFluents = transition;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Pddl4jAction other)) {
      return false;
    }
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return name;
  }
}
