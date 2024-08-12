package lkh.lts;

import java.util.Optional;
import java.util.Set;

public interface LTS<State, Action> {
  /**
   * Add a state without labels to this LTS.
   * @param state a non-null state
   * @throws NullPointerException if the state is null
   */
  void addState(State state);

  /**
   * Add a states with labels to this LTS.
   * @param state a non-null state
   * @param labels a non-null set of labels
   */
  void addState(State state, Set<String> labels);

  /**
   * Add a label to the given state
   * @param state the state
   * @param label the label to add
   */
  void addLabel(State state, String label);

  /**
   * Add all the given labels to the state
   * @param state the state that will have the new labels
   * @param labels the set of labels to add
   */
  void addLabels(State state, Set<String> labels);

  /**
   * Add a transition. If the states don't exist, add them without labels.
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
   * Get the set of labels of the given state
   * @param state the state
   * @return the set of labels that hold in the given state
   */
  Set<String> getLabels(State state);

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
   * @param source the source state
   * @param action the action (symbol) to consume
   * @return the set of states reachable from `source` by consuming `action`
   */
  Set<State> targets(State source, Action action);

  /**
   * Return the set of states reachable from any of the given source states by consuming the given action
   * @param sourceStates the source states
   * @param action the action (symbol) to consume
   * @return the set of states reachable from `sourceStates` by consuming `action`
   *       or empty if any of the source states has no transitions for the given action
   */
  Optional<Set<State>> targets(Set<State> sourceStates, Action action, boolean stronglyExecutable);
}
