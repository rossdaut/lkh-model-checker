package lkh.graph;
import lkh.graph.edge.Edge;

import java.util.List;

/**
 * Represents a directed graph data structure where edges have a specific direction from source to target vertices.
 * This interface extends the base Graph interface to provide specialized operations for directed graphs, including
 * methods to handle incoming and outgoing edges, calculate in-degree and out-degree of vertices, and manage
 * directional relationships between vertices.
 *
 * @param <V> The type of vertices in the directed graph
 * @param <E> The type of edges in the directed graph, which must extend the Edge interface
 */
public interface DirectedGraph<V, E extends Edge<V>> extends Graph<V, E> {

    /**
     * Retrieves the in-degree of the specified vertex in the directed graph.
     * The in-degree is the number of incoming edges to the vertex.
     *
     * @param vertex The vertex whose in-degree is to be calculated.
     * @return The in-degree of the vertex.
     */
    int getInDegree(V vertex);

    /**
     * Retrieves the out-degree of the specified vertex in the directed graph.
     * The out-degree is the number of outgoing edges from the vertex.
     *
     * @param vertex The vertex whose out-degree is to be calculated.
     * @return The out-degree of the vertex.
     */
    int getOutDegree(V vertex);

    /**
     * Retrieves a list of edges from the specified vertex.
     *
     * @param vertex The vertex to find outgoing edges.
     * @return A list of edged that are outgoing of the specified vertex.
     */
    List<E> getOutgoingEdges(V vertex);

    /**
     * Retrieves a list of neighbors that represent the vertices directly reachable
     * from the specified vertex (outgoing neighbors).
     *
     * @param vertex The vertex to find outgoing neighbors.
     * @return A list of vertices that are outgoing neighbors of the specified vertex.
     */
    List<V> getOutgoingNeighbors(V vertex);

    /**
     * Retrieves a list of neighbors that represent the vertices directly reachable
     * from the specified vertex (outgoing neighbors) filtered by the given edge predicate.
     *
     * @param vertex The vertex to find outgoing neighbors.
     * @param edgePredicate A function that returns true for edges that should be considered.
     * @return A list of vertices that are outgoing neighbors of the specified vertex
     *         based on the provided edge predicate.
     */
    List<V> getOutgoingNeighbors(V vertex, java.util.function.Predicate<E> edgePredicate);

    /**
     * Retrieves a list of neighbors that represent the vertices that can directly
     * reach the specified vertex (incoming neighbors).
     *
     * @param vertex The vertex to find incoming neighbors.
     * @return A list of vertices that are incoming neighbors of the specified vertex.
     */
    List<V> getIncomingNeighbors(V vertex);

    /**
     * Removes an edge from the graph.
     *
     * @param edge The edge to be removed from the graph.
     * @return true if the edge was removed, false if it was not found.
     */
    boolean removeEdge(E edge);

    /**
     * Removes all edges that match the given predicate from the specified vertex.
     *
     * @param vertex The source vertex from which to remove outgoing edges.
     * @param predicate A function that returns true for edges that should be removed.
     */
    void removeOutgoingEdgesIf(V vertex, java.util.function.Predicate<E> predicate);
}
