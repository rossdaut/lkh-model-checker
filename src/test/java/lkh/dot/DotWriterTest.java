package lkh.dot;

import lkh.automata.impl.AutomataOperations;
import lkh.automata.impl.GraphDeterministicAutomaton;
import lkh.automata.impl.GraphNonDeterministicAutomaton;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link DotWriter#writeDFA} and {@link DotWriter#writeNFA}.
 *
 * Each test builds an automaton programmatically, writes it to a temp file,
 * reads it back with DotReader, and verifies that the structure and language
 * are preserved after the round-trip.
 *
 * No pre-existing .dot files are required: the files are created and deleted
 * within each test via a temp path.
 */
public class DotWriterTest {

  // ALPHABET: {a, b}
  // LANGUAGE: (a+b)*a+
  //
  // State 0: initial, non-final
  // State 1: final
  //
  // Writes a complete DFA, reads it back and checks structural equality

  // Verifies that writeDFA preserves the automaton structure
  @Test
  public void dfaSimpleRoundTripEquals() throws IOException {
    var dfa = buildDfaSimple();
    Path tmp = tempFile();
    DotWriter.writeDFA(dfa, tmp.toString());
    var read = DotReader.readDFA(tmp.toString());
    assertEquals(AutomataOperations.toIntegerStates(read), dfa);
    Files.deleteIfExists(tmp);
  }

  // Verifies that the DFA read back from disk accepts the same language
  @ParameterizedTest
  @CsvSource({"a,true", "aa,true", "ba,true", "bba,true", "aba,true",
      "'',false", "b,false", "ab,false", "aab,false"})
  public void dfaSimpleRoundTripLanguage(String s, boolean expected) throws IOException {
    var dfa = buildDfaSimple();
    Path tmp = tempFile();
    DotWriter.writeDFA(dfa, tmp.toString());
    var read = DotReader.readDFA(tmp.toString());
    assertEquals(expected, read.evaluate(toWord(s)));
    Files.deleteIfExists(tmp);
  }

  // ALPHABET: {a, b}
  // LANGUAGE: ((a|b)b)* (a|b)a
  //
  // State 0: initial
  // State 1: intermediate
  // State 2: final
  //
  // Writes an NFA, reads it back and checks structural equality

  // Verifies that writeNFA preserves the automaton structure
  @Test
  public void nfaSimpleRoundTripEquals() throws IOException {
    var nfa = buildNfaSimple();
    Path tmp = tempFile();
    DotWriter.writeNFA(nfa, tmp.toString());
    var read = DotReader.readNFA(tmp.toString());
    assertEquals(nfa, read);
    Files.deleteIfExists(tmp);
  }

  // Verifies that the NFA read back from disk accepts the same language
  @ParameterizedTest
  @CsvSource({"aa,true", "ba,true", "bbbbba,true", "abbbaa,true",
      "'',false", "a,false", "b,false", "ab,false", "bb,false", "aaa,false"})
  public void nfaSimpleRoundTripLanguage(String s, boolean expected) throws IOException {
    var nfa = buildNfaSimple();
    Path tmp = tempFile();
    DotWriter.writeNFA(nfa, tmp.toString());
    var read = DotReader.readNFA(tmp.toString());
    assertEquals(expected, read.evaluate(toWord(s)));
    Files.deleteIfExists(tmp);
  }

  // ALPHABET: {a, b}
  // LANGUAGE: (a|b)+
  //
  // Lambda transition: 1 -> 0
  @Test
  public void nfaLambdaRoundTripEquals() throws IOException {
    var nfa = buildNfaLambda();
    Path tmp = tempFile();
    DotWriter.writeNFA(nfa, tmp.toString());
    var read = DotReader.readNFA(tmp.toString());

    // Verifies that the NFA with lambda reads back as an equal automaton
    assertEquals(nfa, read);

    // Verifies that lambda transitions survive the write/read round-trip
    assertFalse(read.emptyDelta("1").isEmpty());
    assertTrue(read.emptyDelta("1").contains("0"));
    Files.deleteIfExists(tmp);
  }

  // -- helpers --

  private GraphDeterministicAutomaton<Integer, String> buildDfaSimple() {
    var dfa = new GraphDeterministicAutomaton<Integer, String>();
    dfa.setInitialState(0);
    dfa.addFinalState(1);
    dfa.addTransition(0, 1, "a");
    dfa.addTransition(1, 1, "a");
    dfa.addTransition(1, 0, "b");
    dfa.addTransition(0, 0, "b");
    return dfa;
  }

  private GraphNonDeterministicAutomaton<String, String> buildNfaSimple() {
    var nfa = new GraphNonDeterministicAutomaton<String, String>();
    nfa.setInitialState("0");
    nfa.addFinalState("2");
    nfa.addTransition("0", "1", "a");
    nfa.addTransition("0", "1", "b");
    nfa.addTransition("1", "2", "a");
    nfa.addTransition("1", "0", "b");
    return nfa;
  }

  private GraphNonDeterministicAutomaton<String, String> buildNfaLambda() {
    var nfa = new GraphNonDeterministicAutomaton<String, String>();
    nfa.setInitialState("0");
    nfa.addFinalState("1");
    nfa.addTransition("0", "1", "a");
    nfa.addTransition("0", "1", "b");
    nfa.addEmptyTransition("1", "0");
    return nfa;
  }

  private Path tempFile() throws IOException {
    return Files.createTempFile("dot_writer_test_", ".dot");
  }

  private List<String> toWord(String s) {
    return s.isEmpty() ? Collections.emptyList() : Arrays.asList(s.split(""));
  }
}
