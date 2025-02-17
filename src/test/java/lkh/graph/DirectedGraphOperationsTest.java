package lkh.graph;

import lkh.graph.edge.DefaultEdge;
import lkh.graph.edge.Edge;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectedGraphOperationsTest {
  @Test
  public void testGetSCCs1() {
    DirectedGraph<Integer, DefaultEdge<Integer>> graph = new HashMapDirectedGraph<>();
    graph.addVertices(new HashSet<>(Set.of(0,1,2,3,4,5,6)));
    graph.addEdge(new DefaultEdge<>(0,1));
    graph.addEdge(new DefaultEdge<>(1,6));
    graph.addEdge(new DefaultEdge<>(1,2));
    graph.addEdge(new DefaultEdge<>(1,4));
    graph.addEdge(new DefaultEdge<>(2,3));
    graph.addEdge(new DefaultEdge<>(3,2));
    graph.addEdge(new DefaultEdge<>(3,4));
    graph.addEdge(new DefaultEdge<>(3,5));
    graph.addEdge(new DefaultEdge<>(4,5));
    graph.addEdge(new DefaultEdge<>(5,4));
    graph.addEdge(new DefaultEdge<>(6,0));
    graph.addEdge(new DefaultEdge<>(6,2));

    Set<Set<Integer>> actual = DirectedGraphOperations.getSCCs(graph);
    Set<Set<Integer>> expected = new HashSet<>();
    expected.add(new HashSet<>(Set.of(0,1,6)));
    expected.add(new HashSet<>(Set.of(2,3)));
    expected.add(new HashSet<>(Set.of(4,5)));

    assertEquals(expected, actual);
  }

  @Test
  public void testGetSCCs2() {
    DirectedGraph<Integer, DefaultEdge<Integer>> graph = new HashMapDirectedGraph<>();
    graph.addVertices(new HashSet<>(Set.of(1,2,3,4,5,6,7)));
    graph.addEdge(new DefaultEdge<>(1,2));
    graph.addEdge(new DefaultEdge<>(2,3));
    graph.addEdge(new DefaultEdge<>(2,4));
    graph.addEdge(new DefaultEdge<>(3,4));
    graph.addEdge(new DefaultEdge<>(3,6));
    graph.addEdge(new DefaultEdge<>(4,1));
    graph.addEdge(new DefaultEdge<>(4,5));
    graph.addEdge(new DefaultEdge<>(5,6));
    graph.addEdge(new DefaultEdge<>(6,7));
    graph.addEdge(new DefaultEdge<>(7,5));

    Set<Set<Integer>> actual = DirectedGraphOperations.getSCCs(graph);
    Set<Set<Integer>> expected = new HashSet<>();
    expected.add(new HashSet<>(Set.of(1,2,3,4)));
    expected.add(new HashSet<>(Set.of(5,6,7)));

    assertEquals(expected, actual);
  }

  @Test
  public void testTopologicalSort1() {
    DirectedGraph<Integer, DefaultEdge<Integer>> graph = new HashMapDirectedGraph<>();
    graph.addVertices(new HashSet<>(Set.of(0,1,2,3,4,5)));
    graph.addEdge(new DefaultEdge<>(2,3));
    graph.addEdge(new DefaultEdge<>(3,1));
    graph.addEdge(new DefaultEdge<>(4,0));
    graph.addEdge(new DefaultEdge<>(4,1));
    graph.addEdge(new DefaultEdge<>(5,0));
    graph.addEdge(new DefaultEdge<>(5,2));

    List<Integer> actual = DirectedGraphOperations.getTopologicalSort(graph);
    assertTrue(checkTopologicalSort(graph, actual));
  }

  @Test
  public void testTopologicalSort2() {
    DirectedGraph<Integer, DefaultEdge<Integer>> graph = new HashMapDirectedGraph<>();
    graph.addVertices(new HashSet<>(Set.of(0,1,2,3,4)));
    graph.addEdge(new DefaultEdge<>(0,1));
    graph.addEdge(new DefaultEdge<>(0,2));
    graph.addEdge(new DefaultEdge<>(0,3));
    graph.addEdge(new DefaultEdge<>(0,4));
    graph.addEdge(new DefaultEdge<>(1,2));
    graph.addEdge(new DefaultEdge<>(2,4));
    graph.addEdge(new DefaultEdge<>(3,2));
    graph.addEdge(new DefaultEdge<>(3,4));

    List<Integer> actual = DirectedGraphOperations.getTopologicalSort(graph);
    assertTrue(checkTopologicalSort(graph, actual));
  }

  private <V, E extends Edge<V>> boolean checkTopologicalSort(DirectedGraph<V, E> graph, List<V> actual) {
    HashSet<V> visited = new HashSet<>();
    for(V v : actual) {
      visited.add(v);
      for(V n : graph.getIncomingNeighbors(v)) {
        if(!visited.contains(n)) return false;
      }
    }
    return true;
  }
}
