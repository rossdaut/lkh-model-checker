package lkh.graph;

import lkh.graph.edge.DefaultEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lkh.graph.edge.Edge;

import java.util.Set;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

public class HashMapDirectedGraphTest {

  private HashMapDirectedGraph<String, Edge<String>> graph;

  @BeforeEach
  public void setUp() {
    graph = new HashMapDirectedGraph<>();
    // Adding vertices
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addVertex("E");
    graph.addVertex("F");
    graph.addVertex("G");
    // Adding edges
    graph.addEdge(new DefaultEdge<>("A", "B"));
    graph.addEdge(new DefaultEdge<>("B", "C"));
    graph.addEdge(new DefaultEdge<>("C", "D"));
    graph.addEdge(new DefaultEdge<>("D", "E"));
    graph.addEdge(new DefaultEdge<>("E", "A"));
    graph.addEdge(new DefaultEdge<>("F", "B"));
    graph.addEdge(new DefaultEdge<>("C", "F"));
    graph.addEdge(new DefaultEdge<>("G", "A"));
    graph.addEdge(new DefaultEdge<>("E", "G"));
    graph.addEdge(new DefaultEdge<>("B", "D"));
  }

  @ParameterizedTest
  @CsvSource({
      "H, true",
      "A, true"
  })
  public void testAddVertex(String vertex, boolean expected) {
    graph.addVertex(vertex);
    assertEquals(expected, graph.containsVertex(vertex));
  }

  @Test
  public void testAddEdgeBetweenExistingVertices() {
    graph.addEdge(new DefaultEdge<>("A", "C"));
    assertTrue(graph.containsEdge("A", "C"));
  }

  @Test
  public void testAddEdgeBetweenExistingAndNonExistingVertex() {
    graph.addEdge(new DefaultEdge<>("A", "Y"));
    assertTrue(graph.containsVertex("Y"));
    assertTrue(graph.containsEdge("A", "Y"));
  }

  @Test
  public void testAddEdgeBetweenNonExistingVertices() {
    graph.addEdge(new DefaultEdge<>("Y", "Z"));
    assertTrue(graph.containsVertex("Y"));
    assertTrue(graph.containsVertex("Z"));
    assertTrue(graph.containsEdge("Y", "Z"));
  }
  
  @ParameterizedTest
  @CsvSource({
      "A, true",
      "B, true",
      "Z, false"
  })
  public void testContainsVertex(String vertex, boolean expected) {
    assertEquals(expected, graph.containsVertex(vertex));
  }

  @ParameterizedTest
  @CsvSource({
      "A, B, true",
      "B, C, true",
      "E, G, true",
      "G, A, true",
      "F, C, false",
      "C, G, false"
  })
  public void testContainsEdge(String source, String target, boolean expected) {
    assertEquals(expected, graph.containsEdge(source, target));
  }

  @ParameterizedTest
  @MethodSource("provideNeighborsData")
  public void testGetNeighbors(String vertex, Set<String> expectedNeighbors) {
    Set<String> neighbors = graph.getNeighbors(vertex);
    assertEquals(expectedNeighbors, neighbors);
  }

  private static Stream<Arguments> provideNeighborsData() {
    return Stream.of(
        Arguments.of("A", Set.of("B")),
        Arguments.of("C", Set.of("D", "F")),
        Arguments.of("Z", Set.of())
    );
  }

  @Test
  public void testGetVertices() {
    Set<String> vertices = graph.getVertices();
    assertEquals(Set.of("A", "B", "C", "D", "E", "F", "G"), vertices);
  }

  @ParameterizedTest
  @CsvSource({
      "A, 2",
      "B, 2",
      "G, 1",
      "Z, 0"
  })
  public void testGetInDegree(String vertex, int expected) {
    assertEquals(expected, graph.getInDegree(vertex));
  }

  @ParameterizedTest
  @CsvSource({
      "A, 1",
      "B, 2",
      "E, 2",
      "Z, 0"
  })
  public void testGetOutDegree(String vertex, int expected) {
    assertEquals(expected, graph.getOutDegree(vertex));
  }

  @ParameterizedTest
  @MethodSource("provideOutgoingNeighborsData")
  public void testGetOutgoingNeighbors(String vertex, List<String> expectedNeighbors) {
    assertEquals(expectedNeighbors, graph.getOutgoingNeighbors(vertex));
  }

  private static Stream<Arguments> provideOutgoingNeighborsData() {
    return Stream.of(
        Arguments.of("A", List.of("B")),
        Arguments.of("B", List.of("C", "D")),
        Arguments.of("E", List.of("A", "G")),
        Arguments.of("Z", List.of())
    );
  }

  @ParameterizedTest
  @MethodSource("provideIncomingNeighborsData")
  public void testGetIncomingNeighbors(String vertex, List<String> expectedNeighbors) {
    assertEquals(expectedNeighbors, graph.getIncomingNeighbors(vertex));
  }

  private static Stream<Arguments> provideIncomingNeighborsData() {
    return Stream.of(
        Arguments.of("G", List.of("E")),
        Arguments.of("B", List.of("A", "F")),
        Arguments.of("D", List.of("B", "C")),
        Arguments.of("Z", List.of())
    );
  }
}
