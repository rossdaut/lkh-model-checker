package lkh.automata;

import lkh.automata.impl.AutomataOperations;
import lkh.automata.impl.GraphDeterministicAutomaton;
import lkh.automata.impl.GraphNonDeterministicAutomaton;
import lkh.dot.DotReader;
import lkh.dot.DotWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AutomataOperationsTest {
  static GraphNonDeterministicAutomaton<String, String> nfa;
  static GraphDeterministicAutomaton<String, String> dfa1, dfa2, dfa3, dfa4, min;
  static String resourcesPath = "src/test/resources";

  @BeforeAll
  static void setUp() {
    try {
      nfa = DotReader.readNFA(resourcesPath + "/nfa.dot");    // (ac | b +)+
      dfa1 = DotReader.readDFA(resourcesPath + "/dfa.dot");   // (ac | b +)+
      dfa2 = DotReader.readDFA(resourcesPath + "/dfa2.dot");  // (a+cb)+
      dfa3 = DotReader.readDFA(resourcesPath + "/dfa3.dot");  // acb
      dfa4 = DotReader.readDFA(resourcesPath + "/dfa4.dot");  // 01+
      min = DotReader.readDFA(resourcesPath + "/min.dot");    // 01+ (minimized)
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testDeterminize() {
    GraphDeterministicAutomaton<Integer, String> expected, actual;
    expected = stringToInt(dfa1);
    actual = AutomataOperations.determinize(nfa);
    assertEquals(expected, actual);
  }

  @Test
  public void asDeterministicTest() {
    GraphDeterministicAutomaton<String, String> actual, expected;
    try {
      GraphNonDeterministicAutomaton<String, String> dfaAsNfa = DotReader.readNFA(resourcesPath + "/dfa.dot");
      actual = AutomataOperations.asDeterministic(dfaAsNfa);
      expected = dfa1;
      assertEquals(expected, actual);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "b", "ac", "acacacacac", "bbbbb", "a", "aaa", "bbaa", "acba"})
  public void complementTest(String string) {
    GraphDeterministicAutomaton<String, String> completeDFA, complement;
    List<String> s = string.isEmpty() ? Collections.emptyList() : Arrays.asList(string.split(""));
    completeDFA = dfa1.clone();
    completeDFA.complete("");

    complement = AutomataOperations.complement(completeDFA);

    assertNotEquals(complement.evaluate(s), dfa1.evaluate(s));
  }

  @ParameterizedTest
  @CsvSource({"acb,true", "acbacb,true", "'',false", "acacac,false", "acbbac,false", "aacb,false", "aaacbacb,false"})
  public void intersectionTest(String string, boolean expected) {
    //dfa1: (ac | b +)+   dfa2: (a+cb)+   intersection: (acb)+
    List<String> s = string.isEmpty() ? Collections.emptyList() : Arrays.asList(string.split(""));
    GraphDeterministicAutomaton<Integer, String> intersection = AutomataOperations.intersection(dfa1, dfa2);
    assertEquals(expected, dfa1.evaluate(s) && dfa2.evaluate(s));
    assertEquals(expected, intersection.evaluate(s));
  }

  @ParameterizedTest
  @CsvSource({"acb,true", "acbacb,false", "'',false", "acacac,false", "acbbac,false", "aacb,false", "aaacbacb,false"})
  public void multipleIntersectionTest(String string, boolean expected) {
    //dfa1: (ac | b +)+   dfa2: (a+cb)+   intersection: (acb)+
    //dfa3: acb  intersection: acb
    List<String> s = string.isEmpty() ? Collections.emptyList() : Arrays.asList(string.split(""));
    Set<GraphDeterministicAutomaton<String, String>> automata = Set.of(dfa1, dfa2, dfa3);

    GraphDeterministicAutomaton<Integer, String> result = AutomataOperations.intersection(automata);

    boolean actual = result.evaluate(s);
    assertEquals(dfa1.evaluate(s) && dfa2.evaluate(s) && dfa3.evaluate(s), actual);
    assertEquals(expected, actual);
  }

  @Test
  public void minimizeTest() {
    GraphDeterministicAutomaton<Integer, String> actual = AutomataOperations.minimize(dfa4);
    GraphDeterministicAutomaton<Integer, String> expected = stringToInt(min);
    DotWriter.writeDFA(actual, "minimized.dot");
    assertEquals(expected, actual);
  }

  // Pre: dfa states are strings of digits
  private GraphDeterministicAutomaton<Integer, String> stringToInt(GraphDeterministicAutomaton<String, String> dfa) {
    GraphDeterministicAutomaton<Integer, String> result = new GraphDeterministicAutomaton<>();

    // Convert Transitions
    Map<String, Integer> map = new HashMap<>();
    for (String state : dfa.getStates()) {
      int stateAsInt = Integer.parseInt(state);
      map.putIfAbsent(state, stateAsInt);

      for (String symbol : dfa.getAlphabet()) {
        dfa.delta(state,symbol).ifPresent(target -> {
          map.putIfAbsent(target, Integer.parseInt(target));
          result.addTransition(stateAsInt, map.get(target), symbol);
        });
      }
    }

    // Convert initial state
    int initialAsInt = Integer.parseInt(dfa.getInitialState());
    result.setInitialState(initialAsInt);

    // Convert final states
    for (String finalState : dfa.getFinalStates()) {
      int finalAsInt = Integer.parseInt(finalState);
      result.addFinalState(finalAsInt);
    }

    return result;
  }
}
