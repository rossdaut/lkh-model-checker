package lkh.automata;

import lkh.automata.impl.GraphDeterministicAutomaton;
import lkh.dot.DotReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GraphDeterministicAutomatonTest {
  private static GraphDeterministicAutomaton<String, String> dfa;
  private static String resourcesPath;

  // Uses astar DFA: language a*, alphabet {a, b}, states {"0", "1"}, final {"0"}
  @BeforeEach
  void setUp() {
    resourcesPath = "src/test/resources/automata/deterministic/";
    dfa = readDFA("dfa.dot");
  }

  @Test
  public void testEmpty() {
    GraphDeterministicAutomaton<Integer, String> empty = GraphDeterministicAutomaton.empty();
    assertEquals(0, empty.getInitialState());
    assertTrue(empty.getStates().contains(0));
    assertTrue(empty.getFinalStates().isEmpty());
  }

  @Test
  public void testAddTransitionReplacesExisting() {
    // "0" -> "0" [a] already exists; replace with "0" -> "1" [a]
    dfa.addTransition("0", "1", "a");
    assertEquals(Optional.of("1"), dfa.delta("0", "a"));
  }

  @Test
  public void testAddTransitionNullSymbolThrows() {
    assertThrows(NullPointerException.class, () -> dfa.addTransition("0", "1", null));
  }

  @ParameterizedTest
  @MethodSource("provideArgsForDelta")
  public void testDelta(String state, String symbol, Optional<String> expected) {
    assertEquals(expected, dfa.delta(state, symbol));
  }

  @ParameterizedTest
  @MethodSource("provideArgsForEvaluate")
  public void testEvaluate(List<String> string, boolean expected) {
    assertEquals(expected, dfa.evaluate(string));
  }

  @Test
  public void testClone() {
    GraphDeterministicAutomaton<String, String> clone = dfa.clone();
    assertEquals(dfa, clone);
  }

  @Test
  public void testIsEmptyFalse() {
    assertFalse(dfa.isEmpty());
  }

  @Test
  public void testIsEmptyTrue() {
    GraphDeterministicAutomaton<Integer, String> noFinals = GraphDeterministicAutomaton.empty();
    noFinals.addTransition(0, 0, "a");
    assertTrue(noFinals.isEmpty());
  }

  @Test
  public void testEqualsSymmetric() {
    GraphDeterministicAutomaton<String, String> other = readDFA("dfa.dot");
    assertEquals(dfa, other);
    assertEquals(other, dfa);
  }

  @Test
  public void testNotEqualsNull() {
    assertNotEquals(null, dfa);
  }

  @Test
  public void testNotEqualsOtherType() {
    assertNotEquals("not an automaton", dfa);
  }

  @Test
  public void testNotEqualsDifferentFinalStates() {
    GraphDeterministicAutomaton<String, String> other = readDFA("dfa.dot");
    other.addFinalState("1");
    assertNotEquals(dfa, other);
  }

  @Test
  public void testNotEqualsDifferentInitialState() {
    GraphDeterministicAutomaton<String, String> a = new GraphDeterministicAutomaton<>();
    GraphDeterministicAutomaton<String, String> b = new GraphDeterministicAutomaton<>();
    b.setInitialState("0");
    assertNotEquals(a, b);
  }

  @Test
  public void testEqualsBothNullInitialState() {
    GraphDeterministicAutomaton<String, String> a = new GraphDeterministicAutomaton<>();
    GraphDeterministicAutomaton<String, String> b = new GraphDeterministicAutomaton<>();
    assertEquals(a, b);
  }

  @Test
  public void testHashCodeConsistency() {
    GraphDeterministicAutomaton<String, String> other = readDFA("dfa.dot");
    assertEquals(dfa.hashCode(), other.hashCode());
  }

  // --- helpers ---

  private GraphDeterministicAutomaton<String, String> readDFA(String path) {
    try {
      return DotReader.readDFA(resourcesPath + "/" + path);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static Stream<Arguments> provideArgsForDelta() {
    return Stream.of(
        Arguments.of("0", "a", Optional.of("0")),
        Arguments.of("0", "b", Optional.of("1")),
        Arguments.of("1", "a", Optional.of("1")),
        Arguments.of("99", "a", Optional.empty())
    );
  }

  private static Stream<Arguments> provideArgsForEvaluate() {
    return Stream.of(
        Arguments.of(List.of(),              true),
        Arguments.of(List.of("a"),       true),
        Arguments.of(List.of("a", "a", "a"), true),
        Arguments.of(List.of("b"),       false),
        Arguments.of(List.of("a", "b"),      false)
    );
  }
}
