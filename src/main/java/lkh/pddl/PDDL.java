package lkh.pddl;

import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.Parser;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Fluent;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;
import lkh.lts.HashMapLTS;
import lkh.lts.LTS;
import lkh.expression.Expression;
import lkh.por.PartialOrderReducer;
import lkh.utils.Pair;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class PDDL {
  LTS<Integer, String> lts;
  Problem problem;
  @Setter
  boolean reduce;

  public PDDL(String domainFilename, String problemFilename) throws FileNotFoundException {
    Parser parser = new Parser();
    DefaultParsedProblem parsedProblem = parser.parse(domainFilename, problemFilename);
    problem = new DefaultProblem(parsedProblem);
    problem.instantiate();
  }

  public LTS<Integer, String> getLTS() {
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
    List<Fluent> fluents = problem.getFluents();

    problem.getGoal().getPositiveFluents().stream().forEach(fluentIdx -> {
      Fluent fluent = fluents.get(fluentIdx);
      fluentsSet.add(toString(fluent, problem));
    });

    problem.getGoal().getNegativeFluents().stream().forEach(fluentIdx -> {
      Fluent fluent = fluents.get(fluentIdx);
      fluentsSet.add("not " + toString(fluent, problem));
    });

    String stringExpression = String.join(" and ", fluentsSet);
    
    try {
      return Expression.of(stringExpression);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private LTS<Integer, String> buildLTS(Problem problem) {
    LTS<Integer,String> lts = new HashMapLTS<>();
    State init = new State(problem.getInitialState());

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
        PartialOrderReducer por = new PartialOrderReducer(problem);
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
            actionToString(nextAction));
      }
    }

    return lts;
  }

  private Set<Pair<Action, State>> defaultExpand(State state) {
    Set<Pair<Action, State>> result = new HashSet<>();

    for (Action action : problem.getActions().stream().filter(action -> action.isApplicable(state)).collect(Collectors.toSet())) {
      State nextState = new State(state);
      nextState.apply(action.getConditionalEffects());
      result.add(new Pair<>(action, nextState));
    }

    return result;
  }

  private String actionToString(Action a){
    // Build the argument list as a string
    List<String> args = new LinkedList<>();
    for (int id : a.getInstantiations()) {
      args.add(problem.getConstantSymbols().get(id));
    }

    // Print the formatted action name with parameters
    return a.getName() + "(" + String.join(", ", args) + ")";
  }

  private static Set<String> labels(State state, Problem problem) {
    Set<String> result = new HashSet<>();
    List<Fluent> fluents = problem.getFluents();

    state.stream().forEach(fluentIdx -> {
      Fluent fluent = fluents.get(fluentIdx);
      result.add(toString(fluent, problem));
    });

    return result;
  }

  private static String toString(Fluent fluent, Problem problem) {
    int fluentSymbol = fluent.getSymbol();
    StringBuilder fluentStr = new StringBuilder();
    fluentStr.append(problem.getPredicateSymbols().get(fluentSymbol));

    List<String> arguments = new LinkedList<>();
    for (int argIdx : fluent.getArguments()) {
      arguments.add(problem.getConstantSymbols().get(argIdx));
    }
    if (!arguments.isEmpty())
      fluentStr.append("(").append(String.join(", ", arguments)).append(")");
    
    return fluentStr.toString();
  }
}
