package lkh.lts;

import java.util.Collection;
import java.util.Set;

public interface LTS {
  void addState(State state);

  void addTransition(State src, Action action, State dest);

  Set<State> getStates();

  /**
   * Return the set of states reachable from the given source by consuming the given action
   * @param from the source state
   * @param action the action (symbol) to consume
   * @return the set of states reachable from `from` by consuming `action`
   */
  Set<State> destinations(State from, Action action);

  /**
   * Return the set of states for which the given propositions hold
   * @param propositions a collection of propositions
   * @return a set of states
   */
  Set<State> evaluate(Collection<Proposition> propositions);

  boolean containsState(State state);
}
