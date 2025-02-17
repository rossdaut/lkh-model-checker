package lkh.lts;

import lkh.graph.DirectedGraph;
import lkh.graph.HashMapDirectedGraph;
import lombok.NonNull;

import java.util.*;
import java.util.stream.Collectors;

public class HashMapLTS<State, Action> implements LTS<State, Action> {
  private final DirectedGraph<State, LTSEdge<State, Action>> graph = new HashMapDirectedGraph<>();
  private final Set<Action> actions = new HashSet<>();
  private final Map<State, Set<String>> labelMap = new HashMap<>();

  @Override
  public void addState(State state) {
    graph.addVertex(state);
    labelMap.put(state, new HashSet<>());
  }

  @Override
  public void addState(State state, @NonNull Set<String> labels) {
    addState(state);
    labelMap.put(state, new HashSet<>(labels));
  }

  @Override
  public void addLabel(@NonNull State state, @NonNull String label) {
    if (!getStates().contains(state)) throw new IllegalArgumentException("state not in LTS");

    labelMap.get(state).add(label);
  }

  @Override
  public void addLabels(@NonNull State state, @NonNull Set<String> labels) {
    if (!getStates().contains(state)) throw new IllegalArgumentException("state not in LTS");

    labelMap.get(state).addAll(labels);
  }

  @Override
  public void addTransition(State source, State target, @NonNull Action action) {
    actions.add(action);
    graph.addEdge(new LTSEdge<>(source, target, action));
  }

  @Override
  public Set<State> getStates() {
    return graph.getVertices();
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
    return graph.getOutgoingEdges(state).stream().map(edge -> edge.getAction()).collect(Collectors.toSet());
  }

  @Override
  public boolean containsState(State state) {
    return graph.containsVertex(state);
  }

  @Override
  public Set<State> targets(State from, Action action) {
    if (!containsState(from))
      throw new IllegalArgumentException("lts doesn't contain the given state");

    return graph.getOutgoingEdges(from).stream()
        .filter(edge -> edge.getAction().equals(action))
        .map(LTSEdge::getTarget)
        .collect(Collectors.toSet());
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
