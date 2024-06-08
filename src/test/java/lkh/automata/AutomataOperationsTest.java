package lkh.automata;

import lkh.dot.DotReader;
import lkh.dot.DotWriter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
    DeterministicAutomaton<Integer, String> determinized = AutomataOperations.determinize(nfa);
    DotWriter.writeNFA(determinized, "determinized.dot");
    assertTrue(false);
  }
}
