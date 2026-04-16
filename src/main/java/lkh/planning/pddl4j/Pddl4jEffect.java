package lkh.planning.pddl4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lkh.planning.Effect;
import lkh.planning.Fluent;
import lkh.planning.Literal;

final class Pddl4jEffect implements Effect {
  private final Collection<Fluent> positiveFluents;
  private final Collection<Fluent> negativeFluents;
  private final Collection<Literal> literals;

  Pddl4jEffect(fr.uga.pddl4j.problem.operator.Effect delegate, Pddl4jProblem problem) {
    this.positiveFluents = problem.wrapFluents(delegate.getPositiveFluents());
    this.negativeFluents = problem.wrapFluents(delegate.getNegativeFluents());
    this.literals = buildLiterals();
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
    return literals;
  }

  private Collection<Literal> buildLiterals() {
    List<Literal> built = new ArrayList<>();
    for (Fluent fluent : positiveFluents) {
      built.add(Literal.positive(fluent));
    }
    for (Fluent fluent : negativeFluents) {
      built.add(Literal.negative(fluent));
    }
    return List.copyOf(built);
  }
}
