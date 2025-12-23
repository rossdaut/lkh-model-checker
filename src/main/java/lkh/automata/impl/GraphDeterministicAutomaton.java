package lkh.automata.impl;

import lkh.automata.DeterministicAutomaton;
import lkh.utils.Pair;

import java.util.*;

/**
 * A deterministic automaton
 * The automaton can't have empty transitions and can't have multiple transitions from a state with the same symbol
 * @param <State> the type of the states
 * @param <Symbol> the type of the symbols
 */
public class GraphDeterministicAutomaton<State, Symbol>
    extends GraphAutomaton<State, Symbol>
    implements DeterministicAutomaton<State, Symbol> {

  public static <Action> GraphDeterministicAutomaton<Integer, Action> empty() {
    GraphDeterministicAutomaton<Integer, Action> empty = new GraphDeterministicAutomaton<>();
    empty.setInitialState(0);
    return empty;
  }

  /**
   * If there is already a transition from source with a (non-null) symbol, it will be replaced
   */
  @Override
  public void addTransition(State source, State target, Symbol symbol) {
    if (symbol == null) throw new NullPointerException("symbol can't be null");

    alphabet.add(symbol);
    removeTransition(source, symbol);
    super.addTransition(source, target, symbol);
  }

  /**
   * Remove a transition from the automaton
   * @param source the source state
   * @param symbol the symbol of the transition
   */
  private void removeTransition(State source, Symbol symbol) {
    graph.removeOutgoingEdgesIf(source, edge ->
      edge.getSymbol().equals(symbol)
    );
  }

  @Override
  public Optional<State> delta(State source, Symbol symbol) {
    if (!graph.containsVertex(source)) return Optional.empty();

    return graph.getOutgoingEdges(source).stream()
      .filter(edge -> (edge.getSymbol().equals(symbol)))
      .map(edge -> edge.getTarget())
      .findFirst();
  }

  @Override
  public boolean evaluate(List<Symbol> string) {
    Optional<State> currentState = Optional.of(initialState);

    for (Symbol symbol : string) {
      currentState = delta(currentState.get(), symbol);
      if (currentState.isEmpty()) return false;
    }

    return finalStates.contains(currentState.get());
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

  @Override
  public GraphDeterministicAutomaton<State, Symbol> clone() {
    GraphDeterministicAutomaton<State, Symbol> cloned = new GraphDeterministicAutomaton<>();

    cloned.initialState = initialState;
    cloned.finalStates.addAll(finalStates);
    cloned.alphabet.addAll(alphabet);

    for (State state : getStates()) {
      for (Symbol symbol : getAlphabet()) {
        Optional<State> targetState = delta(state, symbol);
        targetState.ifPresent(value -> cloned.addTransition(state, value, symbol));
      }
    }

    return cloned;
  }

  @Override
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