package lkh.planning.pddl4j;

import java.util.Collection;
import java.util.List;
import lkh.planning.Condition;
import lkh.planning.Effect;
import lkh.planning.Fluent;
import lkh.planning.Literal;

final class Pddl4jLiteralSet implements Condition, Effect {
  private final Collection<Fluent> positiveFluents;
  private final Collection<Fluent> negativeFluents;

  Pddl4jLiteralSet(Collection<Fluent> positiveFluents, Collection<Fluent> negativeFluents) {
    this.positiveFluents = List.copyOf(positiveFluents);
    this.negativeFluents = List.copyOf(negativeFluents);
  }

  @Override
  public Collection<Fluent> getPositiveFluents() {
    return positiveFluents;
  }

  @Override
  public Collection<Fluent> getNegativeFluents() {
    return negativeFluents;
  }

  @Override
  public Collection<Literal> getLiterals() {
    return Condition.super.getLiterals();
  }
}
