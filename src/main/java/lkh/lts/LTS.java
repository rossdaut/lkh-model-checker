package lkh.lts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class LTS {
  private final HashMap<State, Set<Transition>> map = new HashMap<>();

  public boolean addState(State state) {
    if (state == null) throw new NullPointerException("null state");
    if (map.containsKey(state)) return false;

    map.put(state, new HashSet<>());
    return true;
  }

  public boolean addTransition(State fromState, Action action, State toState) {
    if (action == null) throw new NullPointerException("null action");

    addState(fromState);
    addState(toState);

    return map.get(fromState).add(new Transition(action, toState));
  }

  @Data
  @AllArgsConstructor
  @EqualsAndHashCode
  private static class Transition {
    Action action;
    State state;
  }
}
