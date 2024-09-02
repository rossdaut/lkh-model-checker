package lkh.automata;

import lkh.utils.Pair;

import java.util.*;

/**
 * A deterministic automaton
 * The automaton can't have empty transitions and can't have multiple transitions from a state with the same symbol
 * @param <State> the type of the states
 * @param <Symbol> the type of the symbols
 */
public class DeterministicAutomaton<State, Symbol> extends AbstractAutomaton<State, Symbol> {
  public static <Action> DeterministicAutomaton<Integer, Action> empty() {
    DeterministicAutomaton<Integer, Action> empty = new DeterministicAutomaton<>();
    empty.setInitialState(0);
    return empty;
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
    if (symbol == null) throw new NullPointerException("symbol can't be null");
    alphabet.add(symbol);
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

  /**
   * Get the target state of a transition
   * @param source the source state
   * @param symbol the symbol of the transition
   * @return the target state of the transition
   */
  public Optional<State> delta(State source, Symbol symbol) {
    Map<Symbol, Set<State>> sourceMap = transitionsMap.get(source);
    if (sourceMap == null) return Optional.empty();

    Set<State> targetStates = sourceMap.get(symbol);
    if (targetStates == null) return Optional.empty();

    return targetStates.stream().findAny();
  }

  /**
   * Evaluate a string in the automaton
   * @param string a list of symbols to consume
   * @return true if the string is accepted by the automaton
   */
  @Override
  public boolean evaluate(List<Symbol> string) {
    Optional<State> currentState = Optional.of(initialState);

    for (Symbol symbol : string) {
      currentState = delta(currentState.get(), symbol);
      if (currentState.isEmpty()) return false;
    }

    return finalStates.contains(currentState.get());
  }

  /**
   * Complete the automaton
   * For each state and for each symbol, if there isn't an outgoing transition from state through symbol,
   * add one with 'error' as the target.
   * 'error' will be defined such that when an evaluation falls there, it will be non-successful
   * @param error a non-null object that will act as the 'error' state
   * @throws IllegalArgumentException if the given error state is part of the automaton
   */
  public void complete(State error) {
    if (error == null) throw new NullPointerException("null state");
    if (transitionsMap.containsKey(error))
      throw new IllegalArgumentException("error state should not already be in the automaton");

    Set<Pair<State, Symbol>> pairsToAdd = new HashSet<>();

    addState(error);

    for (State state : getStates()) {
      for (Symbol symbol : getAlphabet()) {
        if (delta(state, symbol).isEmpty()) {
          pairsToAdd.add(new Pair<>(state, symbol));
        }
      }
    }

    for (Pair<State, Symbol> pair : pairsToAdd) {
      addTransition(pair.key(), error, pair.value());
    }
  }

  /**
   * Clone the automaton
   * @return a new automaton with the same states, transitions, and alphabet
   */
  public DeterministicAutomaton<State, Symbol> clone() {
    DeterministicAutomaton<State, Symbol> cloned = new DeterministicAutomaton<>();
    Optional<State> targetState;

    cloned.initialState = initialState;
    cloned.finalStates.addAll(finalStates);
    cloned.alphabet.addAll(alphabet);

    for (State state : getStates()) {
      for (Symbol symbol : getAlphabet()) {
        targetState = delta(state, symbol);
        targetState.ifPresent(value -> cloned.addTransition(state, value, symbol));
      }
    }

    return cloned;
  }

  /**
   * Check if the automaton recognizes the empty language.
   * @return true iff the automaton doesn't recognize any string
   */
  public boolean isEmpty() {
    Set<State> visited = new HashSet<>();
    Queue<State> unvisited = new LinkedList<>();
    unvisited.add(initialState);

    while(!unvisited.isEmpty()) {
      State currentState = unvisited.remove();
      visited.add(currentState);

      if(isFinal(currentState))
        return false;

      for(Symbol symbol : getAlphabet()) {
        delta(currentState, symbol).ifPresent(target -> {
          if (!visited.contains(target))
            unvisited.add(target);
        });
      }
    }

    return true;
  }
}