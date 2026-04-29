package lkh.lts.builder;

import lkh.lts.HashMapLTS;
import lkh.lts.LTS;
import lkh.expression.Expression;
import lkh.planning.Action;
import lkh.planning.Condition;
import lkh.planning.Problem;
import lkh.planning.State;
import lkh.planning.pddl4j.Pddl4jProblem;
import lkh.utils.Pair;

import java.io.FileNotFoundException;
import java.util.*;

public class PDDL implements LTSBuilder {
  private LTS<Integer, String> lts;
  private final Problem problem;
  private ActionSelectionStrategy actionSelectionStrategy;

  public PDDL(String domainFilename, String problemFilename) throws FileNotFoundException {
    this(domainFilename, problemFilename, new DefaultActionSelectionStrategy());
  }

  public PDDL(String domainFilename, String problemFilename, ActionSelectionStrategy actionSelectionStrategy) throws FileNotFoundException {
    this.problem = new Pddl4jProblem(domainFilename, problemFilename);
    setActionSelectionStrategy(actionSelectionStrategy);
  }

  public LTS<Integer, String> buildLTS() {
    if (lts == null)
      lts = buildLTS(problem);

    return lts;
  }

  public int getInitialState() {
    return 0;
  }

  public Expression getInitialExpression() {
    State initial = problem.getInitialState();
    Expression[] props = problem.getFluents().stream()
        .map(fluent -> initial.holds(lkh.planning.Literal.positive(fluent))
            ? Expression.prop(fluent.toString())
            : Expression.not(Expression.prop(fluent.toString())))
        .toArray(Expression[]::new);
    return Expression.and(props);
  }

  public Expression getGoalExpression() {
    Set<String> fluentsSet = new HashSet<>();
    Condition goal = problem.getGoalCondition();
    goal.getPositiveFluents().forEach(fluent -> fluentsSet.add(fluent.toString()));
    goal.getNegativeFluents().forEach(fluent -> fluentsSet.add("not " + fluent));

    String stringExpression = String.join(" and ", fluentsSet);
    
    try {
      return Expression.of(stringExpression);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void setActionSelectionStrategy(ActionSelectionStrategy actionSelectionStrategy) {
    this.actionSelectionStrategy = Objects.requireNonNull(actionSelectionStrategy, "actionSelectionStrategy");
    this.lts = null;
  }

  private LTS<Integer, String> buildLTS(Problem problem) {
    LTS<Integer,String> lts = new HashMapLTS<>();
    State init = problem.getInitialState();

    Queue<Pair<Action, State>> unvisitedStates = new LinkedList<>();
    unvisitedStates.add(new Pair<>(null, init));
    Map<State, Integer> indexMap = new HashMap<>();
    indexMap.put(init, 0);

    while (!unvisitedStates.isEmpty()) {
      Pair<Action, State> pair = unvisitedStates.poll();
      State state = pair.value();
      Action action = pair.key();

      lts.addState(indexMap.get(state), labels(state));

      Set<Pair<Action, State>> nextStates = expand(action, state, problem);

      for (Pair<Action, State> nextPair : nextStates) {
        State nextState = nextPair.value();
        Action nextAction = nextPair.key();

        if (!indexMap.containsKey(nextState)) {
          indexMap.put(nextState, indexMap.size());
          unvisitedStates.add(nextPair);
        }
        lts.addTransition(
            indexMap.get(state),
            indexMap.get(nextState),
            nextAction.getName());
      }
    }

    return lts;
  }

  private Set<Pair<Action, State>> expand(Action previousAction, State state, Problem problem) {
    Set<Pair<Action, State>> result = new HashSet<>();

    for (Action action : actionSelectionStrategy.selectActions(previousAction, state, problem)) {
      State nextState = state.copy();
      nextState.apply(action);
      result.add(new Pair<>(action, nextState));
    }

    return result;
  }

  private static Set<String> labels(State state) {
    Set<String> result = new HashSet<>();
    state.getFluents().forEach(fluent -> result.add(fluent.toString()));

    return result;
  }
}
