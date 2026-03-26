package lkh.lts.builder;

import lkh.lts.HashMapLTS;
import lkh.lts.LTS;
import lkh.expression.Expression;
import lkh.planning.Action;
import lkh.planning.Condition;
import lkh.planning.Fluent;
import lkh.planning.Problem;
import lkh.planning.State;
import lkh.planning.pddl4j.Pddl4jProblem;
import lkh.por.StratifiedReducer;
import lkh.utils.Pair;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.util.*;

public class PDDL implements LTSBuilder {
  LTS<Integer, String> lts;
  Problem problem;
  @Setter
  boolean reduce;

  public PDDL(String domainFilename, String problemFilename) throws FileNotFoundException {
    problem = new Pddl4jProblem(domainFilename, problemFilename);
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
    Expression[] props = lts.getLabels(getInitialState()).stream().map(Expression::prop).toArray(Expression[]::new);
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

      lts.addState(indexMap.get(state), labels(state, problem));

      Set<Pair<Action, State>> nextStates;
      if (reduce) {
        StratifiedReducer por = new StratifiedReducer(problem);
        nextStates = por.stratifiedExpansion(action, state);
      } else {
        nextStates = defaultExpand(state);
      }

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

  private Set<Pair<Action, State>> defaultExpand(State state) {
    Set<Pair<Action, State>> result = new HashSet<>();

    for (Action action : problem.getActions().stream().filter(candidate -> candidate.isApplicable(state)).toList()) {
      State nextState = state.copy();
      nextState.apply(action);
      result.add(new Pair<>(action, nextState));
    }

    return result;
  }

  private static Set<String> labels(State state, Problem problem) {
    Set<String> result = new HashSet<>();
    state.getFluents().forEach(fluent -> result.add(fluent.toString()));

    return result;
  }
}
