package lkh.automata;

import lkh.dot.DotReader;
import lkh.utils.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AutomataOperationsTest {
  static NonDeterministicAutomaton<String, String> nfa;
  static DeterministicAutomaton<String, String> dfa1, dfa2, dfa3;
  static String resourcesPath = "src/test/resources";

  @BeforeAll
  static void setUp() {
    try {
      nfa = DotReader.readNFA(resourcesPath + "/nfa.dot");    // (ac | b +)+
      dfa1 = DotReader.readDFA(resourcesPath + "/dfa.dot");   // (ac | b +)+
      dfa2 = DotReader.readDFA(resourcesPath + "/dfa2.dot");  // (a+cb)+
      dfa3 = DotReader.readDFA(resourcesPath + "/dfa3.dot");  // acb
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testDeterminize() {
    DeterministicAutomaton<Integer, String> expected, actual;
    expected = stringToInt(dfa1);
    actual = AutomataOperations.determinize(nfa);
    assertEquals(expected, actual);
  }

  @Test
  public void asDeterministicTest() {
    DeterministicAutomaton<String, String> actual, expected;
    try {
      NonDeterministicAutomaton<String, String> dfaAsNfa = DotReader.readNFA(resourcesPath + "/dfa.dot");
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
    DeterministicAutomaton<String, String> completeDFA, complement;
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
    DeterministicAutomaton<Integer, String> intersection = AutomataOperations.intersection(dfa1, dfa2);
    assertEquals(expected, dfa1.evaluate(s) && dfa2.evaluate(s));
    assertEquals(expected, intersection.evaluate(s));
  }

  @ParameterizedTest
  @CsvSource({"acb,true", "acbacb,false", "'',false", "acacac,false", "acbbac,false", "aacb,false", "aaacbacb,false"})
  public void multipleIntersectionTest(String string, boolean expected) {
    //dfa1: (ac | b +)+   dfa2: (a+cb)+   intersection: (acb)+
    //dfa3: acb  intersection: acb
    List<String> s = string.isEmpty() ? Collections.emptyList() : Arrays.asList(string.split(""));
    Set<DeterministicAutomaton<String, String>> automata = Set.of(dfa1, dfa2, dfa3);

    DeterministicAutomaton<Integer, String> result = AutomataOperations.intersection(automata);

    boolean actual = result.evaluate(s);
    assertEquals(dfa1.evaluate(s) && dfa2.evaluate(s) && dfa3.evaluate(s), actual);
    assertEquals(expected, actual);
  }

  // Pre: dfa states are strings of digits
  private DeterministicAutomaton<Integer, String> stringToInt(DeterministicAutomaton<String, String> dfa) {
    DeterministicAutomaton<Integer, String> result = new DeterministicAutomaton<>();

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
