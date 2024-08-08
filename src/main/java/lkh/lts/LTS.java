package lkh.lts;

import java.util.Collection;
import java.util.Set;

public interface LTS {
  void addState(State state);

  /**
   * Add a transition. If the states don't exist, add them too.
   * @param src a non-null source state
   * @param action a non-null action
   * @param dest a non-null target state
   * @throws NullPointerException if any of the arguments is null
   */
  void addTransition(State src, Action action, State dest);

  /**
   * Get the set of states of LTS
   * @return the set of states
   */
  Set<State> getStates();

  /**
   * Get the set of actions of LTS
   * @return the set of actions
   */
  Set<Action> getActions();

  /**
   * Return whether the given state exists in the LTS
   * @param state the state to check
   * @return true if the state exists in the LTS, false otherwise
   */
  boolean containsState(State state);

  /**
   * Return the set of states reachable from the given source by consuming the given action
   * @param from the source state
   * @param action the action (symbol) to consume
   * @return the set of states reachable from `from` by consuming `action`
   */
  Set<State> targets(State from, Action action);

  /**
   * Return the set of states for which the given propositions hold
   * @param propositions a collection of propositions
   * @return a set of states
   */
  Set<State> evaluate(Collection<Proposition> propositions);
}
