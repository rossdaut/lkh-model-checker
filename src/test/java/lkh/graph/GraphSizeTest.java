package lkh.graph;

import lkh.graph.edge.DefaultEdge;
import lkh.utils.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GraphSizeTest {

    @Test
    public void testGraphSize() {
        HashMapDirectedGraph<String, DefaultEdge<String>> graph = new HashMapDirectedGraph<>();

        // Agregar vértices
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addVertex("C");

        // Agregar arcos
        graph.addEdge(new DefaultEdge<>("A", "B"));
        graph.addEdge(new DefaultEdge<>("B", "C"));
        graph.addEdge(new DefaultEdge<>("C", "A"));

        // Verificar tamaño
        Pair<Integer, Integer> size = graph.getSize();
        assertEquals(3, size.key(), "Should have 3 vertices");
        assertEquals(3, size.value(), "Should have 3 edges");
    }

    @Test
    public void testEmptyGraphSize() {
        HashMapDirectedGraph<String, DefaultEdge<String>> graph = new HashMapDirectedGraph<>();

        Pair<Integer, Integer> size = graph.getSize();
        assertEquals(0, size.key(), "Empty graph should have 0 vertices");
        assertEquals(0, size.value(), "Empty graph should have 0 edges");
    }
}

