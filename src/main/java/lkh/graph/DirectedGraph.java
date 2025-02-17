package lkh.graph;
import lkh.graph.edge.Edge;

import java.util.List;

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
     * Retrieves a list of neighbors that represent the vertices directly reachable
     * from the specified vertex (outgoing neighbors).
     *
     * @param vertex The vertex to find outgoing neighbors.
     * @return A list of vertices that are outgoing neighbors of the specified vertex.
     */
    List<V> getOutgoingNeighbors(V vertex);

    /**
     * Retrieves a list of neighbors that represent the vertices that can directly
     * reach the specified vertex (incoming neighbors).
     *
     * @param vertex The vertex to find incoming neighbors.
     * @return A list of vertices that are incoming neighbors of the specified vertex.
     */
    List<V> getIncomingNeighbors(V vertex);
}
