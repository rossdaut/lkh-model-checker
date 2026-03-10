package lkh.automata;

import lkh.automata.impl.AutomataOperations;
import lkh.dot.DotReader;
import lkh.dot.DotWriter;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

public class MinimalIntersectionTest {
  @Test
  public void minimalIntersectionTest() throws FileNotFoundException {
    String base = "src/test/resources/automata/intersection/set/four_dfas/";

    var dfaEvenA = DotReader.readDFA(base + "/dfa_even_a.dot");
    var dfaEvenB = DotReader.readDFA(base + "/dfa_even_b.dot");
    var dfaEndsA = DotReader.readDFA(base + "/dfa_ends_a.dot");
    var dfaAplus = DotReader.readDFA(base + "/dfa_aplus.dot");

    var result = AutomataOperations.intersection(List.of(dfaEvenA, dfaEvenB, dfaEndsA, dfaAplus));
    DotWriter.writeDFA(result, "intersection.dot");

    var min_intersection = AutomataOperations.minimize(result);
    DotWriter.writeDFA(min_intersection, "min_intersection.dot");
  }
}
