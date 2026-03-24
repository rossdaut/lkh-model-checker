package lkh.por;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lkh.graph.DirectedGraph;
import lkh.graph.DirectedGraphOperations;
import lkh.graph.HashMapDirectedGraph;
import lkh.graph.edge.DefaultEdge;
import lkh.planning.Action;
import lkh.planning.Fluent;
import lkh.planning.Problem;
import lkh.planning.State;
import lkh.utils.Pair;

public class StratifiedReducer {
  private final Problem problem;
  private DirectedGraph<Fluent, DefaultEdge<Fluent>> causalGraph;
  private DirectedGraph<Set<Fluent>, DefaultEdge<Set<Fluent>>> contractedGraph;
  private Map<Action, Integer> layer;

  public StratifiedReducer(Problem problem) {
    this.problem = problem;
    buildCausalGraph();
    buildContractedGraph();
    stratify();
  }

  public Set<Pair<Action, State>> stratifiedExpansion(Action action, State state) {
    if (state == null) {
      throw new IllegalArgumentException("Null state");
    }
    Set<Pair<Action, State>> result = new HashSet<>();

    for (Action action2 : problem.getActions().stream().filter(candidate -> candidate.isApplicable(state)).toList()) {
      if (layer.get(action2) >= layer.get(action) || followUpAction(action, action2)) {
        State nextState = state.copy();
        nextState.apply(action2);
        result.add(new Pair<>(action2, nextState));
      }
    }

    return result;
  }

  private void buildCausalGraph() {
    causalGraph = new HashMapDirectedGraph<>();
    causalGraph.addVertices(new HashSet<>(problem.getFluents()));

    for (Action action : problem.getActions()) {
      AnalyzableAction analyzableAction = toAnalyzableAction(action);
      for (Fluent transition : analyzableAction.getTransitionFluents()) {
        for (Fluent dependent : analyzableAction.getDependentFluents()) {
          if (!transition.equals(dependent)) {
            causalGraph.addEdge(new DefaultEdge<>(transition, dependent));
          }
        }
      }
      for (Fluent affected : analyzableAction.getAffectedFluents()) {
        for (Fluent transition : analyzableAction.getTransitionFluents()) {
          if (!affected.equals(transition)) {
            causalGraph.addEdge(new DefaultEdge<>(affected, transition));
          }
        }
      }
    }
  }

  private void buildContractedGraph() {
    contractedGraph = new HashMapDirectedGraph<>();
    Set<Set<Fluent>> stronglyConnectedComponents = DirectedGraphOperations.getSCCs(causalGraph);
    contractedGraph.addVertices(stronglyConnectedComponents);

    for (Set<Fluent> sourceComponent : stronglyConnectedComponents) {
      for (Set<Fluent> targetComponent : stronglyConnectedComponents) {
        if (sourceComponent.equals(targetComponent)) {
          continue;
        }

        for (Fluent fluent : sourceComponent) {
          if (causalGraph.getNeighbors(fluent).stream().anyMatch(targetComponent::contains)) {
            contractedGraph.addEdge(new DefaultEdge<>(sourceComponent, targetComponent));
          }
        }
      }
    }
  }

  private void stratify() {
    Map<Set<Fluent>, Integer> componentLayer = new HashMap<>();

    for (Set<Fluent> component : contractedGraph.getVertices()) {
      componentLayer.put(component, 1);
    }

    for (Set<Fluent> component : DirectedGraphOperations.getTopologicalSort(contractedGraph)) {
      for (Set<Fluent> predecessor : contractedGraph.getIncomingNeighbors(component)) {
        componentLayer.put(component, Math.max(componentLayer.get(component), componentLayer.get(predecessor) + 1));
      }
    }

    actionLayer(componentLayer);
  }

  private void actionLayer(Map<Set<Fluent>, Integer> componentLayer) {
    layer = new HashMap<>();
    layer.put(null, 0);
    for (Action action : problem.getActions()) {
      Collection<Fluent> transitions = toAnalyzableAction(action).getTransitionFluents();
      Fluent fluent = transitions.stream().findFirst().orElse(null);
      if (fluent == null) {
        layer.put(action, Integer.MAX_VALUE);
      } else {
        for (Set<Fluent> component : contractedGraph.getVertices()) {
          if (component.contains(fluent)) {
            layer.put(action, componentLayer.get(component));
            break;
          }
        }
      }
    }
  }

  private boolean followUpAction(Action first, Action second) {
    AnalyzableAction firstAction = toAnalyzableAction(first);
    AnalyzableAction secondAction = toAnalyzableAction(second);

    Set<Fluent> firstCondition = new HashSet<>(firstAction.getAffectedFluents());
    firstCondition.retainAll(secondAction.getDependentFluents());
    if (!firstCondition.isEmpty()) {
      return true;
    }

    Set<Fluent> secondCondition = new HashSet<>(firstAction.getAffectedFluents());
    secondCondition.retainAll(secondAction.getAffectedFluents());
    return !secondCondition.isEmpty();
  }

  private static AnalyzableAction toAnalyzableAction(Action action) {
    if (!(action instanceof AnalyzableAction analyzableAction)) {
      throw new IllegalArgumentException("Action must implement AnalyzableAction");
    }
    return analyzableAction;
  }
}
