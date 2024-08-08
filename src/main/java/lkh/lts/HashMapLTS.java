package lkh.lts;

import java.util.*;

public class HashMapLTS<State, Action> implements LTS<State, Action> {
  private final Map<State, Map<Action, Set<State>>> map = new HashMap<>();
  private final Set<Action> actions = new HashSet<>();

  @Override
  public void addState(State state) {
    if (state == null) throw new NullPointerException("null state");

    map.putIfAbsent(state, new HashMap<>());
  }

  @Override
  public void addTransition(State source, State target, Action action) {
    if (action == null) throw new NullPointerException("null action");

    addState(source);
    addState(target);
    actions.add(action);

    map.get(source).putIfAbsent(action, new HashSet<>());
    map.get(source).get(action).add(target);
  }

  @Override
  public Set<State> getStates() {
    return map.keySet();
  }

  @Override
  public Set<Action> getActions() {
    return actions;
  }

  @Override
  public boolean containsState(State state) {
    return getStates().contains(state);
  }

  @Override
  public Set<State> targets(State from, Action action) {
    if (!containsState(from))
      throw new IllegalArgumentException("lts doesn't contain the given state");

    return map.get(from).getOrDefault(action, new HashSet<>());
  }
}
