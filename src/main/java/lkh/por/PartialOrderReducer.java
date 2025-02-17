package lkh.por;

import java.util.*;
import java.util.stream.Collectors;

import fr.uga.pddl4j.problem.Fluent;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;

import lkh.graph.DirectedGraph;
import lkh.graph.DirectedGraphOperations;
import lkh.graph.HashMapDirectedGraph;
import lkh.graph.edge.DefaultEdge;

import lkh.utils.Pair;
import lombok.EqualsAndHashCode;

public class PartialOrderReducer {
  private final Problem problem;
  private final Map<Action, SASAction> actionsMap;
  private final Map<Fluent, String> fluentsMap;
  private DirectedGraph<String, DefaultEdge<String>> causalGraph;
  private DirectedGraph<Set<String>, DefaultEdge<Set<String>>> contractedGraph;
  private Map<Action, Integer> layer;

  public PartialOrderReducer(Problem problem) {
    this.problem = problem;

    fluentsMap = problem.getFluents().stream()
        .collect(Collectors.toMap(
            fluent -> fluent,
            problem::toString
        ));

    actionsMap = problem.getActions().stream()
        .collect(Collectors.toMap(
            action -> action,
            action -> new SASAction(problem, action)
        ));

    buildCausalGraph();
    buildContractedGraph();
    stratify();
  }

  public Set<Pair<Action,State>> stratifiedExpansion(Action action, State state) {
    if (state == null) { throw new IllegalArgumentException("Null state"); }
    Set<Pair<Action,State>> result = new HashSet<>();

    for (Action action2 : problem.getActions().stream().filter(a -> a.isApplicable(state)).collect(Collectors.toSet())) {
      if (layer.get(action2) >= layer.get(action) || followUpAction(action, action2)) {
        State nextState = new State(state);
        nextState.apply(action2.getConditionalEffects());
        result.add(new Pair<>(action2, nextState));
      }
    }

    return result;
  }

  private void buildCausalGraph() {
    causalGraph = new HashMapDirectedGraph<>();
    causalGraph.addVertices(new HashSet<>(fluentsMap.values()));

    for (String fluent : causalGraph.getVertices()) {
      for (String fluent2 : causalGraph.getVertices()) {
        if (fluent.equals(fluent2)) continue;
        for (SASAction action : actionsMap.values()) {
          if ((action.transition.contains(fluent) && action.dependent.contains(fluent2)) || (action.affected.contains(fluent) && action.transition.contains(fluent2))) {
            causalGraph.addEdge(new DefaultEdge<>(fluent, fluent2));
          }
        }
      }
    }
  }

  private void buildContractedGraph() {
    contractedGraph = new HashMapDirectedGraph<>();
    Set<Set<String>> SCCs = DirectedGraphOperations.getSCCs(causalGraph);

    for(Set<String> SCC : SCCs) {
      for(Set<String> SCC2 : SCCs) {
        if(SCC.equals(SCC2)) continue;

        for(String fluent : SCC) {
          if(causalGraph.getNeighbors(fluent).stream().anyMatch(SCC2::contains)) {
            contractedGraph.addEdge(new DefaultEdge<>(SCC, SCC2));
          }
        }
      }
    }
  }

  private void stratify() {
    Map<Set<String>, Integer> sccsLayer = new HashMap<>();

    for(Set<String> SCC : contractedGraph.getVertices()) {
      sccsLayer.put(SCC, 1);
    }

    for(Set<String> SCC : DirectedGraphOperations.getTopologicalSort(contractedGraph)) {
      for(Set<String> SCC2 : contractedGraph.getIncomingNeighbors(SCC)) {
        sccsLayer.put(SCC, Math.max(sccsLayer.get(SCC), sccsLayer.get(SCC2)) + 1);
      }
    }

    actionLayer(sccsLayer);
  }

  private void actionLayer(Map<Set<String>, Integer> sccsLayer) {
    layer = new HashMap<>();
    layer.put(null, 0);
    for(Action action : problem.getActions()) {
      String fluent = actionsMap.get(action).transition.stream().findFirst().orElse(null);
      if (fluent == null) {
        layer.put(action, Integer.MAX_VALUE);
      } else {
        for (Set<String> SCC : contractedGraph.getVertices()) {
          if (SCC.contains(fluent)) {
            layer.put(action, sccsLayer.get(SCC));
            break;
          }
        }
      }
    }
  }

  private boolean followUpAction(Action a, Action b) {
    SASAction action1 = actionsMap.get(a);
    SASAction action2 = actionsMap.get(b);

    Set<String> firstCond = new HashSet<>(action1.affected);
    firstCond.retainAll(action2.dependent);

    if(!firstCond.isEmpty()) return true;

    Set<String> secondCond = new HashSet<>(action1.affected);
    secondCond.retainAll(action2.affected);

    return !secondCond.isEmpty();
  }

  @EqualsAndHashCode
  public static class SASAction {
    private final String name;
    private final HashSet<String> dependent;
    private HashSet<String> transition;
    private final HashSet<String> affected;

    public SASAction(Problem problem, Action pddlAction) {

      // Build the argument list as a string
      List<String> args = new LinkedList<>();
      for (int id : pddlAction.getInstantiations()) {
        args.add(problem.getConstantSymbols().get(id));
      }

      // Print the formatted action name with parameters
      this.name = "(" + pddlAction.getName() + " " + String.join(" ", args) + ")";
      this.dependent = new HashSet<>();
      this.transition = new HashSet<>();
      this.affected = new HashSet<>();

      List<Fluent> fluents = problem.getFluents();
      //dep
      pddlAction.getPrecondition().getPositiveFluents().stream().forEach(fluentIdx -> dependent.add(problem.toString(fluents.get(fluentIdx))));
      pddlAction.getPrecondition().getNegativeFluents().stream().forEach(fluentIdx -> dependent.add(problem.toString(fluents.get(fluentIdx))));

      //aff
      pddlAction.getUnconditionalEffect().getPositiveFluents().stream().forEach(fluentIdx -> affected.add(problem.toString(fluents.get(fluentIdx))));
      pddlAction.getUnconditionalEffect().getNegativeFluents().stream().forEach(fluentIdx -> affected.add(problem.toString(fluents.get(fluentIdx))));

      //trans
      transition = new HashSet<>(affected);
      transition.retainAll(dependent);
    }
  }
}
