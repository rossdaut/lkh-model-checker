package lkh.automata;

import lkh.utils.Pair;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;

/**
 * A non-deterministic automaton
 * The automaton can have empty transitions and multiple transitions from a state with the same symbol
 * @param <State> the type of the states
 * @param <Symbol> the type of the symbols
 */
@EqualsAndHashCode(callSuper = false)
public class NonDeterministicAutomaton<State, Symbol> {
  // Map of transitions
  protected final Map<State, Map<Symbol, Set<State>>> transitionsMap = new HashMap<>();
  @Getter
  protected State initialState;
  @Getter
  protected final Set<State> finalStates = new HashSet<>();
  @Getter
  protected final Set<Symbol> alphabet = new HashSet<>();

  public void setInitialState(State initialState) {
    addState(initialState);
    this.initialState = initialState;
  }

  public boolean addFinalState(State state) {
    addState(state);
    return finalStates.add(state);
  }
  /**
   * Add a state to the automaton
   * @param state the state to add
   * @return true if the state was not already in the automaton
   */
  public boolean addState(State state) {
    if (state == null) throw new NullPointerException("null state");
    return transitionsMap.putIfAbsent(state, new HashMap<>()) == null;
  }

  /**
   * Add a transition to the automaton
   * Symbol can't be null and will be added to the alphabet
   * @param source the source state
   * @param target the target state
   * @param symbol the symbol of the transition
   * @return true if the transition was not already in the automaton
   */
  public boolean addTransition(State source, State target, Symbol symbol) {
    if (symbol == null) throw new NullPointerException("symbol can't be null");

    alphabet.add(symbol);
    return addTransitionAux(source, target, symbol);
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
   * Add an empty transition to the non-deterministic automaton
   * @param source the source state
   * @param target the target state
   * @return true if the empty transition was not already in the automaton
   */
  public boolean addEmptyTransition(State source, State target) {
    return addTransitionAux(source, target, null);
  }

  /**
   * Get the set of states of the automaton
   * @return the set of states
   */
  public Set<State> getStates() {
    return transitionsMap.keySet();
  }

  /**
   * Check if the automaton contains a state
   * @param state the state to check
   * @return true if the state is in the automaton
   */
  public boolean containsState(State state) {
    return transitionsMap.containsKey(state);
  }

  /**
   * Get the set of states reachable from source by consuming the symbol
   * @param source the source state
   * @param symbol the symbol to consume
   * @return the set of target states
   */
  public Set<State> delta(State source, Symbol symbol) {
    if (!containsState(source)) throw new IllegalArgumentException("source state not in states set");

    return transitionsMap
        .get(source)
        .getOrDefault(symbol, new HashSet<>());
  }

  /**
   * Get all states reachable from source by an empty transition
   * @param source the source state
   * @return states reachable from source by an empty transition
   */
  public Set<State> emptyDelta(State source) {
    return delta(source, null);
  }

  /**
   * Get the set of states reachable from any of the given states
   * by consuming empty transitions consecutively
   * @param states the set of source states
   * @return the set of target states
   */
  public Set<State> lambdaClosure(Set<State> states) {
    Set<State> result = new HashSet<>();

    states.forEach(s ->
      result.addAll(lambdaClosure(s))
    );

    return result;
  }

  /**
   * Get the set of states reachable from the given state
   * by consuming empty transitions consecutively
   * @param state the source state
   * @return the set of target states
   */
  public Set<State> lambdaClosure(State state) {
    Set<State> result = new HashSet<>(Collections.singleton(state));  // result = { state }
    Set<State> newStates = new HashSet<>();

    boolean changed = true;

    while (changed) {
      result.forEach(s -> newStates.addAll(emptyDelta(s)));
      changed = result.addAll(newStates);
      newStates.clear();
    }

    return result;
  }

  /**
   * Return the set of states reached by consuming the symbol from any of the given states.
   * Does not consider empty transitions.
   * Does not include the source states, except for the ones that have a loop.
   * @param states the set of source states
   * @param symbol the symbol to consume
   * @return the set of target states
   */
  public Set<State> move(Set<State> states, Symbol symbol) {
    Set<State> result = new HashSet<>();
    for (State state : states) {
      result.addAll(delta(state, symbol));
    }
    return result;
  }

  /**
   * Evaluate a string in the automaton
   * At each step, empty transitions are considered
   * @param string a list of symbols to consume
   * @return true if the string is accepted by the automaton
   */
  public boolean evaluate(List<Symbol> string) {
    Set<State> currentStates = lambdaClosure(initialState);

    for (Symbol symbol : string) {
      currentStates = lambdaClosure(move(currentStates, symbol));
      if (currentStates.isEmpty()) return false;
    }

    currentStates.retainAll(finalStates);
    return !currentStates.isEmpty();
  }

  /**
   * Add a transition to the automaton
   * @param source the source state
   * @param target the target state
   * @param symbol the symbol of the transition
   * @return true if the transition was not already in the automaton
   */
  private boolean addTransitionAux(State source, State target, Symbol symbol) {
    addState(source);
    addState(target);

    transitionsMap.get(source).putIfAbsent(symbol, new HashSet<>());

    return transitionsMap
        .get(source)
        .get(symbol)
        .add(target);
  }
}