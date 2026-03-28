package lkh.dot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DotReader#readDFA} and {@link DotReader#readNFA}.
 *
 * Each test case uses a hand-written .dot file under src/test/resources/dot/:
 *   - dfa_simple/automaton.dot  : a simple complete DFA over {a,b}
 *   - nfa_simple/automaton.dot  : a simple NFA over {a,b}
 *   - nfa_lambda/automaton.dot  : an NFA over {a,b} with a lambda transition
 */
public class DotReaderTest {

  private static final String RESOURCES_PATH = "src/test/resources/dot";

  // ALPHABET: {a, b}
  // LANGUAGE: (a+b)*a+
  //
  // State 0: initial, non-final
  // State 1: final
  //
  // dfa_simple/automaton.dot : complete DFA
  @Test
  public void dfaSimpleStructure() throws FileNotFoundException {
    var dfa = DotReader.readDFA(RESOURCES_PATH + "/dfa_simple/automaton.dot");

    // Verifies that the DFA has exactly 2 states, the correct initial state, and 1 final state
    assertEquals(2, dfa.getStates().size());
    assertEquals("0", dfa.getInitialState());
    assertEquals(1, dfa.getFinalStates().size());
    assertTrue(dfa.getFinalStates().contains("1"));

    // Verifies that the DFA has the correct alphabet
    assertEquals(2, dfa.getAlphabet().size());
    assertTrue(dfa.getAlphabet().containsAll(List.of("a", "b")));
  }

  // Verifies that the DFA accepts the specified language
  @ParameterizedTest
  @CsvSource({"a,true", "aa,true", "ba,true", "bba,true", "aba,true",
              "'',false", "b,false", "ab,false", "aab,false"})
  public void dfaSimpleLanguage(String s, boolean expected) throws FileNotFoundException {
    var dfa = DotReader.readDFA(RESOURCES_PATH + "/dfa_simple/automaton.dot");
    assertEquals(expected, dfa.evaluate(toWord(s)));
  }

  // ALPHABET: {a, b}
  // LANGUAGE: ((a|b)b)* (a|b)a
  //
  // State 0: initial
  // State 1: intermediate
  // State 2: final
  //
  // nfa_simple/automaton.dot : NFA
  @Test
  public void nfaSimpleStructure() throws FileNotFoundException {
    var nfa = DotReader.readNFA(RESOURCES_PATH + "/nfa_simple/automaton.dot");

    // Verifies that the NFA has exactly 3 states, the correct initial state, and 1 final state
    assertEquals(3, nfa.getStates().size());
    assertEquals("0", nfa.getInitialState());
    assertEquals(1, nfa.getFinalStates().size());
    assertTrue(nfa.getFinalStates().contains("2"));

    // Verifies that the NFA has the correct alphabet
    assertEquals(2, nfa.getAlphabet().size());
    assertTrue(nfa.getAlphabet().containsAll(List.of("a", "b")));
  }


  // Verifies that the NFA accepts exactly strings of length 2 ending in 'a'
  @ParameterizedTest
  @CsvSource({"aa,true", "ba,true", "bbbbba,true", "abbbaa,true",
              "'',false", "a,false", "b,false", "ab,false", "bb,false", "aaa,false"})
  public void nfaSimpleLanguage(String s, boolean expected) throws FileNotFoundException {
    var nfa = DotReader.readNFA(RESOURCES_PATH + "/nfa_simple/automaton.dot");
    assertEquals(expected, nfa.evaluate(toWord(s)));
  }

  // ALPHABET: {a, b}
  // LANGUAGE: (a|b)+
  //
  // State 0: initial
  // State 1: final
  // Lambda transition: 1 -> 0
  //
  // nfa_lambda/automaton.dot : NFA with one lambda transition


  @Test
  public void nfaLambdaStructure() throws FileNotFoundException {
    var nfa = DotReader.readNFA(RESOURCES_PATH + "/nfa_lambda/automaton.dot");

    // Verifies that the NFA has exactly 2 states, the correct initial state, and 1 final state
    assertEquals(2, nfa.getStates().size());
    assertEquals("0", nfa.getInitialState());
    assertEquals(1, nfa.getFinalStates().size());
    assertTrue(nfa.getFinalStates().contains("1"));

    // Verifies that the NFA has the correct lambda transition
    assertFalse(nfa.emptyDelta("1").isEmpty());
    assertTrue(nfa.emptyDelta("1").contains("0"));

    // Verifies that the NFA has the correct alphabet
    assertEquals(2, nfa.getAlphabet().size());
    assertTrue(nfa.getAlphabet().containsAll(List.of("a", "b")));
  }

  // Verifies that the NFA accepts exactly single-symbol strings (with lambda closure, also longer via looping)
  @ParameterizedTest
  @CsvSource({"a,true", "b,true", "aa,true", "ab,true", "ba,true", "bb,true",
              "'',false"})
  public void nfaLambdaLanguage(String s, boolean expected) throws FileNotFoundException {
    var nfa = DotReader.readNFA(RESOURCES_PATH + "/nfa_lambda/automaton.dot");
    assertEquals(expected, nfa.evaluate(toWord(s)));
  }

  // -- helpers --

  private List<String> toWord(String s) {
    return s.isEmpty() ? Collections.emptyList() : Arrays.asList(s.split(""));
  }
}
