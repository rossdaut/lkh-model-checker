package lkh.planning.pddl4j;

import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.Parser;
import fr.uga.pddl4j.planners.LogLevel;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.util.BitVector;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import lkh.planning.Action;
import lkh.planning.Condition;
import lkh.planning.Fluent;
import lkh.planning.Problem;
import lkh.planning.State;

public class Pddl4jProblem implements Problem {
  private final fr.uga.pddl4j.problem.Problem delegate;
  private final List<Pddl4jFluent> fluents;
  private final List<Pddl4jAction> actions;
  private final List<? extends Fluent> unmodifiableFluents;
  private final List<? extends Action> unmodifiableActions;
  private final Condition goalCondition;

  public Pddl4jProblem(String domainFilename, String problemFilename) throws FileNotFoundException {
    Parser parser = new Parser();
    parser.setLogLevel(LogLevel.OFF);
    DefaultParsedProblem parsedProblem = parser.parse(domainFilename, problemFilename);
    delegate = new DefaultProblem(parsedProblem);
    delegate.instantiate();
    validateNoConditionalEffects();
    fluents = buildFluents(delegate);
    actions = buildActions(delegate);
    unmodifiableFluents = Collections.unmodifiableList(fluents);
    unmodifiableActions = Collections.unmodifiableList(actions);
    goalCondition = new Pddl4jCondition(delegate.getGoal(), this);
  }

  fr.uga.pddl4j.problem.Problem unwrap() {
    return delegate;
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
    return new Pddl4jState(new fr.uga.pddl4j.problem.State(delegate.getInitialState()), this);
  }

  @Override
  public Condition getGoalCondition() {
    return goalCondition;
  }

  Collection<Fluent> wrapFluents(BitVector bitVector) {
    LinkedHashSet<Fluent> wrapped = new LinkedHashSet<>();
    bitVector.stream().forEach(index -> wrapped.add(fluents.get(index)));
    return Collections.unmodifiableCollection(wrapped);
  }

  Collection<Fluent> wrapFluents(fr.uga.pddl4j.problem.State state) {
    LinkedHashSet<Fluent> wrapped = new LinkedHashSet<>();
    state.stream().forEach(index -> wrapped.add(fluents.get(index)));
    return Collections.unmodifiableCollection(wrapped);
  }

  private List<Pddl4jFluent> buildFluents(fr.uga.pddl4j.problem.Problem problem) {
    List<Pddl4jFluent> wrapped = new ArrayList<>();
    int index = 0;
    for (fr.uga.pddl4j.problem.Fluent fluent : problem.getFluents()) {
      wrapped.add(new Pddl4jFluent(renderFluent(fluent, problem), index));
      index++;
    }
    return wrapped;
  }

  private List<Pddl4jAction> buildActions(fr.uga.pddl4j.problem.Problem problem) {
    List<Pddl4jAction> wrapped = new ArrayList<>();
    for (fr.uga.pddl4j.problem.operator.Action action : problem.getActions()) {
      wrapped.add(new Pddl4jAction(action, this));
    }
    return wrapped;
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

  private static String renderFluent(fr.uga.pddl4j.problem.Fluent fluent, fr.uga.pddl4j.problem.Problem problem) {
    int fluentSymbol = fluent.getSymbol();
    StringBuilder fluentStr = new StringBuilder();
    fluentStr.append(problem.getPredicateSymbols().get(fluentSymbol));

    List<String> arguments = new ArrayList<>();
    for (int argIdx : fluent.getArguments()) {
      arguments.add(problem.getConstantSymbols().get(argIdx));
    }
    if (!arguments.isEmpty()) {
      fluentStr.append("(").append(String.join(", ", arguments)).append(")");
    }

    return fluentStr.toString();
  }
}
