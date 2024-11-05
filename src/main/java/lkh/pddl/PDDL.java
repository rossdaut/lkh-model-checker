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

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class PDDL {
  LTS<Integer, String> lts;
  Problem problem;

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

  public Expression getGoal() {
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

    Queue<State> unvisitedStates = new LinkedList<>();
    unvisitedStates.add(init);
    Map<State, Integer> indexMap = new HashMap<>();
    indexMap.put(init, 0);

    while(!unvisitedStates.isEmpty()){
      State currentState = unvisitedStates.poll();
      lts.addState(indexMap.get(currentState), labels(currentState, problem));

      for(Action action : problem.getActions().stream().filter(action->action.isApplicable(currentState)).collect(Collectors.toSet())){
        State nextState = new State(currentState);
        StringBuilder actionString = new StringBuilder(action.getName());
        nextState.apply(action.getConditionalEffects());

        if (!indexMap.containsKey(nextState)) {
          indexMap.put(nextState, indexMap.size());
          unvisitedStates.add(nextState);
        }

        String instances = instancesString(action, problem);
        if (!instances.isEmpty())
          actionString.append("(").append(instances).append(")");

        lts.addTransition(
                indexMap.get(currentState),
                indexMap.get(nextState),
                actionString.toString());
      }
    }

    return lts;
  }

  private static String instancesString(Action action, Problem problem) {
    List<String> instancesList = new LinkedList<>();

    Arrays.stream(action.getInstantiations()).forEach(instantiation -> {
      instancesList.add(problem.getConstantSymbols().get(instantiation));
    });

    return String.join(", ", instancesList);
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
