package lkh.lts;

import lkh.utils.Pair;
import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class LTSSizeTest {

    @Test
    public void testLTSSize() {
        HashMapLTS<String, String> lts = new HashMapLTS<>();

        // Agregar estados
        lts.addState("s0", Set.of("p", "q"));
        lts.addState("s1", Set.of("p"));
        lts.addState("s2", Set.of("q"));

        // Agregar transiciones
        lts.addTransition("s0", "s1", "a");
        lts.addTransition("s1", "s2", "b");
        lts.addTransition("s2", "s0", "c");

        // Verificar tamaño
        Pair<Integer, Integer> size = lts.getSize();
        assertEquals(3, size.key(), "Should have 3 states");
        assertEquals(3, size.value(), "Should have 3 transitions");
    }

    @Test
    public void testEmptyLTSSize() {
        HashMapLTS<String, String> lts = new HashMapLTS<>();

        Pair<Integer, Integer> size = lts.getSize();
        assertEquals(0, size.key(), "Empty LTS should have 0 states");
        assertEquals(0, size.value(), "Empty LTS should have 0 transitions");
    }
}

