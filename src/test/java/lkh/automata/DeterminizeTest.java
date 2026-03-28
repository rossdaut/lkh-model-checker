package lkh.automata;

import lkh.automata.impl.AutomataOperations;
import lkh.dot.DotReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link AutomataOperations#determinize} and {@link AutomataOperations#asDeterministic}.
 *
 * Each test case has its own subfolder under src/test/resources/automata/determinize/:
 *   - input.dot   : the NFA input
 *   - expected.dot: the expected result DFA (for determinize cases)
 */
public class DeterminizeTest {

  private static final String RESOURCES_PATH = "src/test/resources/automata/determinize";

  // ALPHABET: {a,b}
  // LANGUAGE: {a,b}*abb  (string that end with "abb" )
  //
  // input.dot    : NFA for {a,b}*abb  -> with two transitions on 'a' from q0.
  // expected.dot : DFA for determinize({a,b}*abb)

  // Verifies the accepted language after determinizing
  @ParameterizedTest
  @CsvSource({"abb,true", "ababb,true", "aabb,true", "ababababb,true",
              "'',false", "a,false", "abbabbb,false", "bbab,false"})
  public void abbDeterminizeLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readNFA(RESOURCES_PATH + "/astar_b/input.dot");
    var result = AutomataOperations.determinize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void abbDeterminizeEquals() throws FileNotFoundException {
    var input    = DotReader.readNFA(RESOURCES_PATH + "/astar_b/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/astar_b/expected.dot");
    assertEquals(AutomataOperations.toIntegerStates(expected), AutomataOperations.determinize(input));
  }

  // ALPHABET: {a,b}
  // LANGUAGE: (ab)+  (one or more repetitions of "ab")
  // NFA has a λ-transition from q2 back to q0.
  //
  // input.dot    : NFA for (ab)+
  // expected.dot : DFA for determinize((ab)+)

  // Verifies the accepted language after determinizing
  @ParameterizedTest
  @CsvSource({"ab,true", "abab,true", "ababab,true",
              "'',false", "a,false", "b,false", "aba,false", "ba,false"})
  public void abPlusDeterminizeLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readNFA(RESOURCES_PATH + "/ab_plus/input.dot");
    var result = AutomataOperations.determinize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void abPlusDeterminizeEquals() throws FileNotFoundException {
    var input    = DotReader.readNFA(RESOURCES_PATH + "/ab_plus/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/ab_plus/expected.dot");
    assertEquals(AutomataOperations.toIntegerStates(expected), AutomataOperations.determinize(input));
  }

  // ALPHABET: {a,b,c}
  // LANGUAGE: (ac|b+)+  (one or more repetitions of "ac" or one or more "b"s)
  // NFA has several λ-transitions.
  //
  // input.dot    : NFA for (ac|b+)+
  // expected.dot : DFA for determinize((ac|b+)+)

  // Verifies the accepted language after determinizing
  @ParameterizedTest
  @CsvSource({"b,true", "ac,true", "bbb,true", "acb,true", "bacac,true",
              "'',false", "a,false", "c,false", "abc,false"})
  public void acbPlusDeterminizeLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readNFA(RESOURCES_PATH + "/acb_plus/input.dot");
    var result = AutomataOperations.determinize(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void acbPlusDeterminizeEquals() throws FileNotFoundException {
    var input    = DotReader.readNFA(RESOURCES_PATH + "/acb_plus/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/acb_plus/expected.dot");
    assertEquals(AutomataOperations.toIntegerStates(expected), AutomataOperations.determinize(input));
  }

  // ALPHABET: {a,b,c}
  // LANGUAGE: a+{b,c}*  (NFA with deterministic structure — no λ-transitions, no multi-transitions)
  // asDeterministic should succeed and preserve the language.
  //
  // input.dot : NFA for a*  (valid input for asDeterministic)

  // Verifies the accepted language after asDeterministic
  @ParameterizedTest
  @CsvSource({"'a',true", "ab,true", "abcc,true", "'',false", "abca,false", "b,false", "c,false"})
  public void alreadyDFAAsDeterministicLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readNFA(RESOURCES_PATH + "/already_dfa/input.dot");
    var result = AutomataOperations.asDeterministic(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that asDeterministic produces a DFA structurally equal to readDFA on the same file
  @Test
  public void alreadyDFAAsDeterministicEqualsReadDFA() throws FileNotFoundException {
    var input    = DotReader.readNFA(RESOURCES_PATH + "/already_dfa/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/already_dfa/input.dot");
    assertEquals(expected, AutomataOperations.asDeterministic(input));
  }

  // ALPHABET: {a,b,c}
  // LANGUAGE: (ac|b+)+ (NFA with deterministic structure — no λ-transitions, no multi-transitions)
  //
  // input2.dot : NFA for (ac|b+)+  (valid input for asDeterministic)

  // Verifies the accepted language after asDeterministic
  @ParameterizedTest
  @CsvSource({"'ac',true", "b,true", "acbac,true", "'',false", "abc,false", "a,false", "c,false"})
  public void alreadyDFAAsDeterministicLanguage2(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readNFA(RESOURCES_PATH + "/already_dfa/input2.dot");
    var result = AutomataOperations.asDeterministic(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that asDeterministic produces a DFA structurally equal to readDFA on the same file
  @Test
  public void alreadyDFAAsDeterministicEqualsReadDFA2() throws FileNotFoundException {
    var input    = DotReader.readNFA(RESOURCES_PATH + "/already_dfa/input2.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/already_dfa/input2.dot");
    assertEquals(expected, AutomataOperations.asDeterministic(input));
  }

  // ALPHABET: {a}
  // NFA with two transitions on 'a' from the same state — not directly determinizable.
  //
  // input.dot : NFA with q0 --a--> q1  and  q0 --a--> q2

  // Verifies that asDeterministic throws when the NFA has multiple transitions on the same symbol
  @Test
  public void nondeterministicAsDeterministicThrows() throws FileNotFoundException {
    var input = DotReader.readNFA(RESOURCES_PATH + "/nondeterministic/input.dot");
    assertThrows(IllegalStateException.class, () -> AutomataOperations.asDeterministic(input));
  }

  // Verifies that asDeterministic throws when the NFA has λ-transitions
  @Test
  public void lambdaTransitionAsDeterministicThrows() throws FileNotFoundException {
    var input = DotReader.readNFA(RESOURCES_PATH + "/ab_plus/input.dot");
    assertThrows(IllegalStateException.class, () -> AutomataOperations.asDeterministic(input));
  }

  private List<String> toWord(String s) {
    return s.isEmpty() ? Collections.emptyList() : Arrays.asList(s.split(""));
  }
}
