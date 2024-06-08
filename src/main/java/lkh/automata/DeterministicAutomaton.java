package lkh.automata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DeterministicAutomaton<State, Symbol> extends NonDeterministicAutomaton<State, Symbol> {
  @Override
  public boolean addEmptyTransition(State source, State target) {
    throw new UnsupportedOperationException("Cannot add an empty transition to a FDA");
  }

  @Override
  public boolean addTransition(State source, State target, Symbol symbol) {
    removeTransition(source, symbol);
    return super.addTransition(source, target, symbol);
  }

  private boolean removeTransition(State source, Symbol symbol) {
    Map<Symbol, Set<State>> sourceMap = transitionsMap.get(source);
    if (sourceMap == null) return false;

    sourceMap.remove(symbol);
    return true;
  }
}