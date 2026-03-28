package lkh.planning.pddl4j;

import java.util.Collection;
import lkh.planning.Effect;
import lkh.planning.Fluent;

final class Pddl4jEffect implements Effect {
  private final fr.uga.pddl4j.problem.operator.Effect delegate;
  private final Pddl4jProblem problem;

  Pddl4jEffect(fr.uga.pddl4j.problem.operator.Effect delegate, Pddl4jProblem problem) {
    this.delegate = delegate;
    this.problem = problem;
  }

  @Override
  public Collection<Fluent> getPositiveFluents() {
    return problem.wrapFluents(delegate.getPositiveFluents());
  }

  @Override
  public Collection<Fluent> getNegativeFluents() {
    return problem.wrapFluents(delegate.getNegativeFluents());
  }
}
