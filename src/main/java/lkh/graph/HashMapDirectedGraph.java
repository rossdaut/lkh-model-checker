package lkh.graph;

import lkh.graph.edge.Edge;

import java.util.*;


/**
 * A directed graph implementation backed by a {@code HashMap}.
 * Vertices are represented as keys in the map, and edges connecting these vertices
 * are maintained in a {@code Set} as the corresponding values.
 *
 * @param <V> The type of the vertices in the graph.
 * @param <E> The type of the edges in the graph, constrained to extend the Edge interface.
 */
public class HashMapDirectedGraph<V, E extends Edge<V>> implements DirectedGraph<V, E> {
  private final Map<V, Set<E>> map = new HashMap<>();

  @Override
  public void addVertex(V vertex) {
    if (vertex == null) throw new NullPointerException("null vertex");
    map.putIfAbsent(vertex, new HashSet<>());
  }

  @Override
  public void addEdge(E edge) {
    if (edge == null) throw new NullPointerException("null edge");
    if (!map.containsKey(edge.getSource())) addVertex(edge.getSource());
    if (!map.containsKey(edge.getTarget())) addVertex(edge.getTarget());
    map.get(edge.getSource()).add(edge);
  }

  @Override
  public void addVertices(Set<V> vertices) {
    if (vertices == null) throw new NullPointerException("null vertices");
    for (V vertex : vertices) {
      addVertex(vertex);
    }
  }

  @Override
  public void addEdges(Set<E> edges) {
    if (edges == null) throw new NullPointerException("null edges");
    for (E edge : edges) {
      addEdge(edge);
    }
  }

  @Override
  public boolean containsVertex(V vertex) {
    return map.containsKey(vertex);
  }

  @Override
  public boolean containsEdge(V from, V to) {
    if (!map.containsKey(from)) return false;

    for (E edge : map.get(from)) {
      if (edge.getTarget().equals(to)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Set<V> getNeighbors(V vertex) {
    Set<V> neighbors = new HashSet<>();

    if (map.containsKey(vertex)) {
      for (E edge : map.get(vertex)) {
        neighbors.add(edge.getTarget());
      }
    }
    return neighbors;
  }

  @Override
  public Set<V> getVertices() {
    return map.keySet();
  }

  @Override
  public int getInDegree(V vertex) {
    int inDegree = 0;

    for (V v : map.keySet()) {
      for (E edge : map.get(v)) {
        if (edge.getTarget().equals(vertex)) {
          inDegree++;
        }
      }
    }
    return inDegree;
  }

  @Override
  public int getOutDegree(V vertex) {
    if (!map.containsKey(vertex)) return 0;

    return map.get(vertex).size();
  }

  @Override
  public List<V> getOutgoingNeighbors(V vertex) {
    List<V> neighbors = new ArrayList<>();

    if (map.containsKey(vertex)) {
      for (E edge : map.get(vertex)) {
        neighbors.add(edge.getTarget());
      }
    }
    return neighbors;
  }

  @Override
  public List<V> getIncomingNeighbors(V vertex) {
    List<V> neighbors = new ArrayList<>();

    for (V v : map.keySet()) {
      for (E edge : map.get(v)) {
        if (edge.getTarget().equals(vertex)) {
          neighbors.add(v);
        }
      }
    }
    return neighbors;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    HashMapDirectedGraph<?, ?> that = (HashMapDirectedGraph<?, ?>) obj;
    return Objects.equals(map, that.map);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    for (V vertex : map.keySet()) {
      builder.append(vertex).append(" -> ");

      for (E edge : map.get(vertex)) {
        builder.append(edge.getTarget()).append(", ");
      }
      if (builder.charAt(builder.length() - 2) == ',') {
        builder.setLength(builder.length() - 2); // Remove trailing comma
      }
      builder.append("\n");
    }
    return builder.toString();
  }
}

