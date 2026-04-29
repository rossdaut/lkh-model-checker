package lkh.planning.pddl4j;

import java.util.Collection;
import java.util.Objects;
import lkh.planning.Action;
import lkh.planning.Fluent;
import lkh.planning.Literal;
import lkh.planning.State;

final class Pddl4jState implements State {
  private final fr.uga.pddl4j.problem.State delegate;
  private final Pddl4jFluentRegistry fluentRegistry;
  private Collection<Fluent> cachedFluents;

  Pddl4jState(fr.uga.pddl4j.problem.State delegate, Pddl4jFluentRegistry fluentRegistry) {
    this.delegate = delegate;
    this.fluentRegistry = fluentRegistry;
  }

  fr.uga.pddl4j.problem.State unwrap() {
    return delegate;
  }

  @Override
  public Collection<Fluent> getFluents() {
    if (cachedFluents == null) {
      cachedFluents = fluentRegistry.wrap(delegate);
    }
    return cachedFluents;
  }

  @Override
  public boolean holds(Literal literal) {
    if (literal.fluent() instanceof Pddl4jFluent fluent) {
      boolean present = delegate.get(fluent.index());
      return literal.positive() == present;
    }
    return State.super.holds(literal);
  }

  @Override
  public State copy() {
    return new Pddl4jState(new fr.uga.pddl4j.problem.State(delegate), fluentRegistry);
  }

  @Override
  public void apply(Action action) {
    if (!(action instanceof Pddl4jAction pddl4jAction)) {
      throw new IllegalArgumentException("Action must be a Pddl4jAction");
    }
    delegate.apply(pddl4jAction.unwrap().getUnconditionalEffect());
    cachedFluents = null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Pddl4jState other)) {
      return false;
    }
    return delegate.equals(other.delegate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(delegate);
  }
}
