package lkh.dot;

import lkh.automata.NonDeterministicAutomaton;

import java.io.FileNotFoundException;

public class ReadTest {
  public static void main(String[] args) {
    try {
      NonDeterministicAutomaton<String, String> nfa = DotReader.readNFA("test.dot");
      System.out.println(nfa);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
