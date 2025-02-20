package lkh.lts;

import java.util.*;

public class HashMapLTS<State, Action> implements LTS<State, Action> {
  private final Map<State, Map<Action, Set<State>>> map = new HashMap<>();
  private final Set<Action> actions = new HashSet<>();
  private final Map<State, Set<String>> labelMap = new HashMap<>();

  @Override
  public void addState(State state) {
    if (state == null) throw new NullPointerException("null state");

    map.putIfAbsent(state, new HashMap<>());
    labelMap.putIfAbsent(state, new HashSet<>());
  }

  @Override
  public void addState(State state, Set<String> labels) {
    if (labels == null) throw new NullPointerException("null labels");

    addState(state);
    labelMap.put(state, new HashSet<>(labels));
  }

  @Override
  public void addLabel(State state, String label) {
    if (state == null) throw new NullPointerException("null state");
    if (label == null) throw new NullPointerException("null label");
    if (!getStates().contains(state)) throw new IllegalArgumentException("state not in LTS");

    labelMap.get(state).add(label);
  }

  @Override
  public void addLabels(State state, Set<String> labels) {
    if (state == null) throw new NullPointerException("null state");
    if (labels == null) throw new NullPointerException("null labels");
    if (!getStates().contains(state)) throw new IllegalArgumentException("state not in LTS");

    labelMap.get(state).addAll(labels);
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
  public Set<String> getLabels(State state) {
    if (!containsState(state)) throw new IllegalArgumentException("state not in LTS");
    return labelMap.get(state);
  }

  @Override
  public Set<Action> getActions() {
    return actions;
  }

  @Override
  public Set<Action> getActions(State state) {
    if (!containsState(state)) throw new IllegalArgumentException("state not in LTS");
    return map.get(state).keySet();
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

  @Override
  public Optional<Set<State>> targets(Set<State> sourceStates, Action action, boolean stronglyExecutable) {
    Set<State> targets = new HashSet<>();
    Set<State> targetStates;

    for (State source : sourceStates) {
      targetStates = targets(source, action);
      if (stronglyExecutable && targetStates.isEmpty()) return Optional.empty();
      targets.addAll(targetStates);
    }

    return Optional.of(targets);
  }

  @Override
  public String toString(State state) {
    return state.toString() + "[" + String.join(", ", getLabels(state)) + "]";
  }
}
