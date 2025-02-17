package lkh.graph;

import lkh.graph.edge.Edge;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectedGraphOperations {

  /**
   * Identifies and returns the strongly connected components (SCCs) of a directed graph.
   * A strongly connected component is a maximal subgraph where any two vertices are
   * reachable from one another.
   *
   * @param <V> The type of vertices in the graph.
   * @param <E> The type of edges in the graph, extending the Edge interface.
   * @param graph The directed graph on which to perform the analysis of strongly connected components.
   * @return A set of sets of vertices, where each inner set represents a strongly connected component.
   */
  public static <V, E extends Edge<V>> Set<Set<V>> getSCCs(DirectedGraph<V, E> graph) {
    AtomicInteger id = new AtomicInteger(0);
    Map<V,Integer> ids = new HashMap<>();
    Map<V,Integer> low = new HashMap<>();
    Map<V,Boolean> onStack = new HashMap<>();
    Stack<V> stack = new Stack<>();

    // Initialize all structures
    for (V v : graph.getVertices()) {
      ids.put(v, -1);
      low.put(v, -1);
      onStack.put(v, false);
    }

    // Recursive start
    for (V v : graph.getVertices()) {
      if (ids.get(v) == -1) {
        getSCCsUtil(graph, v, id, ids, low, onStack, stack);
      }
    }

    // Extract SCC's
    // For this, collect keys with same value from low map
    Map<Integer, Set<V>> result = new HashMap<>();
    for (V v : graph.getVertices()) {
      Integer lowValue = low.get(v);
      if (!result.containsKey(lowValue)) result.put(lowValue, new HashSet<>());
      result.get(lowValue).add(v);
    }

    return new HashSet<>(result.values());
  }

  /**
   * Recursively identifies and processes Strongly Connected Components (SCCs) within a directed graph
   * using Tarjan's algorithm. This method updates discovery and low-link values for vertices, pushes
   * vertices onto a stack, and extracts SCCs when appropriate.
   *
   * @param <V>       The type representing vertices in the graph.
   * @param <E>       The type representing edges in the graph, extending the {@code Edge<V>} interface.
   * @param graph     The directed graph to be processed.
   * @param v         The current vertex being visited.
   * @param id        The discovery index to assign to the vertex.
   * @param ids       A map keeping track of discovery indices for vertices.
   * @param low       A map storing the lowest discovery index reachable from each vertex.
   * @param onStack   A map indicating whether each vertex is currently on the stack.
   * @param stack     A stack used during the DFS to keep track of visited vertices.
   */
  private static <V, E extends Edge<V>> void getSCCsUtil(DirectedGraph<V, E> graph, V v, AtomicInteger id, Map<V, Integer> ids, Map<V, Integer> low, Map<V, Boolean> onStack, Stack<V> stack) {
    stack.push(v);
    ids.put(v, id.get());
    low.put(v, id.getAndIncrement());
    onStack.put(v, true);

    for (V n : graph.getNeighbors(v)) {
      // Recursive call
      if (ids.get(n) == -1)  getSCCsUtil(graph, n, id, ids, low, onStack, stack);
      // Low update if explored neighbor is on the stack
      if (onStack.get(n))    low.put(v, Math.min(low.get(v), low.get(n)));
    }

    // If v is the SCC's initializer, then remove all SCC's members of the stack
    if (ids.get(v).equals(low.get(v))) {
      while (true) {
        V n = stack.pop();
        onStack.put(n, false);

        if (n.equals(v)) break;
        else low.put(n, low.get(v));
      }
    }
  }

  /**
   * Computes and returns a topological sort of the vertices in the given directed graph.
   * A topological sort is a linear ordering of a graph's vertices such that for every
   * directed edge (u, v), vertex u appears before vertex v in the ordering.
   *
   * @param <V> The type of vertices in the graph.
   * @param <E> The type of edges in the graph, extending the {@code Edge<V>} interface.
   * @param graph The directed graph for which to compute the topological sort.
   *              The graph must be a Directed Acyclic Graph (DAG) for the sort to succeed.
   * @return A list of vertices representing the topological order. The list will be empty
   *         if the graph contains cycles or is empty.
   */
  public static <V, E extends Edge<V>> List<V> getTopologicalSort(DirectedGraph<V, E> graph) {
    List<V> result = new ArrayList<>();
    Set<V> visited = new HashSet<>();
    Stack<V> stack = new Stack<>();

    for(V v : graph.getVertices()) {
      if(!visited.contains(v)){
        getTopologicalSortUtil(v, graph, visited, stack);
      }
    }

    while(!stack.isEmpty()){
      result.add(stack.pop());
    }
    return result;
  }

  /**
   * A utility method that performs a depth-first traversal of the graph to assist
   * in topological sorting. The method marks the given vertex as visited, recursively
   * explores its unvisited neighbors, and pushes the vertex onto the stack when its
   * neighbors have been fully processed.
   *
   * @param <V>     The type of vertices in the directed graph.
   * @param <E>     The type of edges in the directed graph, extending the {@code Edge<V>} interface.
   * @param v       The current vertex being processed.
   * @param graph   The directed graph containing the vertices and edges.
   * @param visited A set of vertices that have been visited during the traversal.
   * @param stack   A stack to store the topological order of the vertices.
   */
  private static <V, E extends Edge<V>> void getTopologicalSortUtil(V v, DirectedGraph<V, E> graph, Set<V> visited, Stack<V> stack) {
    visited.add(v);
    for(V n : graph.getNeighbors(v)){
      if(!visited.contains(n)){
        getTopologicalSortUtil(n, graph, visited, stack);
      }
    }
    stack.push(v);
  }
}
