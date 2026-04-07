  package lkh.automata.impl;

import lkh.automata.Automaton;
import lkh.graph.DirectedGraph;
import lkh.graph.HashMapDirectedGraph;
import lkh.utils.Pair;
import logger.Logger;
import logger.LoggerContext;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;

/**
 * Abstract base class for graph-based automaton implementations.
 * Uses the DirectedGraph from the graph package to represent the automaton structure,
 * where vertices are states and edges are transitions labeled with symbols.
 *
 * @param <State> the type of the states
 * @param <Symbol> the type of the symbols
 */
@EqualsAndHashCode(callSuper = false)
public abstract class GraphAutomaton<State, Symbol> implements Automaton<State, Symbol> {
  /**
   * Directed graph representing the automaton structure.
   * Vertices represent states and edges represent transitions labeled with symbols.
   * The use of DirectedGraph delegates graph operations to the graph package,
   * simplifying the automaton implementation.
   */
  protected final DirectedGraph<State, AutomatonEdge<State, Symbol>> graph;
  @Getter
  protected State initialState;
  @Getter
  protected final Set<State> finalStates = new HashSet<>();
  @Getter
  protected final Set<Symbol> alphabet = new HashSet<>();

  public GraphAutomaton() {
      // HashMapDirectedGraph will automatically pick up logger from LoggerContext
      this.graph = new HashMapDirectedGraph<>();
  }

  public GraphAutomaton(Logger logger) {
      this.graph = new HashMapDirectedGraph<>(logger);
  }

  @Override
  public void setInitialState(State initialState) {
    graph.addVertex(initialState);
    this.initialState = initialState;
  }

  @Override
  public void addFinalState(State state) {
    graph.addVertex(state);
    finalStates.add(state);
  }

  @Override
  public void addFinalStates(Set<State> states) {
    finalStates.addAll(states);
  }

  @Override
  public void addState(State state) {
    if (state == null) throw new NullPointerException("null state");

    graph.addVertex(state);
  }

  @Override
  public void addTransition(State source, State target, Symbol symbol) {
    graph.addVertex(source);
    graph.addVertex(target);
    graph.addEdge(new AutomatonEdge<>(source, target, symbol));
  }

  @Override
  public Set<State> getStates() {
    return graph.getVertices();
  }

  @Override
  public Set<State> getNonFinalStates() {
    Set<State> nonFinalStates = new HashSet<>(getStates());
    nonFinalStates.removeAll(finalStates);
    return nonFinalStates;
  }

  @Override
  public boolean containsState(State state) {
    return graph.containsVertex(state);
  }

  @Override
  public boolean isFinal(State state) {
    return finalStates.contains(state);
  }

  @Override
  public Set<Pair<Symbol, State>> outgoingTransitions(State state) {
    Set<Pair<Symbol, State>> transitions = new HashSet<>();

    for (var edge : graph.getOutgoingEdges(state)) {
      transitions.add(new Pair<>(edge.getSymbol(), edge.getTarget()));
    }

    return transitions;
  }

  @Override
  public abstract boolean evaluate(List<Symbol> string);

  protected abstract boolean hasTransition(State source, Symbol symbol);

  @Override
  public void complete(State error) {
    if (error == null) throw new NullPointerException("null state");
    if (graph.containsVertex(error))
      throw new IllegalArgumentException("error state should not already be in the automaton");

    Set<Pair<State, Symbol>> pairsToAdd = new HashSet<>();

    addState(error);

    for (State state : getStates()) {
      for (Symbol symbol : getAlphabet()) {
        if (!hasTransition(state, symbol)) {
          pairsToAdd.add(new Pair<>(state, symbol));
        }
      }
    }

    for (Pair<State, Symbol> pair : pairsToAdd) {
      addTransition(pair.key(), error, pair.value());
    }
  }

  /**
   * Get the size of the automaton (number of states and transitions)
   * @return a Pair with (states, transitions)
   */
  public Pair<Integer, Integer> getSize() {
    return graph.getSize();
  }
}
