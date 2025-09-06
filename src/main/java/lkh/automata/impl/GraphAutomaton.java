  package lkh.automata.impl;

import lkh.automata.Automaton;
import lkh.utils.Pair;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.*;

@EqualsAndHashCode(callSuper = false)
public abstract class GraphAutomaton<State, Symbol> implements Automaton<State, Symbol> {
  // Map of transitions
  protected final Map<State, Map<Symbol, Set<State>>> transitionsMap = new HashMap<>();
  @Getter
  protected State initialState;
  @Getter
  protected final Set<State> finalStates = new HashSet<>();
  @Getter
  protected final Set<Symbol> alphabet = new HashSet<>();

  @Override
  public void setInitialState(State initialState) {
    addState(initialState);
    this.initialState = initialState;
  }

  @Override
  public void addFinalState(State state) {
    addState(state);
    finalStates.add(state);
  }

  @Override
  public void addFinalStates(Set<State> states) {
    finalStates.addAll(states);
  }

  @Override
  public void addState(State state) {
    if (state == null) throw new NullPointerException("null state");

    transitionsMap.putIfAbsent(state, new HashMap<>());
  }

  @Override
  public void addTransition(State source, State target, Symbol symbol) {
    addState(source);
    addState(target);

    transitionsMap.get(source).putIfAbsent(symbol, new HashSet<>());

    transitionsMap
      .get(source)
      .get(symbol)
      .add(target);
  }

  @Override
  public Set<State> getStates() {
    return transitionsMap.keySet();
  }

  @Override
  public Set<State> getNonFinalStates() {
    Set<State> nonFinalStates = new HashSet<>(getStates());
    nonFinalStates.removeAll(finalStates);
    return nonFinalStates;
  }

  @Override
  public boolean containsState(State state) {
    return transitionsMap.containsKey(state);
  }

  @Override
  public boolean isFinal(State state) {
    return finalStates.contains(state);
  }

  @Override
  public Set<Pair<Symbol, State>> outgoingTransitions(State state) {
    Set<Pair<Symbol, State>> transitions = new HashSet<>();

    for (var entry : transitionsMap.get(state).entrySet()) {
      for (var target : entry.getValue()) {
        transitions.add(new Pair<>(entry.getKey(), target));
      }
    }

    return transitions;
  }

  @Override
  public abstract boolean evaluate(List<Symbol> string);

  @Override
  public abstract void complete(State error);
}
