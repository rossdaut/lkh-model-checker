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

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

public class PDDL {
  public static LTS<Integer, String> asLTS(String domainFilename, String problemFilename) throws FileNotFoundException {
    Parser parser = new Parser();
    DefaultParsedProblem parsedProblem = parser.parse(domainFilename, problemFilename);
    Problem problem = new DefaultProblem(parsedProblem);
    problem.instantiate();
    return buildLTS(problem);
  }

  private static LTS<Integer, String> buildLTS(Problem problem) {
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
      int fluentSymbol = fluent.getSymbol();
      StringBuilder fluentStr = new StringBuilder();
      fluentStr.append(problem.getPredicateSymbols().get(fluentSymbol));

      List<String> arguments = new LinkedList<>();
      for (int argIdx : fluent.getArguments()) {
        arguments.add(problem.getConstantSymbols().get(argIdx));
      }
      if (!arguments.isEmpty())
        fluentStr.append("(").append(String.join(", ", arguments)).append(")");
      result.add(fluentStr.toString());
    });

    return result;
  }
}
