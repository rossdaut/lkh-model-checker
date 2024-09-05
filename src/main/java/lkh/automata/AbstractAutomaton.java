  package lkh.automata;

import lkh.utils.Pair;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;

/**
 * An abstract automaton
 * @param <State> the type of the states
 * @param <Symbol> the type of the symbols
 */
@EqualsAndHashCode(callSuper = false)
public abstract class AbstractAutomaton<State, Symbol> {
  // Map of transitions
  protected final Map<State, Map<Symbol, Set<State>>> transitionsMap = new HashMap<>();
  @Getter
  protected State initialState;
  @Getter
  protected final Set<State> finalStates = new HashSet<>();
  @Getter
  protected final Set<Symbol> alphabet = new HashSet<>();

  /**
   * Add a initial state to the automaton
   * @param initialState the state to add
   */
  public void setInitialState(State initialState) {
    addState(initialState);
    this.initialState = initialState;
  }

  /**
   * Add a final state to the automaton
   * @param state the state to add
   * @return true if the state was not already in the final states set
   */
  public boolean addFinalState(State state) {
    addState(state);
    return finalStates.add(state);
  }

  public void addFinalStates(Set<State> states) {
    finalStates.addAll(states);
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
   * @param source the source state
   * @param target the target state
   * @param symbol the symbol of the transition
   * @return true if the transition was not already in the automaton
   */
  public boolean addTransition(State source, State target, Symbol symbol) {
    addState(source);
    addState(target);

    transitionsMap.get(source).putIfAbsent(symbol, new HashSet<>());

    return transitionsMap
            .get(source)
            .get(symbol)
            .add(target);
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
   * Return whether the given state is final
   * @param state a State object
   * @return true if the state is final, false otherwise
   */
  public boolean isFinal(State state) {
    return finalStates.contains(state);
  }

  public Set<Pair<Symbol, State>> outgoingTransitions(State state) {
    Set<Pair<Symbol, State>> transitions = new HashSet<>();

    for (var entry : transitionsMap.get(state).entrySet()) {
      for (var target : entry.getValue()) {
        transitions.add(new Pair<>(entry.getKey(), target));
      }
    }

    return transitions;
  }

  /**
   * Evaluate a string with the automaton
   * @param string a list of symbols to consume
   * @return true if the string is accepted by the automaton
   */
  abstract boolean evaluate(List<Symbol> string);

  /**
   * Complete the automaton
   * For each state and for each symbol, if there isn't an outgoing transition from state through symbol,
   * add one with 'error' as the target.
   * 'error' will be defined such that when an evaluation falls there, it will be non-successful
   * @param error a non-null object that will act as the 'error' state
   * @throws IllegalArgumentException if the given error state is part of the automaton
   */
  abstract void complete(State error);
}
