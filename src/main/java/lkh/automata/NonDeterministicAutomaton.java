package lkh.automata;

import java.util.*;

public class NonDeterministicAutomaton<State, Symbol> extends AbstractAutomaton<State, Symbol> {
  protected final Map<State, Map<Symbol, Set<State>>> transitionsMap = new HashMap<>();

  @Override
  public boolean addState(State state) {
    return transitionsMap.putIfAbsent(state, new HashMap<>()) == null;
  }

  @Override
  public boolean addTransition(State source, State target, Symbol symbol) {
    if (symbol == null) throw new NullPointerException("symbol can't be null");

    alphabet.add(symbol);
    return addTransitionAux(source, target, symbol);
  }

  public boolean addEmptyTransition(State source, State target) {
    return addTransitionAux(source, target, null);
  }

  @Override
  public Set<State> getStates() {
    return transitionsMap.keySet();
  }

  @Override
  public boolean containsState(State state) {
    return transitionsMap.containsKey(state);
  }

  public Set<State> delta(State source, Symbol symbol) {
    return transitionsMap
        .get(source)
        .get(symbol);
  }

  /**
   * Get all states reachable from source by an empty transition
   * @param source the source state
   * @return states reachable from source by an empty transition
   */
  public Set<State> emptyDelta(State source) {
    return delta(source, null);
  }

  public Set<State> lambdaClosure(Set<State> states) {
    Set<State> result = new HashSet<>();

    states.forEach(s ->
      result.addAll(lambdaClosure(s))
    );

    return result;
  }

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

/*
  public Set<State> lambdaClosure2(State state) {
    Set<State> result = new HashSet<>(Collections.singleton(state));  // result = { state }
    Set<State> newStates = new HashSet<>(Collections.singleton(state));
    Set<State> aux = new HashSet<>();

    boolean changed = true;

    while (changed) {
      newStates.forEach(s -> { aux.addAll(emptyDelta(s)); aux.removeAll(result); });
      changed = result.addAll(aux);
      newStates = Set.copyOf(aux);
      aux.clear();
    }

    return result;
  }
*/

  public Set<State> move(Set<State> states, Symbol symbol) {
    Set<State> result = new HashSet<>();
    for (State state : states) {
      result.addAll(delta(state, symbol));
    }
    return result;
  }

  private boolean addTransitionAux(State source, State target, Symbol symbol) {
    addState(source);
    addState(target);

    return transitionsMap
            .get(source)
            .getOrDefault(symbol, new HashSet<>())
            .add(target);
  }
}
