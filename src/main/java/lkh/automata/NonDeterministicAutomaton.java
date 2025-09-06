package lkh.automata;

import java.util.Set;

public interface NonDeterministicAutomaton<State, Symbol> extends Automaton<State, Symbol> {
  /**
   * Get the set of states reachable from source by consuming the symbol
   * @param source the source state
   * @param symbol the symbol to consume
   * @return the set of target states
   */
  Set<State> delta(State source, Symbol symbol);

  /**
   * Get all states reachable from source by an empty transition
   * @param source the source state
   * @return states reachable from source by an empty transition
   */
  Set<State> emptyDelta(State source);

  /**
   * Add an empty transition to the non-deterministic automaton
   * @param source the source state
   * @param target the target state
   */
  void addEmptyTransition(State source, State target);

  /**
   * Get the set of states reachable from any of the given states
   * by consuming empty transitions consecutively
   * @param states the set of source states
   * @return the set of target states
   */
  Set<State> lambdaClosure(Set<State> states);

  /**
   * Get the set of states reachable from the given state
   * by consuming empty transitions consecutively
   * @param state the source state
   * @return the set of target states
   */
  Set<State> lambdaClosure(State state);

  /**
   * Return the set of states reached by consuming the symbol from any of the given states.
   * Does not consider empty transitions.
   * Does not include the source states, except for the ones that have a loop.
   * @param states the set of source states
   * @param symbol the symbol to consume
   * @return the set of target states
   */
  Set<State> move(Set<State> states, Symbol symbol);
}
