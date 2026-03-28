package lkh.automata;

import lkh.automata.impl.AutomataOperations;
import lkh.automata.impl.GraphDeterministicAutomaton;
import lkh.automata.impl.GraphNonDeterministicAutomaton;
import lkh.dot.DotReader;
import lkh.dot.DotWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the three overloads of {@link AutomataOperations#intersection}:
 *   1. intersection(DFA, DFA)
 *   2. intersection(NFA, NFA)
 *   3. intersection(Set<DFA>)
 *
 * All resources are self-contained inside {@code src/test/resources/automata/intersection/}.
 * See that folder's README.md for language descriptions.
 *
 * For each case two things are verified:
 *   1. Language correctness — the result accepts/rejects the expected strings.
 *   2. Structural equality  — the result equals the pre-computed expected .dot file.
 */
public class IntersectionTest {

  private static final String BASE = "src/test/resources/automata/intersection";

  // intersection(DFA, DFA)

  // ALPHABETS: {a} {b}
  // L1: a+   L2: b+   L1 ∩ L2 = ∅  (disjoint alphabets)
  //
  // dfa1.dot : DFA for a+
  // dfa2.dot : DFA for b+
  // expected.dot : DFA for ∅ (no final states)

  // Verifies that the intersection rejects every string (empty language)
  @ParameterizedTest
  @CsvSource({"a,false", "b,false", "aa,false", "bb,false", "'',false"})
  public void aplusBplusIntersectionLanguage(String s, boolean expected) throws FileNotFoundException {
    var dfa1 = DotReader.readDFA(BASE + "/dfa_dfa/aplus_bplus/dfa1.dot");
    var dfa2 = DotReader.readDFA(BASE + "/dfa_dfa/aplus_bplus/dfa2.dot");
    var result = AutomataOperations.intersection(dfa1, dfa2);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void aplusBplusIntersectionEquals() throws FileNotFoundException {
    var dfa1     = DotReader.readDFA(BASE + "/dfa_dfa/aplus_bplus/dfa1.dot");
    var dfa2     = DotReader.readDFA(BASE + "/dfa_dfa/aplus_bplus/dfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/dfa_dfa/aplus_bplus/expected.dot"));
    assertEquals(expected, AutomataOperations.intersection(dfa1, dfa2));
  }

  // ALPHABET: {a,b}
  // L1: strings with even number of a's   L2: strings with even number of b's
  // L1 ∩ L2 = strings with even #a and even #b
  //
  // dfa1.dot : DFA for even #a
  // dfa2.dot : DFA for even #b
  // expected.dot : DFA for even #a and even #b

  // Verifies that the intersection accepts exactly strings with even #a and even #b
  @ParameterizedTest
  @CsvSource({"'',true", "aa,true", "bb,true", "aabb,true", "abba,true",
              "a,false", "b,false", "ab,false", "aab,false", "abb,false"})
  public void evenAEvenBIntersectionLanguage(String s, boolean expected) throws FileNotFoundException {
    var dfa1 = DotReader.readDFA(BASE + "/dfa_dfa/even_a_even_b/dfa1.dot");
    var dfa2 = DotReader.readDFA(BASE + "/dfa_dfa/even_a_even_b/dfa2.dot");
    var result = AutomataOperations.intersection(dfa1, dfa2);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void evenAEvenBIntersectionEquals() throws FileNotFoundException {
    var dfa1     = DotReader.readDFA(BASE + "/dfa_dfa/even_a_even_b/dfa1.dot");
    var dfa2     = DotReader.readDFA(BASE + "/dfa_dfa/even_a_even_b/dfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/dfa_dfa/even_a_even_b/expected.dot"));
    assertEquals(expected, AutomataOperations.intersection(dfa1, dfa2));
  }

  // ALPHABET: {a,b}
  // L1: (a|b)*a  (ends with a)   L2: (a|b)*b  (ends with b)
  // L1 ∩ L2 = ∅  (a string cannot end with both a and b)
  //
  // dfa1.dot : DFA for (a|b)*a
  // dfa2.dot : DFA for (a|b)*b
  // expected.dot : DFA for ∅

  // Verifies that the intersection rejects every string (empty language)
  @ParameterizedTest
  @CsvSource({"a,false", "b,false", "ab,false", "ba,false", "aba,false", "bab,false", "'',false"})
  public void endsAEndsBIntersectionLanguage(String s, boolean expected) throws FileNotFoundException {
    var dfa1 = DotReader.readDFA(BASE + "/dfa_dfa/ends_a_ends_b/dfa1.dot");
    var dfa2 = DotReader.readDFA(BASE + "/dfa_dfa/ends_a_ends_b/dfa2.dot");
    var result = AutomataOperations.intersection(dfa1, dfa2);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void endsAEndsBIntersectionEquals() throws FileNotFoundException {
    var dfa1     = DotReader.readDFA(BASE + "/dfa_dfa/ends_a_ends_b/dfa1.dot");
    var dfa2     = DotReader.readDFA(BASE + "/dfa_dfa/ends_a_ends_b/dfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/dfa_dfa/ends_a_ends_b/expected.dot"));
    assertEquals(expected, AutomataOperations.intersection(dfa1, dfa2));
  }

  // ALPHABET: {a}
  // L1: a*   L2: a+   L1 ∩ L2 = a+
  //
  // dfa1.dot : DFA for a*
  // dfa2.dot : DFA for a+
  // expected.dot : DFA for a+

  // Verifies that the intersection accepts exactly a+
  @ParameterizedTest
  @CsvSource({"a,true", "aa,true", "aaa,true", "'',false"})
  public void astarAplusIntersectionLanguage(String s, boolean expected) throws FileNotFoundException {
    var dfa1 = DotReader.readDFA(BASE + "/dfa_dfa/astar_aplus/dfa1.dot");
    var dfa2 = DotReader.readDFA(BASE + "/dfa_dfa/astar_aplus/dfa2.dot");
    var result = AutomataOperations.intersection(dfa1, dfa2);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void astarAplusIntersectionEquals() throws FileNotFoundException {
    var dfa1     = DotReader.readDFA(BASE + "/dfa_dfa/astar_aplus/dfa1.dot");
    var dfa2     = DotReader.readDFA(BASE + "/dfa_dfa/astar_aplus/dfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/dfa_dfa/astar_aplus/expected.dot"));
    assertEquals(expected, AutomataOperations.intersection(dfa1, dfa2));
  }

  // ALPHABET: {a,b,c}
  // L1: (ac|b+)+   L2: b*   L1 ∩ L2 = b+
  //
  // dfa1.dot : complete DFA for (ac|b+)+
  // dfa2.dot : DFA for b*
  // expected.dot : DFA for b+

  // Verifies that the intersection accepts exactly b+
  @ParameterizedTest
  @CsvSource({"b,true", "bb,true", "bbb,true",
              "'',false", "a,false", "ac,false", "acb,false", "bc,false"})
  public void acbPlusBstarIntersectionLanguage(String s, boolean expected) throws FileNotFoundException {
    var dfa1 = DotReader.readDFA(BASE + "/dfa_dfa/acb_plus_bstar/dfa1.dot");
    var dfa2 = DotReader.readDFA(BASE + "/dfa_dfa/acb_plus_bstar/dfa2.dot");
    var result = AutomataOperations.intersection(dfa1, dfa2);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void acbPlusBstarIntersectionEquals() throws FileNotFoundException {
    var dfa1     = DotReader.readDFA(BASE + "/dfa_dfa/acb_plus_bstar/dfa1.dot");
    var dfa2     = DotReader.readDFA(BASE + "/dfa_dfa/acb_plus_bstar/dfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/dfa_dfa/acb_plus_bstar/expected.dot"));
    assertEquals(expected, AutomataOperations.intersection(dfa1, dfa2));
  }

  // intersection(NFA, NFA)

  // ALPHABET: {a,b}
  // L1: a(a|b)   L2: (a|b)b   L1 ∩ L2 = {ab}
  // NFA feature: nfa1 has two transitions from q1 on different symbols to the same target;
  //              nfa2 has two transitions from p0 on different symbols to the same target.
  //
  // nfa1.dot : NFA for a(a|b)
  // nfa2.dot : NFA for (a|b)b
  // expected.dot : NFA for {ab}

  // Verifies that the intersection accepts exactly {ab}
  @ParameterizedTest
  @CsvSource({"ab,true", "aa,false", "b,false", "a,false", "'',false", "abb,false", "aab,false"})
  public void twoTargetsIntersectionLanguage(String s, boolean expected) throws FileNotFoundException {
    var nfa1 = DotReader.readNFA(BASE + "/nfa_nfa/two_targets/nfa1.dot");
    var nfa2 = DotReader.readNFA(BASE + "/nfa_nfa/two_targets/nfa2.dot");
    var result = AutomataOperations.intersection(nfa1, nfa2);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the NFA written in expected.dot
  @Test
  public void twoTargetsIntersectionEquals() throws FileNotFoundException {
    GraphNonDeterministicAutomaton<String, String> nfa1 = DotReader.readNFA(BASE + "/nfa_nfa/two_targets/nfa1.dot");
    GraphNonDeterministicAutomaton<String, String> nfa2 = DotReader.readNFA(BASE + "/nfa_nfa/two_targets/nfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/nfa_nfa/two_targets/expected.dot"));
    assertEquals(expected, intersectNFAs(nfa1, nfa2));
  }

  // ALPHABET: {a,b}
  // L1: b | ab  (λ-transition from initial state)   L2: ab*   L1 ∩ L2 = {ab}
  // NFA feature: nfa1 has a λ-transition from q0 to q1.
  //
  // nfa1.dot : NFA for b | ab
  // nfa2.dot : NFA for ab*
  // expected.dot : NFA for {ab}

  // Verifies that the intersection accepts exactly {ab}
  @ParameterizedTest
  @CsvSource({"ab,true", "b,false", "a,false", "'',false", "abb,false", "aab,false"})
  public void lambdaStartIntersectionLanguage(String s, boolean expected) throws FileNotFoundException {
    var nfa1 = DotReader.readNFA(BASE + "/nfa_nfa/lambda_start/nfa1.dot");
    var nfa2 = DotReader.readNFA(BASE + "/nfa_nfa/lambda_start/nfa2.dot");
    var result = AutomataOperations.intersection(nfa1, nfa2);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the NFA written in expected.dot
  @Test
  public void lambdaStartIntersectionEquals() throws FileNotFoundException {
    GraphNonDeterministicAutomaton<String, String> nfa2 = DotReader.readNFA(BASE + "/nfa_nfa/lambda_start/nfa1.dot");
    GraphNonDeterministicAutomaton<String, String> nfa1 = DotReader.readNFA(BASE + "/nfa_nfa/lambda_start/nfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/nfa_nfa/lambda_start/expected.dot"));
    assertEquals(expected, intersectNFAs(nfa1, nfa2));
  }

  // ALPHABET: {a,b}
  // L1: a+b   L2: a+(b|ε)  (λ-transition into final state)   L1 ∩ L2 = a+b
  // NFA feature: nfa2 has a λ-transition from p1 into the final state p2.
  //
  // nfa1.dot : NFA for a+b
  // nfa2.dot : NFA for a+(b|ε)
  // expected.dot : NFA for a+b

  // Verifies that the intersection accepts exactly a+b
  @ParameterizedTest
  @CsvSource({"ab,true", "aab,true", "aaab,true",
              "'',false", "a,false", "aa,false", "b,false", "ba,false", "abb,false"})
  public void lambdaAcceptIntersectionLanguage(String s, boolean expected) throws FileNotFoundException {
    var nfa1 = DotReader.readNFA(BASE + "/nfa_nfa/lambda_accept/nfa1.dot");
    var nfa2 = DotReader.readNFA(BASE + "/nfa_nfa/lambda_accept/nfa2.dot");
    var result = AutomataOperations.intersection(nfa1, nfa2);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the NFA written in expected.dot
  @Test
  public void lambdaAcceptIntersectionEquals() throws FileNotFoundException {
    GraphNonDeterministicAutomaton<String, String> nfa1 = DotReader.readNFA(BASE + "/nfa_nfa/lambda_accept/nfa1.dot");
    GraphNonDeterministicAutomaton<String, String> nfa2 = DotReader.readNFA(BASE + "/nfa_nfa/lambda_accept/nfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/nfa_nfa/lambda_accept/expected.dot"));
    assertEquals(expected, intersectNFAs(nfa1, nfa2));
  }

  // ALPHABET: {a,b}
  // L1: (a|b)*a   L2: a(a|b)*   L1 ∩ L2 = strings over {a,b} starting and ending with a
  // NFA feature: nfa1 has two a-transitions from q0 plus a λ-transition from q1 to q2;
  //              nfa2 has two a-transitions from p0 to distinct looping states.
  //
  // nfa1.dot : NFA for (a|b)*a
  // nfa2.dot : NFA for a(a|b)*
  // expected.dot : DFA for strings starting and ending with a

  // Verifies that the intersection accepts exactly strings starting and ending with a
  @ParameterizedTest
  @CsvSource({"a,true", "aa,true", "aba,true", "aaba,true", "abba,true",
              "'',false", "b,false", "ab,false", "ba,false", "bb,false", "bab,false"})
  public void mixedNdIntersectionLanguage(String s, boolean expected) throws FileNotFoundException {
    var nfa1 = DotReader.readNFA(BASE + "/nfa_nfa/mixed_nd/nfa1.dot");
    var nfa2 = DotReader.readNFA(BASE + "/nfa_nfa/mixed_nd/nfa2.dot");
    var result = AutomataOperations.intersection(nfa1, nfa2);
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void mixedNdIntersectionEquals() throws FileNotFoundException {
    GraphNonDeterministicAutomaton<String, String> nfa1 = DotReader.readNFA(BASE + "/nfa_nfa/mixed_nd/nfa1.dot");
    GraphNonDeterministicAutomaton<String, String> nfa2 = DotReader.readNFA(BASE + "/nfa_nfa/mixed_nd/nfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/nfa_nfa/mixed_nd/expected.dot"));
    assertEquals(expected, intersectNFAs(nfa1, nfa2));
  }

  // Collection intersection

  @Test
  public void collectionIntersectionNullThrows() {
    assertThrows(NullPointerException.class, () -> AutomataOperations.intersection((List<GraphDeterministicAutomaton<Integer, String>>) null));
  }

  // Verifies that passing an empty collection throws IllegalArgumentException
  @Test
  public void collectionIntersectionEmptyThrows() {
    assertThrows(IllegalArgumentException.class, () -> AutomataOperations.intersection(new ArrayList<>()));
  }

  // ALPHABET: {a}
  // Collection contains a single DFA for a+.   Result = a+
  //
  // dfa.dot : DFA for a+
  // expected.dot : DFA for a+

  // Verifies that a singleton collection returns the same language
  @ParameterizedTest
  @CsvSource({"a,true", "aa,true", "'',false"})
  public void collectionIntersectionSingleLanguage(String s, boolean expected) throws FileNotFoundException {
    var dfa = DotReader.readDFA(BASE + "/set/single/dfa.dot");
    var result = AutomataOperations.intersection(List.of(dfa));
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void collectionIntersectionSingleEquals() throws FileNotFoundException {
    var dfa      = DotReader.readDFA(BASE + "/set/single/dfa.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/set/single/expected.dot"));
    assertEquals(expected, AutomataOperations.intersection(List.of(dfa)));
  }

  // ALPHABET: {a}
  // L1: a*   L2: a+   L1 ∩ L2 = a+
  //
  // dfa1.dot : DFA for a*
  // dfa2.dot : DFA for a+
  // expected.dot : DFA for a+

  // Verifies that the intersection of two DFAs accepts exactly a+
  @ParameterizedTest
  @CsvSource({"a,true", "aa,true", "aaa,true", "'',false"})
  public void collectionIntersectionTwoDfasLanguage(String s, boolean expected) throws FileNotFoundException {
    var dfa1 = DotReader.readDFA(BASE + "/set/two_dfas/dfa1.dot");
    var dfa2 = DotReader.readDFA(BASE + "/set/two_dfas/dfa2.dot");
    var result = AutomataOperations.intersection(List.of(dfa1, dfa2));
    assertEquals(expected, result.evaluate(toWord(s)));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void collectionIntersectionTwoDfasEquals() throws FileNotFoundException {
    var dfa1     = DotReader.readDFA(BASE + "/set/two_dfas/dfa1.dot");
    var dfa2     = DotReader.readDFA(BASE + "/set/two_dfas/dfa2.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/set/two_dfas/expected.dot"));
    assertEquals(expected, AutomataOperations.intersection(List.of(dfa1, dfa2)));
  }

  // ALPHABET: {a,b}
  // even_a ∩ even_b ∩ ends_a ∩ a+  = strings with even #a, even #b, ending in a, with ≥1 a
  //
  // A List is used (instead of Set) to guarantee a deterministic iteration order.
  //
  // input: dfa_even_a.dot / dfa_even_b.dot / dfa_ends_a.dot / dfa_aplus.dot
  // expected.dot : DFA for the intersection

  // Verifies that the intersection of four DFAs accepts/rejects the expected strings
  @ParameterizedTest
  @CsvSource({"aa,true", "bbaa,true", "aabbaa,true",
              "a,false", "ab,false", "aab,false", "bb,false", "'',false"})
  public void collectionIntersectionFourDfasLanguage(String sl, boolean expected) throws FileNotFoundException {
    var dfaEvenA = DotReader.readDFA(BASE + "/set/four_dfas/dfa_even_a.dot");
    var dfaEvenB = DotReader.readDFA(BASE + "/set/four_dfas/dfa_even_b.dot");
    var dfaEndsA = DotReader.readDFA(BASE + "/set/four_dfas/dfa_ends_a.dot");
    var dfaAplus = DotReader.readDFA(BASE + "/set/four_dfas/dfa_aplus.dot");
    var result = AutomataOperations.intersection(List.of(dfaEvenA, dfaEvenB, dfaEndsA, dfaAplus));

    List<String> s = toWord(sl);

    assertEquals(dfaEvenA.evaluate(s) && dfaEvenB.evaluate(s) && dfaEndsA.evaluate(s) && dfaAplus.evaluate(s), result.evaluate(s));
    assertEquals(expected, result.evaluate(s));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void collectionIntersectionFourDfasEquals() throws FileNotFoundException {
    var dfaEvenA = DotReader.readDFA(BASE + "/set/four_dfas/dfa_even_a.dot");
    var dfaEvenB = DotReader.readDFA(BASE + "/set/four_dfas/dfa_even_b.dot");
    var dfaEndsA = DotReader.readDFA(BASE + "/set/four_dfas/dfa_ends_a.dot");
    var dfaAplus = DotReader.readDFA(BASE + "/set/four_dfas/dfa_aplus.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/set/four_dfas/expected.dot"));
    assertEquals(expected, AutomataOperations.intersection(List.of(dfaEvenA, dfaEvenB, dfaEndsA, dfaAplus)));
  }

  // ALPHABET: {a,b}
  // (ac | b +)+ ∩ (a+cb)+ ∩ acb = acb
  //
  // dfa1: (ac | b +)+   dfa2: (a+cb)+   intersection: (acb)+
  // dfa3: acb  intersection: acb
  //
  // A List is used (instead of Set) to guarantee a deterministic iteration order.
  //
  // input: dfa1.dot / dfa2.dot / dfa3.dot
  // expected.dot : DFA for the intersection

  // Verifies that the intersection of four DFAs accepts/rejects the expected strings
  @ParameterizedTest
  @CsvSource({"acb,true", "acbacb,false", "'',false", "acacac,false", "acbbac,false", "aacb,false", "aaacbacb,false"})
  public void collectionIntersectionThreeDfasLanguage(String st, boolean expected) throws FileNotFoundException {
    var dfa1 = DotReader.readDFA(BASE + "/set/three_dfas/dfa1.dot");
    var dfa2 = DotReader.readDFA(BASE + "/set/three_dfas/dfa2.dot");
    var dfa3 = DotReader.readDFA(BASE + "/set/three_dfas/dfa3.dot");

    Set<GraphDeterministicAutomaton<String, String>> automata = Set.of(dfa1, dfa2, dfa3);

    GraphDeterministicAutomaton<Integer, String> result = AutomataOperations.intersection(automata);

    List<String> s = toWord(st);

    assertEquals(dfa1.evaluate(s) && dfa2.evaluate(s) && dfa3.evaluate(s), result.evaluate(s));
    assertEquals(expected, result.evaluate(s));
  }

  // Verifies that the resulting automaton is identical to the DFA written in expected.dot
  @Test
  public void collectionIntersectionThreeDfasEquals() throws FileNotFoundException {
    var dfa1 = DotReader.readDFA(BASE + "/set/three_dfas/dfa1.dot");
    var dfa2 = DotReader.readDFA(BASE + "/set/three_dfas/dfa2.dot");
    var dfa3 = DotReader.readDFA(BASE + "/set/three_dfas/dfa3.dot");
    var expected = AutomataOperations.toIntegerStates(DotReader.readDFA(BASE + "/set/three_dfas/expected.dot"));

    List<GraphDeterministicAutomaton<String, String>> automata = List.of(dfa1, dfa2, dfa3);

    GraphDeterministicAutomaton<Integer, String> result = AutomataOperations.intersection(automata);

    assertEquals(expected, result);
  }

  // Helpers

  private <A, B, S> GraphDeterministicAutomaton<Integer, S> intersectNFAs(
      GraphNonDeterministicAutomaton<A, S> a,
      GraphNonDeterministicAutomaton<B, S> b) {
    return AutomataOperations.intersection(a, b);
  }

  private List<String> toWord(String s) {
    return s.isEmpty() ? Collections.emptyList() : Arrays.asList(s.split(""));
  }
}
