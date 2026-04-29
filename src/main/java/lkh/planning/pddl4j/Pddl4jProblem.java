package lkh.planning.pddl4j;

import fr.uga.pddl4j.parser.Parser;
import fr.uga.pddl4j.planners.LogLevel;
import fr.uga.pddl4j.problem.DefaultProblem;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lkh.planning.Action;
import lkh.planning.Fluent;
import lkh.planning.Problem;
import lkh.planning.State;

public class Pddl4jProblem implements Problem {
  private final DefaultProblem delegate;
  private final Pddl4jFluentRegistry fluents;
  private final List<? extends Fluent> unmodifiableFluents;
  private final List<? extends Action> unmodifiableActions;
  private final lkh.planning.Condition goalCondition;

  public Pddl4jProblem(String domainFilename, String problemFilename) throws FileNotFoundException {
    Parser parser = new Parser();
    parser.setLogLevel(LogLevel.OFF);
    delegate = new DefaultProblem(parser.parse(domainFilename, problemFilename));
    delegate.instantiate();
    validateNoConditionalEffects();
    fluents = new Pddl4jFluentRegistry(delegate);
    unmodifiableFluents = fluents.all();
    unmodifiableActions = buildActions();
    goalCondition = new Pddl4jLiteralSet(
        fluents.wrap(delegate.getGoal().getPositiveFluents()),
        fluents.wrap(delegate.getGoal().getNegativeFluents()));
  }

  @Override
  public List<? extends Fluent> getFluents() {
    return unmodifiableFluents;
  }

  @Override
  public List<? extends Action> getActions() {
    return unmodifiableActions;
  }

  @Override
  public State getInitialState() {
    return new Pddl4jState(new fr.uga.pddl4j.problem.State(delegate.getInitialState()), fluents);
  }

  @Override
  public lkh.planning.Condition getGoalCondition() {
    return goalCondition;
  }

  private List<? extends Action> buildActions() {
    List<Pddl4jAction> actions = new ArrayList<>();
    for (fr.uga.pddl4j.problem.operator.Action action : delegate.getActions()) {
      actions.add(new Pddl4jAction(action, delegate, fluents));
    }
    return List.copyOf(actions);
  }

  private void validateNoConditionalEffects() {
    for (fr.uga.pddl4j.problem.operator.Action action : delegate.getActions()) {
      boolean hasConditionalEffects = action.getConditionalEffects().stream()
          .anyMatch(effect -> !effect.getCondition().isEmpty());
      if (hasConditionalEffects) {
        throw new IllegalArgumentException("Conditional effects are not supported: " + delegate.toString(action));
      }
    }
  }
}
