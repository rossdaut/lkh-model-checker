package lkh.automata;

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
 * Tests for {@link lkh.automata.impl.GraphDeterministicAutomaton#complete}.
 *
 * Each test case has its own subfolder under src/test/resources/automata/complete/:
 *   - input.dot   : the incomplete DFA
 *   - expected.dot: the expected complete DFA (with sink state "error")
 */
public class CompleteTest {

  private static final String RESOURCES_PATH = "src/test/resources/automata/complete";

  // ALPHABET: {a,b,c}
  // LANGUAGE: (ac|b+)+
  // Adding state "error" for all missing transitions.
  //
  // input.dot    : incomplete DFA for (ac|b+)+
  // expected.dot : complete DFA for (ac|b+)+

  // Verifies that completing the DFA preserves the accepted language
  @ParameterizedTest
  @CsvSource({"b,true", "ac,true", "bbb,true", "acb,true",
              "'',false", "a,false", "c,false", "abc,false"})
  public void acbPlusCompleteLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/acb_plus/input.dot");
    input.complete("error");
    assertEquals(expected, input.evaluate(toWord(s)));
  }

  // Verifies that the completed DFA is identical to the DFA written in expected.dot
  @Test
  public void acbPlusCompleteEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/acb_plus/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/acb_plus/expected.dot");
    input.complete("error");
    assertEquals(expected, input);
  }

  // Verifies that calling complete() again on an already-complete DFA throws an exception
  // (the "error" state would already be present)
  @Test
  public void acbPlusCompleteTwiceThrows() throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/acb_plus/input.dot");
    input.complete("error");
    assertThrows(IllegalArgumentException.class, () -> input.complete("error"));
  }

  // ALPHABET: {a,b,c}
  // LANGUAGE: {acb}  (only the string "acb")
  //
  // input.dot    : incomplete DFA for {acb}
  // expected.dot : complete DFA for {acb}

  // Verifies that completing the DFA preserves the accepted language
  @ParameterizedTest
  @CsvSource({"acb,true", "'',false", "a,false", "ac,false", "acbc,false", "b,false"})
  public void acbCompleteLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/acb/input.dot");
    input.complete("error");
    assertEquals(expected, input.evaluate(toWord(s)));
  }

  // Verifies that the completed DFA is identical to the DFA written in expected.dot
  @Test
  public void acbCompleteEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/acb/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/acb/expected.dot");
    input.complete("error");
    assertEquals(expected, input);
  }

  // ALPHABET: {0,1}
  // LANGUAGE: 01+  (a zero followed by one or more ones)
  //
  // input.dot    : incomplete DFA for 01+
  // expected.dot : complete DFA for 01+

  // Verifies that completing the DFA preserves the accepted language
  @ParameterizedTest
  @CsvSource({"01,true", "011,true", "0111,true",
              "'',false", "0,false", "1,false", "10,false", "00,false"})
  public void zeroOnePlusCompleteLanguage(String s, boolean expected) throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/zero_one_plus/input.dot");
    input.complete("error");
    assertEquals(expected, input.evaluate(toWord(s)));
  }

  // Verifies that the completed DFA is identical to the DFA written in expected.dot
  @Test
  public void zeroOnePlusCompleteEquals() throws FileNotFoundException {
    var input    = DotReader.readDFA(RESOURCES_PATH + "/zero_one_plus/input.dot");
    var expected = DotReader.readDFA(RESOURCES_PATH + "/zero_one_plus/expected.dot");
    input.complete("error");
    assertEquals(expected, input);
  }

  //Test that calling complete() with a null state throws a NullPointerException
  @Test
  public void completeNullStateThrows() throws FileNotFoundException {
    var input = DotReader.readDFA(RESOURCES_PATH + "/acb_plus/input.dot");
    assertThrows(NullPointerException.class, () -> input.complete(null));
  }

  // ALPHABET: {a,b}
  // LANGUAGE: a
  //
  // input.dot    : incomplete NFA with duplicate transitions on 'a' from state 0
  //                (0 -a-> 1 and 0 -a-> 2) and a lambda transition (2 -> 3)
  // expected.dot : complete NFA with sink state "error" for all missing (state, symbol) pairs
  // Verifies that the completed NFA is identical to the NFA written in expected.dot
  @Test
  public void abStarACompleteEquals() throws FileNotFoundException {
    var input    = DotReader.readNFA(RESOURCES_PATH + "/a/input.dot");
    var expected = DotReader.readNFA(RESOURCES_PATH + "/a/expected.dot");
    input.complete("error");
    assertEquals(expected, input);
  }

  private List<String> toWord(String s) {
    return s.isEmpty() ? Collections.emptyList() : Arrays.asList(s.split(""));
  }
}

