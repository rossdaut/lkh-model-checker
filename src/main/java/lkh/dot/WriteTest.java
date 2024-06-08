package lkh.dot;

import lkh.automata.NonDeterministicAutomaton;

public class WriteTest {
  public static void main(String[] args) {
    NonDeterministicAutomaton<Integer, Character> a = new NonDeterministicAutomaton<>();

    a.addTransition(0, 1, 'a');
    a.addTransition(0, 1, 'b');
    a.addTransition(0, 2, 'b');
    a.addEmptyTransition(0, 3);

    a.setInitialState(0);
    a.addFinalState(2);
    a.addFinalState(3);

    System.out.println(a.getStates());
    System.out.println(a.delta(0, 'b'));

    DotWriter.writeNFA(a, "test.dot");
  }
}
