package lkh.automata;

import lkh.dot.DotReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutomataOperationsTest {
  static NonDeterministicAutomaton<String, String> nfa, dfa;

  @BeforeAll
  static void setUp() {
    try {
      String resourcesPath = "src/test/resources";
      nfa = DotReader.readNFA(resourcesPath + "/nfa.dot");
      dfa = DotReader.readNFA(resourcesPath + "/dfa.dot");
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testDeterminize() {
    NonDeterministicAutomaton<Integer, String> expected, actual;
    expected = stringToInt(dfa);
    actual = AutomataOperations.determinize(nfa);
    assertEquals(expected, actual);
  }

  // Pre: dfa states are strings of digits
  private NonDeterministicAutomaton<Integer, String> stringToInt(NonDeterministicAutomaton<String, String> dfa) {
    NonDeterministicAutomaton<Integer, String> result = new NonDeterministicAutomaton<>();

    // Convert Transitions
    Map<String, Integer> map = new HashMap<>();
    for (String state : dfa.getStates()) {
      int stateAsInt = Integer.parseInt(state);
      map.putIfAbsent(state, stateAsInt);

      for (String symbol : dfa.getAlphabet()) {
        for (String target : dfa.delta(state, symbol)) {
          map.putIfAbsent(target, Integer.parseInt(target));
          result.addTransition(stateAsInt, map.get(target), symbol);
        }
      }
      for (String target : dfa.emptyDelta(state))
        result.addEmptyTransition(stateAsInt, map.get(target));
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
