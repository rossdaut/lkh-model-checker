package lkh.automata;

import lombok.Getter;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractAutomaton<State, Symbol> {

  @Getter
  protected State initialState;
  protected final Set<State> finalStates = new HashSet<>();
  protected final Set<Symbol> alphabet = new HashSet<>();

  public void setInitialState(State initialState) {
    if (!containsState(initialState))
      throw new IllegalArgumentException("ïnitialState not in states set");

    this.initialState = initialState;
  }

  public boolean addFinalState(State state) {
    if (!containsState(initialState))
      throw new IllegalArgumentException("ïnitialState not in states set");

    return finalStates.add(state);
  }

  public abstract boolean addState(State state);

  public abstract boolean addTransition(State source, State target, Symbol symbol);

  public abstract Set<State> getStates();

  public abstract boolean containsState(State state);
}
