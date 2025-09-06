package lkh.dot;

import lkh.automata.impl.GraphNonDeterministicAutomaton;

import java.io.FileNotFoundException;

public class ReadTest {
  public static void main(String[] args) {
    try {
      GraphNonDeterministicAutomaton<String, String> nfa = DotReader.readNFA("test.dot");
      System.out.println(nfa);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
