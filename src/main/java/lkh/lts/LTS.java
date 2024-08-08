package lkh.lts;

import java.util.Set;

public interface LTS<State, Action> {
  /**
   * Add a state to this LTS.
   * @param state a non-null state
   * @throws NullPointerException if the state is null
   */
  void addState(State state);

  /**
   * Add a transition. If the states don't exist, add them too.
   * @param source a non-null source state
   * @param action a non-null action
   * @param target a non-null target state
   * @throws NullPointerException if any of the arguments is null
   */
  void addTransition(State source, State target, Action action);

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
}
