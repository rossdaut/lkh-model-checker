package lkh.automata;

import java.util.Map;
import java.util.Set;

/**
 * A deterministic automaton
 * The automaton can't have empty transitions and can't have multiple transitions from a state with the same symbol
 * @param <State> the type of the states
 * @param <Symbol> the type of the symbols
 */
public class DeterministicAutomaton<State, Symbol> extends NonDeterministicAutomaton<State, Symbol> {
  /**
   * Cancel the addition of an empty transition
   * @param source the source state
   * @param target the target state
   */
  @Override
  public boolean addEmptyTransition(State source, State target) {
    throw new UnsupportedOperationException("Cannot add an empty transition to a FDA");
  }

  /**
   * Add a transition to the automaton
   * If there is already a transition from source with symbol, it will be replaced
   * If symbol can't be null and if is not in the alphabet, it will be added
   * @param source the source state
   * @param target the target state
   * @param symbol the symbol of the transition
   * @return true if the transition was not already in the automaton
   */
  @Override
  public boolean addTransition(State source, State target, Symbol symbol) {
    removeTransition(source, symbol);
    return super.addTransition(source, target, symbol);
  }

  /**
   * Remove a transition from the automaton
   * @param source the source state
   * @param symbol the symbol of the transition
   * @return true if the transition was in the automaton
   */
  private boolean removeTransition(State source, Symbol symbol) {
    Map<Symbol, Set<State>> sourceMap = transitionsMap.get(source);
    if (sourceMap == null) return false;

    sourceMap.remove(symbol);
    return true;
  }
}