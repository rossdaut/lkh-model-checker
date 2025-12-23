package lkh.automata;

import lkh.automata.impl.GraphDeterministicAutomaton;

import java.util.Optional;

public interface DeterministicAutomaton<State, Symbol> extends Automaton<State, Symbol> {
  /**
   * Get the target state of a transition
   * @param source the source state
   * @param symbol the symbol of the transition
   * @return the target state of the transition
   */
  Optional<State> delta(State source, Symbol symbol);

  /**
   * Clone the automaton
   * @return a new automaton with the same states, transitions, and alphabet
   */
  GraphDeterministicAutomaton<State, Symbol> clone();

  /**
   * Check if the automaton recognizes the empty language.
   * @return true iff the automaton doesn't recognize any string
   */
  boolean isEmpty();
}