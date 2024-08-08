package lkh.lts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class HashMapLTS implements LTS {
  private final HashMap<State, Set<Transition>> map = new HashMap<>();
  private final Set<Action> actions = new HashSet<>();

  @Override
  public void addState(State state) {
    if (state == null) throw new NullPointerException("null state");

    map.putIfAbsent(state, new HashSet<>());
  }

  @Override
  public void addTransition(State fromState, Action action, State toState) {
    if (action == null) throw new NullPointerException("null action");

    addState(fromState);
    addState(toState);

    map.get(fromState).add(new Transition(action, toState));
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

    return map.get(from).stream()
        .filter(t -> t.action.equals(action))   // find collection of transitions
        .map(Transition::getState)              // get collection of states
        .collect(Collectors.toSet());
  }

  @Override
  public Set<State> evaluate(Collection<Proposition> propositions) {
    return getStates().stream()
        .filter(s -> s.satisfiesAll(propositions))
        .collect(Collectors.toSet());
  }

  @Data
  @AllArgsConstructor
  @EqualsAndHashCode
  private static class Transition {
    Action action;
    State state;
  }
}
