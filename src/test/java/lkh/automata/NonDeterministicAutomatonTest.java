package lkh.automata;

import lkh.dot.DotReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NonDeterministicAutomatonTest {
  private static NonDeterministicAutomaton<String, String> nfa;

  @BeforeAll
  static void setUp() {
    try {
      String resourcesPath = "src/test/resources";
      nfa = DotReader.readNFA(resourcesPath + "/nfa.dot");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testStatesGetter() {
    Set<String> actualStates = nfa.getStates();
    Set<String> expectedStates = new HashSet<>(Arrays.asList(
        "q0", "q1", "q2", "q3", "q4", "q5"
    ));

    assertEquals(expectedStates, actualStates);
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
        Arguments.of("q4", "b", Set.of("q4","q5")),
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
