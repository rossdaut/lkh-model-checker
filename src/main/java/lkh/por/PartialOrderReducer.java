package lkh.por;

import java.util.*;
import java.util.stream.Collectors;

import fr.uga.pddl4j.problem.Fluent;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.operator.Action;

import lkh.graph.DirectedGraph;
import lkh.graph.DirectedGraphOperations;
import lkh.graph.HashMapDirectedGraph;
import lkh.graph.edge.DefaultEdge;

import lombok.EqualsAndHashCode;
import lombok.Getter;

public class PartialOrderReducer {
  private final Problem problem;
  @Getter
  private Map<Action, SASAction> actionsMap;
  @Getter
  private Map<Fluent, String> fluentsMap;
  @Getter
  private DirectedGraph<String, DefaultEdge<String>> causalGraph;
  @Getter
  DirectedGraph<Set<String>, DefaultEdge<Set<String>>> contractedGraph;
  @Getter
  Map<Set<String>, Integer> layer;

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

  public void buildCausalGraph() {
    causalGraph = new HashMapDirectedGraph<>();
    causalGraph.addVertices(new HashSet<>(fluentsMap.values()));

    for (String fluent : causalGraph.getVertices()) {
      for (String fluent2 : causalGraph.getVertices()) {
        if (fluent.equals(fluent2)) continue;
        for (SASAction action : actionsMap.values()) {
          if ((action.transition.contains(fluent) && action.dependent.contains(fluent2)) || (action.affected.contains(fluent) && action.transition.contains(fluent2))) {
            causalGraph.addEdge(new DefaultEdge<String>(fluent, fluent2));
          }
        }
      }
    }
  }

  public void buildContractedGraph() {
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

  public void stratify() {
    layer = new HashMap<>();

    for(Set<String> SCC : contractedGraph.getVertices()) {
      layer.put(SCC, 1);
    }

    for(Set<String> SCC : DirectedGraphOperations.getTopologicalSort(contractedGraph)) {
      for(Set<String> SCC2 : contractedGraph.getIncomingNeighbors(SCC)) {
        layer.put(SCC, Math.max(layer.get(SCC), layer.get(SCC2)) + 1);
      }
    }
  }

  @EqualsAndHashCode
  public static class SASAction {
    private String name;
    private HashSet<String> dependent;
    private HashSet<String> transition;
    private HashSet<String> affected;

    public SASAction(String name) {
      this.name = name;
      this.dependent = new HashSet<>();
      this.transition = new HashSet<>();
      this.affected = new HashSet<>();
    }

    public SASAction(Problem problem, Action pddlAction) {

      // Get pddlAction parameters
      int[] parameters = pddlAction.getParameters();

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
      pddlAction.getPrecondition().getPositiveFluents().stream().forEach(fluentIdx -> {
        dependent.add(problem.toString(fluents.get(fluentIdx)));
      });
      pddlAction.getPrecondition().getNegativeFluents().stream().forEach(fluentIdx -> {
        dependent.add(problem.toString(fluents.get(fluentIdx)));
      });

      //aff
      pddlAction.getUnconditionalEffect().getPositiveFluents().stream().forEach(fluentIdx -> {
        affected.add(problem.toString(fluents.get(fluentIdx)));
      });
      pddlAction.getUnconditionalEffect().getNegativeFluents().stream().forEach(fluentIdx -> {
        affected.add(problem.toString(fluents.get(fluentIdx)));
      });

      //trans
      transition = new HashSet<>(affected);
      transition.retainAll(dependent);
    }

    public SASAction(String name, HashSet<String> dependent, HashSet<String> affected) {
      this.name = name;
      this.dependent = dependent;
      this.affected = affected;
      transition = new HashSet<>(affected);
      transition.retainAll(dependent);
    }
  }
}
