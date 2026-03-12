package lkh.automata;

import lkh.automata.impl.GraphNonDeterministicAutomaton;
import lkh.dot.DotReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class GraphNonDeterministicAutomatonTest {
  private static GraphNonDeterministicAutomaton<String, String> nfa;
  private static String resourcesPath;

  @BeforeEach
  void setUp() {
    try {
      resourcesPath = "src/test/resources";
      nfa = DotReader.readNFA(resourcesPath + "/automata/non_deterministic/nfa.dot");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testGetInitialState() {
    assertEquals("q0", nfa.getInitialState());
  }

  @Test
  public void testGetFinalStates() {
    assertEquals(Set.of("q4", "q5"), nfa.getFinalStates());
  }

  @Test
  public void testAddFinalState() {
    nfa.addFinalState("q1");
    assertTrue(nfa.isFinal("q1"));
  }

  @Test
  public void testAddFinalStates() {
    nfa.addFinalStates(Set.of("q1", "q2"));
    assertTrue(nfa.isFinal("q1"));
    assertTrue(nfa.isFinal("q2"));
  }

  @ParameterizedTest
  @CsvSource({"q1, true", "q4, true", "notAState, false"})
  public void testContainsState(String state, boolean expected) {
    assertEquals(expected, nfa.containsState(state));
  }


  @ParameterizedTest
  @CsvSource({"q4, true", "q0, false", "notAState, false"})
  public void testIsFinal(String state, boolean expected) {
    assertEquals(expected, nfa.isFinal(state));
  }

  @Test
  public void testGetNonFinalStates() {
    assertEquals(Set.of("q0", "q1", "q2", "q3"), nfa.getNonFinalStates());
  }

  @Test
  public void testOutgoingTransitions() {
    // q0 -> q1 [a],  q0 -> q3 [lambda]
    var transitions = nfa.outgoingTransitions("q0");
    assertEquals(2, transitions.size());
  }

  @Test
  public void testAddEmptyTransition() {
    nfa.addEmptyTransition("q1", "q3");
    assertTrue(nfa.emptyDelta("q1").contains("q3"));
  }


  @Test
  public void testStatesGetter() {
    Set<String> actualStates = nfa.getStates();
    Set<String> expectedStates = new HashSet<>(Arrays.asList(
        "q0", "q1", "q2", "q3", "q4", "q5"
    ));

    assertEquals(expectedStates, actualStates);
  }

  @Test
  public void testAddTransition() {
    nfa.addTransition("q0", "a", "q1");
    assertEquals(Set.of("q1"), nfa.delta("q0", "a"));
  }

  @Test
  public void testAddNullTransitionThrows() {
    assertThrows(NullPointerException.class, () -> nfa.addTransition("q0", "a", null));
  }

  @Test
  public void testDeltaNonExistingStateThrows() {
    assertThrows(IllegalArgumentException.class, () -> nfa.delta("notAState", "a"));
  }

  @Test
  public void testAddNullStateThrows() {
    assertThrows(NullPointerException.class, () -> nfa.addState(null));
  }

  @ParameterizedTest
  @MethodSource("provideArgsForDelta")
  public void testDelta(String state, String action, Set<String> expectedStates) {
    Set<String> actualStates = nfa.delta(state, action);
    assertEquals(expectedStates, actualStates);
  }

  @ParameterizedTest
  @MethodSource("provideArgsForEmptyDelta")
  public void testEmptyDelta(String state, Set<String> expectedStates) {
    Set<String> actualStates = nfa.emptyDelta(state);
    assertEquals(expectedStates, actualStates);
  }

  @ParameterizedTest
  @MethodSource("provideArgsForLambdaClosure")
  public void testLambdaClosure(String state, Set<String> expectedStates) {
    Set<String> actualStates = nfa.lambdaClosure(state);
    assertEquals(expectedStates, actualStates);
  }

  @ParameterizedTest
  @MethodSource("provideArgsForSetLambdaClosure")
  public void testLambdaClosureFromSet(Set<String> states, Set<String> expectedStates) {
    Set<String> actualStates = nfa.lambdaClosure(states);
    assertEquals(expectedStates, actualStates);
  }

  @ParameterizedTest
  @MethodSource("provideArgsForMove")
  public void testMove(Set<String> states, String symbol, Set<String> expectedStates) {
    Set<String> actualStates = nfa.move(states, symbol);
    assertEquals(expectedStates, actualStates);
  }

  @ParameterizedTest
  @MethodSource("provideArgsForEvaluate")
  public void testEvaluate(List<String> string, boolean isAccepted) {
    assertEquals(isAccepted, nfa.evaluate(string));
  }

  @Test
  public void testClone() {
    GraphNonDeterministicAutomaton<String, String> clone = nfa.clone();
    assertEquals(nfa, clone);
  }

  @Test
  public void testEqualsSymmetric() {
    GraphNonDeterministicAutomaton<String, String> other = readNFA("automata/non_deterministic/nfa.dot");
    assertEquals(nfa, other);
    assertEquals(other, nfa);
  }

  @Test
  public void testNotEqualAfterMutation() {
    GraphNonDeterministicAutomaton<String, String> other = readNFA("automata/non_deterministic/nfa.dot");
    other.addState("extra");
    assertNotEquals(nfa, other);
  }


  @Test
  public void testHashCodeConsistency() {
    GraphNonDeterministicAutomaton<String, String> other = readNFA("automata/non_deterministic/nfa.dot");
    assertEquals(nfa.hashCode(), other.hashCode());
  }

  @Test
  public void testNotEqualsNull() {
    assertNotEquals(null, nfa);
  }

  @Test
  public void testNotEqualsOtherType() {
    assertNotEquals("not an automaton", nfa);
  }

  @Test
  public void testNotEqualsDifferentAlphabet() {
    GraphNonDeterministicAutomaton<String, String> other = readNFA("automata/non_deterministic/nfa.dot");
    other.addTransition("q0", "q1", "z");
    assertNotEquals(nfa, other);
  }

  @Test
  public void testNotEqualsDifferentFinalStates() {
    GraphNonDeterministicAutomaton<String, String> other = readNFA("automata/non_deterministic/nfa.dot");
    other.addFinalState("q0");
    assertNotEquals(nfa, other);
  }

  @Test
  public void testNotEqualsDifferentInitialState() {
    GraphNonDeterministicAutomaton<String, String> a = new GraphNonDeterministicAutomaton<>();
    GraphNonDeterministicAutomaton<String, String> b = new GraphNonDeterministicAutomaton<>();
    b.setInitialState("q0");
    assertNotEquals(a, b);
  }

  @Test
  public void testEqualsBothNullInitialState() {
    GraphNonDeterministicAutomaton<String, String> a = new GraphNonDeterministicAutomaton<>();
    GraphNonDeterministicAutomaton<String, String> b = new GraphNonDeterministicAutomaton<>();
    assertEquals(a, b);
  }

  // --- helpers ---

  private GraphNonDeterministicAutomaton<String, String> readNFA(String path) {
    try {
      return DotReader.readNFA(resourcesPath + "/" + path);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static Stream<Arguments> provideArgsForLambdaClosure() {
    return Stream.of(
        Arguments.of("q0", Set.of("q0", "q3")),
        Arguments.of("q1", Set.of("q1")),
        Arguments.of("q2", Set.of("q2", "q5", "q0", "q3"))
    );
  }

  private static Stream<Arguments> provideArgsForDelta() {
    return Stream.of(
        Arguments.of("q0", "a", Set.of("q1")),
        Arguments.of("q4", "b", Set.of("q4", "q5")),
        Arguments.of("q1", "c", Set.of("q2"))
    );
  }

  private static Stream<Arguments> provideArgsForEmptyDelta() {
    return Stream.of(
        Arguments.of("q0", Set.of("q3")),
        Arguments.of("q2", Set.of("q5")),
        Arguments.of("q5", Set.of("q0"))
    );
  }

  private static Stream<Arguments> provideArgsForSetLambdaClosure() {
    return Stream.of(
        Arguments.of(Set.of(), Set.of()),
        Arguments.of(Set.of("q1"), Set.of("q1")),
        Arguments.of(Set.of("q1", "q3"), Set.of("q1", "q3")),
        Arguments.of(Set.of("q2"), Set.of("q2", "q5", "q0", "q3")),
        Arguments.of(Set.of("q5", "q4"), Set.of("q5", "q4", "q0", "q3"))
    );
  }

  private static Stream<Arguments> provideArgsForMove() {
    return Stream.of(
        Arguments.of(Set.of(), "a", Set.of()),
        Arguments.of(Set.of("q0"), "b", Set.of()),
        Arguments.of(Set.of("q0"), "a", Set.of("q1")),
        Arguments.of(Set.of("q3"), "b", Set.of("q4", "q5")),
        Arguments.of(Set.of("q1", "q3"), "c", Set.of("q2")),
        Arguments.of(Set.of("q4"), "b", Set.of("q4", "q5")),
        Arguments.of(Set.of("q3", "q4"), "b", Set.of("q4", "q5"))
    );
  }

  private static Stream<Arguments> provideArgsForEvaluate() {
    return Stream.of(
        Arguments.of(List.of(), false),
        Arguments.of(List.of("a"), false),
        Arguments.of(List.of("c"), false),
        Arguments.of(List.of("a", "c"), true),
        Arguments.of(List.of("b"), true),
        Arguments.of(List.of("b", "b", "b"), true),
        Arguments.of(List.of("a", "c", "b", "b", "a", "c"), true),
        Arguments.of(List.of("a", "b", "a", "c"), false),
        Arguments.of(List.of("a", "c", "b", "c"), false),
        Arguments.of(List.of("b", "b", "a", "c", "a", "c", "b"), true)
    );
  }
}
