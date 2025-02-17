package lkh.graph.edge;

/**
 * Represents a generic edge in a graph, connecting two vertices.
 *
 * @param <V> The type of the vertices connected by this edge.
 */
public interface Edge<V> {
  /**
   * Retrieves the source vertex of the edge.
   *
   * @return The source vertex of this edge.
   */
  V getSource();

  /**
   * Retrieves the target vertex of the edge.
   *
   * @return The target vertex connected by this edge.
   */
  V getTarget();
}
