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
import lkh.lts.builder.ActionSelectionStrategy;
import lkh.planning.Action;
import lkh.planning.Fluent;
import lkh.planning.Problem;
import lkh.planning.State;

public class StratifiedActionSelectionStrategy implements ActionSelectionStrategy {
  private Problem problem;
  private DirectedGraph<Fluent, DefaultEdge<Fluent>> causalGraph;
  private DirectedGraph<Set<Fluent>, DefaultEdge<Set<Fluent>>> contractedGraph;
  private Map<Action, Integer> layer;

  @Override
  public Collection<? extends Action> selectActions(Action previousAction, State state, Problem problem) {
    if (state == null) {
      throw new IllegalArgumentException("Null state");
    }
    if (problem == null) {
      throw new IllegalArgumentException("Null problem");
    }

    ensureInitialized(problem);
    int previousLayer = layer.getOrDefault(previousAction, 0);
    Set<Action> result = new HashSet<>();

    for (Action candidate : problem.getApplicableActions(state)) {
      if (layer.get(candidate) >= previousLayer || followUpAction(previousAction, candidate)) {
        result.add(candidate);
      }
    }

    return result;
  }

  private void ensureInitialized(Problem problem) {
    if (this.problem == problem) {
      return;
    }

    this.problem = problem;
    buildCausalGraph();
    buildContractedGraph();
    stratify();
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
    Set<Set<Fluent>> sccs = DirectedGraphOperations.getSCCs(causalGraph);
    contractedGraph.addVertices(sccs);

    for (Set<Fluent> sourceComponent : sccs) {
      for (Set<Fluent> targetComponent : sccs) {
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

    assignActionLayers(componentLayer);
  }

  private void assignActionLayers(Map<Set<Fluent>, Integer> componentLayer) {
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
    if (first == null) {
      return false;
    }

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
