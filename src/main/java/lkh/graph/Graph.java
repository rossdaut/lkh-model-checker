package lkh.graph;

import lkh.graph.edge.Edge;

import java.util.Set;

/**
 * Represents a generic graph with vertices and edges. The graph can be directed or undirected,
 * depending on the implementation. This interface provides basic functionality to manage vertices
 * and edges, as well as to query relationships within the graph.
 *
 * @param <V> The type of vertices in the graph.
 * @param <E> The type of edges in the graph, which must extend the {@link Edge} interface.
 */
public interface Graph<V, E extends Edge<V>> {
    /**
     * Adds a vertex to the graph. If the vertex already exists in the graph, no action is taken.
     *
     * @param vertex The vertex to be added to the graph.
     */
    void addVertex(V vertex);


    /**
     * Adds an edge to the graph. The edge connects two vertices and contains associated data.
     *
     * @param edge The edge to be added to the graph. The edge must connect vertices
     *             that are already present in the graph or will be added during this operation.
     */
    void addEdge(E edge);


    /**
     * Adds a collection of vertices to the graph. If a vertex in the collection
     * already exists in the graph, no action is taken for that vertex.
     *
     * @param vertices A set of vertices to be added to the graph. Each vertex in the set may
     *                 represent a unique element of type V.
     */
    void addVertices(Set<V> vertices);


    /**
     * Adds a collection of edges to the graph. Each edge in the collection must connect
     * vertices that are already present in the graph or will be added during this operation.
     *
     * @param edges A set of edges to be added to the graph. Each edge connects two vertices
     *              and may carry associated data.
     */
    void addEdges(Set<E> edges);

    /**
     * Checks if the specified vertex exists in the graph.
     *
     * @param vertex The vertex to be checked for existence in the graph.
     * @return true if the vertex is present in the graph, false otherwise.
     */
    boolean containsVertex(V vertex);

    /**
     * Checks if there exists an edge in the graph from the specified source vertex
     * to the specified target vertex.
     *
     * @param from The source vertex of the edge to check for.
     * @param to The target vertex of the edge to check for.
     * @return true if an edge exists from the source vertex to the target vertex,
     *         false otherwise.
     */
    boolean containsEdge(V from, V to);

    /**
     * Retrieves a list of neighbors for the specified vertex. Neighbors are defined as vertices
     * that are directly connected to the given vertex by an edge.
     *
     * @param vertex The vertex whose neighbors are to be retrieved.
     * @return A list of vertices that are neighbors of the specified vertex. If the vertex does not exist
     *         in the graph, the behavior is implementation-dependent (e.g., returning an empty list or null).
     */
    Set<V> getNeighbors(V vertex);

    /**
     * Retrieves all the vertices present in the graph.
     *
     * @return A set containing all vertices in the graph.
     */
    Set<V> getVertices();
}
