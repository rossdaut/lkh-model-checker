package lkh.planning.pddl4j;

import java.util.Collection;
import lkh.planning.Condition;
import lkh.planning.Fluent;

final class Pddl4jCondition implements Condition {
  private final fr.uga.pddl4j.problem.operator.Condition delegate;
  private final Pddl4jProblem problem;

  Pddl4jCondition(fr.uga.pddl4j.problem.operator.Condition delegate, Pddl4jProblem problem) {
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
