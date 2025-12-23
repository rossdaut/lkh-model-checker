package lkh.automata.impl;

import lkh.automata.NonDeterministicAutomaton;
import lkh.utils.Pair;

import java.util.*;

/**
 * A non-deterministic automaton
 * The automaton can have empty transitions and multiple transitions from a state with the same symbol
 * @param <State> the type of the states
 * @param <Symbol> the type of the symbols
 */
public class GraphNonDeterministicAutomaton<State, Symbol>
    extends GraphAutomaton<State, Symbol>
    implements NonDeterministicAutomaton<State, Symbol> {


  @Override
  public void addTransition(State source, State target, Symbol symbol) {
    if (symbol == null) throw new NullPointerException("symbol can't be null");

    alphabet.add(symbol);
    super.addTransition(source, target, symbol);
  }

  @Override
  public void addEmptyTransition(State source, State target) {
    super.addTransition(source, target, null);
  }

  @Override
  public Set<State> delta(State source, Symbol symbol) {
    if (!containsState(source)) throw new IllegalArgumentException("source state not in states set");

    return new HashSet<>(graph.getOutgoingNeighbors(
        source,
        edge -> edge.hasSymbol(symbol)
    ));
  }

  @Override
  public Set<State> emptyDelta(State source) {
    return delta(source, null);
  }

  @Override
  public Set<State> lambdaClosure(Set<State> states) {
    Set<State> result = new HashSet<>();

    states.forEach(s ->
      result.addAll(lambdaClosure(s))
    );

    return result;
  }

  @Override
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

  @Override
  public Set<State> move(Set<State> states, Symbol symbol) {
    Set<State> result = new HashSet<>();
    for (State state : states) {
      result.addAll(delta(state, symbol));
    }
    return result;
  }

  @Override
  public boolean evaluate(List<Symbol> string) {
    Set<State> currentStates = lambdaClosure(initialState);

    for (Symbol symbol : string) {
      currentStates = lambdaClosure(move(currentStates, symbol));
      if (currentStates.isEmpty()) return false;
    }

    currentStates.retainAll(finalStates);
    return !currentStates.isEmpty();
  }

  @Override
  public void complete(State error) {
    if (error == null) throw new NullPointerException("null state");
    if (graph.containsVertex(error))
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
  @Override
  public GraphNonDeterministicAutomaton<State, Symbol> clone() {
    GraphNonDeterministicAutomaton<State, Symbol> cloned = new GraphNonDeterministicAutomaton<>();

    cloned.initialState = initialState;
    cloned.finalStates.addAll(finalStates);
    cloned.alphabet.addAll(alphabet);

    for (State state : getStates()) {
      for (Symbol symbol : getAlphabet()) {
        Set<State> targetStates = delta(state, symbol);
        for (State target : targetStates) {
          cloned.addTransition(state, target, symbol);
        }
      }

      for (State target : emptyDelta(state)) {
        cloned.addEmptyTransition(state, target);
      }
    }

    return cloned;
  }
}

