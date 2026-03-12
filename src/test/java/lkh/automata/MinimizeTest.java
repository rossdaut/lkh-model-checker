package lkh.automata;

import lkh.automata.impl.AutomataOperations;
import lkh.dot.DotReader;
import lkh.dot.DotWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link AutomataOperations#minimize}.
 *
 * Each test case has its own subfolder under src/test/resources/automata/minimize/:
 *   - input.dot   : the DFA to minimize
 *   - expected.dot: the expected minimal DFA
 */
public class MinimizeTest {

  private static final String RESOURCES_PATH = "src/test/resources/automata/operations/minimize";

  // ALPHABET: {a,b}
  // LANGUAGE: a*
  // The DFA is already minimal -- minimize() must return an equivalent 2-state DFA.
  //
  // input.dot   : DFA for a* -- 2 states, already minimal
  // expected.dot: minimize(input) -- still 2 states

  // Verifies that the minimized DFA still accepts exactly a*
  @ParameterizedTest
  @CsvSource({"'',true", "a,true", "aa,true", "aaa,true", "b,false", "ab,false", "ba,false"})
  public void alreadyMinimalLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/already_minimal/input.dot");
    var result = AutomataOperations.minimize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  // and that the state count does not change for an already-minimal DFA
  @Test
  public void alreadyMinimalEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/already_minimal/input.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(RESOURCES_PATH + "/already_minimal/expected.dot"));
    var result   = AutomataOperations.minimize(input);
    assertEquals(expected, result);
    assertEquals(input.getStates().size(), result.getStates().size());
  }

  // ALPHABET: {0,1}
  // LANGUAGE: 01+
  // The input has 5 states; states 2 and 3 (both final, same outgoing behaviour)
  // are equivalent and get fused -- result has 4 states.
  //
  // input.dot   : DFA for 01+ -- 5 states, two equivalent final states
  // expected.dot: minimize(input) -- 4 states

  // Verifies that the minimized DFA still accepts exactly 01+
  @ParameterizedTest
  @CsvSource({"01,true", "011,true", "0111,true", "'',false", "0,false", "1,false", "10,false", "00,false"})
  public void twoEquivalentFinalsLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/two_equivalent_finals/input.dot");
    var result = AutomataOperations.minimize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  // and that the two equivalent final states are indeed fused (5 -> 4 states)
  @Test
  public void twoEquivalentFinalsEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/two_equivalent_finals/input.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(RESOURCES_PATH + "/two_equivalent_finals/expected.dot"));
    var result   = AutomataOperations.minimize(input);
    assertEquals(expected, result);


    assertEquals(input.getStates().size() - 1, result.getStates().size());
  }

  // ALPHABET: {a,b}
  // LANGUAGE: (ab)*
  // The input has 5 states; two of them are equivalent dead (trap) states
  // that get fused, leaving 3 states.
  //
  // input.dot   : DFA for (ab)* -- 5 states, two equivalent dead states
  // expected.dot: minimize(input) -- 3 states

  // Verifies that the minimized DFA still accepts exactly (ab)*
  @ParameterizedTest
  @CsvSource({"'',true", "ab,true", "abab,true", "ababab,true",
              "a,false", "b,false", "aba,false", "aab,false", "bab,false"})
  public void deadStatesLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/dead_states/input.dot");
    var result = AutomataOperations.minimize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  // and that the two equivalent dead states are fused (5 -> 3 states)
  @Test
  public void deadStatesEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/dead_states/input.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(RESOURCES_PATH + "/dead_states/expected.dot"));
    var result   = AutomataOperations.minimize(input);
    assertEquals(expected, result);
    assertEquals(3, result.getStates().size());
  }

  // ALPHABET: {a,b}
  // LANGUAGE: a(a|b)*
  // The input has 6 states; four equivalent final states all get fused
  // into one, leaving 3 states.
  //
  // input.dot   : DFA for a(a|b)* -- 6 states, four equivalent final states
  // expected.dot: minimize(input) -- 3 states

  // Verifies that the minimized DFA still accepts exactly a(a|b)*
  @ParameterizedTest
  @CsvSource({"a,true", "aa,true", "ab,true", "aab,true", "aba,true", "abba,true",
              "'',false", "b,false", "ba,false", "baa,false"})
  public void manyRedundantLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/many_redundant/input.dot");
    var result = AutomataOperations.minimize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  // and that four equivalent finals collapse to one (6 -> 3 states)
  @Test
  public void manyRedundantEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/many_redundant/input.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(RESOURCES_PATH + "/many_redundant/expected.dot"));
    var result   = AutomataOperations.minimize(input);
    assertEquals(expected, result);
    assertEquals(3, result.getStates().size());
  }

  // ALPHABET: {a,b}
  // LANGUAGE: {a,b}*  (all strings -- single all-accepting state)
  // The DFA is already minimal with 1 state -- minimize() must leave it unchanged.
  //
  // input.dot   : DFA for {a,b}* -- 1 state
  // expected.dot: minimize(input) -- still 1 state

  // Verifies that the minimized DFA still accepts every string
  @ParameterizedTest
  @CsvSource({"'',true", "a,true", "b,true", "ab,true", "ba,true", "aabb,true"})
  public void singleStateLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/single_state/input.dot");
    var result = AutomataOperations.minimize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  // and that a single-state DFA stays at 1 state after minimization
  @Test
  public void singleStateEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/single_state/input.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(RESOURCES_PATH + "/single_state/expected.dot"));
    var result   = AutomataOperations.minimize(input);
    assertEquals(expected, result);
    assertEquals(1, result.getStates().size());
  }

  // ALPHABET: {0,1}
  // LANGUAGE: {0,1} ∪ {0,1}³{0,1}*   ( words of length 1 or length ≥ 3 )
  //
  // input.dot   : DFA for {a,b}*  -> two paths with 6 states
  // expected.dot: minimize(input) -> one path with 4 states

  // Verifies that the minimized DFA still accepts every string
  @ParameterizedTest
  @CsvSource({"0,true", "1,true", "001,true", "111,true", "1001,true", "01010,true",
              "'',false", "10,false", "01,false", "11,false", "00,false"})
  public void twoEquivalentPathsLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/two_equivalent_paths/input.dot");
    var result = AutomataOperations.minimize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  // and that a single-state DFA contains 4 states after minimization
  @Test
  public void twoEquivalentPathsEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/two_equivalent_paths/input.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(RESOURCES_PATH + "/two_equivalent_paths/expected.dot"));
    var result   = AutomataOperations.minimize(input);
    assertEquals(expected, result);
    assertEquals(4, result.getStates().size());
  }

  // ALPHABET: {0,1}
  // LANGUAGE: ((01|10)1*0)*(01|10)1*
  //
  // input.dot: DFA for ((01|10)1*0)*(01|10)1* -- 8 states
  // expected.dot: minimize(input) -- 5 states

  // Verifies that the minimized DFA still accepts every string
  @ParameterizedTest
  @CsvSource({"01,true", "10,true", "011,true", "101,true", "01001,true", "10010,true", "010101,true",
              "'',false", "0,false", "1,false", "00,false", "11,false", "010,false", "100,false", "01011,false"})
  public void eightStatesLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/eight_states/input.dot");
    var result = AutomataOperations.minimize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  // and that a single-state DFA contains 5 states after minimization
  @Test
  public void eightStatesEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/eight_states/input.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(RESOURCES_PATH + "/eight_states/expected.dot"));
    var result   = AutomataOperations.minimize(input);
    assertEquals(expected, result);
    assertEquals(5, result.getStates().size());
  }

  // ALPHABET: {0,1}
  // LANGUAGE: 01+
  //
  // input.dot: DFA for 01+ -> Complete with 5 states
  // expected.dot: minimize(input) -- Complete with 4 states

  // Verifies that the minimized DFA still accepts every string
  @ParameterizedTest
  @CsvSource({"01,true", "011,true", "0111,true",
             "'',false", "0,false", "1,false", "10,false", "00,false"})
  public void zeroOnePlusLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/zero_oneplus/input.dot");
    var result = AutomataOperations.minimize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  // and that a single-state DFA contains 4 states after minimization
  @Test
  public void zeroOnePlusEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/zero_oneplus/input.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(RESOURCES_PATH + "/zero_oneplus/expected.dot"));
    var result   = AutomataOperations.minimize(input);
    assertEquals(expected, result);
    assertEquals(4, result.getStates().size());
  }

  private List<String> toWord(String s) {
    return s.isEmpty() ? Collections.emptyList() : Arrays.asList(s.split(""));
  }
}

