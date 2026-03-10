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

/**
 * Tests for {@link AutomataOperations#complement}.
 *
 * Each test case has its own subfolder under src/test/resources/automata/complement/:
 *   - input.dot   : the complete DFA to complement
 *   - expected.dot: the expected result DFA
 */
public class ComplementTest {

  private static final String RESOURCES_PATH = "src/test/resources/automata/complement";

  // ALPHABET: {a,b}
  // LANGUAGE: a*  (zero or more 'a's)
  // COMPLEMENT: L = {a,b}* \ a*  ->  strings with at least one 'b'
  //
  // input.dot : DFA for a*
  // expected.dot : DFA for complement(a*)

  // Verifies that the resulting automaton accepts exactly the strings with at least one 'b'
  @ParameterizedTest
  @CsvSource({"b,true", "ab,true", "ba,true", "abb,true", "'',false", "a,false", "aa,false"})
  public void astarComplementLenguaje(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/astar/input.dot");
    var result = AutomataOperations.complement(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void astarComplementEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/astar/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/astar/expected.dot");
    assertEquals(expected, AutomataOperations.complement(input));
  }

  // Verifies that complement(complement(L)) = L (involution)
  @ParameterizedTest
  @CsvSource({"'',true", "a,true", "aa,true", "b,false", "ab,false", "ba,false"})
  public void astarComplementDoble(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/astar/input.dot");
    var doubleComp = AutomataOperations.complement(AutomataOperations.complement(input));
    assertEquals(expected, doubleComp.evaluate(toWord(s)));
  }

  // ALPHABET: {a,b,c}
  // LANGUAGE: (ac|b+)+  (one or more repetitions of "ac" or one or more "b"s)
  // COMPLEMENT: strings not accepted by (ac|b+)+, like "", "a", "c", "abc", etc.
  //
  // input.dot : complete DFA for (ac|b+)+
  // expected.dot : DFA for complement((ac|b+)+)

  // Verifies that the resulting automaton accepts exactly the strings NOT in (ac|b+)+
  @ParameterizedTest
  @CsvSource({"'',true", "a,true", "c,true", "abc,true",
              "b,false", "ac,false", "bbb,false", "acb,false"})
  public void acbPlusComplementLenguaje(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/acb_plus/input.dot");
    var result = AutomataOperations.complement(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void acbPlusComplementEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/acb_plus/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/acb_plus/expected.dot");
    assertEquals(expected, AutomataOperations.complement(input));
  }

  // ALPHABET: {a,b}
  // LANGUAGE: Σ*
  // COMPLEMENT: ∅
  //
  // input.dot : DFA for Σ*
  // expected.dot : DFA for complement(Σ*)

  // Verifies that the resulting automaton rejects every string (empty language)
  @ParameterizedTest
  @CsvSource({"'',false", "a,false", "b,false", "ab,false", "aabb,false"})
  public void sigmaStarComplementLenguaje(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/sigma_star/input.dot");
    var result = AutomataOperations.complement(input);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void sigmaStarComplementEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/sigma_star/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/sigma_star/expected.dot");
    assertEquals(expected, AutomataOperations.complement(input));
  }

  private List<String> toWord(String s) {
    return s.isEmpty() ? Collections.emptyList() : Arrays.asList(s.split(""));
  }
}
