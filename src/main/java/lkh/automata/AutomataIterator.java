package lkh.automata;

import java.util.*;
import java.util.function.Consumer;

public class AutomataIterator<State, Symbol> implements Iterator<List<Symbol>> {
  private final AbstractAutomaton<State, Symbol> automaton;
  private final int limit;
  private final Set<State> visited = new HashSet<>();
  private final Queue<StateDescriptor<State, Symbol>> queue;

  public AutomataIterator(AbstractAutomaton<State, Symbol> automaton, int limit) {
    this.automaton = automaton;
    this.limit = limit;
    queue = new LinkedList<>();
    queue.add(new StateDescriptor<>(new LinkedList<>(), automaton.getInitialState()));
  }

  /**
   * Check if there is another string accepted by the automaton with a length shorter than the limit.
   * @return true if there is another accepted string, false otherwise
   */
  @Override
  public boolean hasNext() {
    return findAccepted();
  }

  /**
   * Get the next string accepted by the automaton with a length shorter than the limit.
   * @return the next accepted string, null if non exists
   */
  @Override
  public List<Symbol> next() {
    findAccepted();
    StateDescriptor<State, Symbol> state = queue.poll();
    if (state == null) return null;

    advance(state);
    return state.string;
  }

  /**
   * Perform the given action for each element of the remaining elements.
   * @param action The action to be performed for each element
   */
  @Override
  public void forEachRemaining(Consumer<? super List<Symbol>> action) {
    Iterator.super.forEachRemaining(action);
  }

  /**
   * Find the next accepted string by the automaton.
   * @return true if an accepted string is found, false otherwise
   */
  private boolean findAccepted() {
    while (!queue.isEmpty()) {
      if (automaton.isFinal(queue.peek().state)) {
        return true;
      }

      StateDescriptor<State, Symbol> state = queue.remove();
      visited.add(state.state);
      advance(state);
    }

    return false;
  }

  /**
   * Advance the automaton to the next string by enqueuing all possible transitions from the current state.
   * @param state the current state
   */
  private void advance(StateDescriptor<State, Symbol> state) {
    if (state.string.size() == limit) return;

    for (var transition : automaton.outgoingTransitions(state.state)) {
      List<Symbol> newString = new LinkedList<>(state.string);
      newString.add(transition.key());

      if (visited.contains(transition.value())) continue;

      queue.add(new StateDescriptor<>(newString, transition.value()));
    }
  }

  /**
   * A record to hold the current state and the string that led to it.
   * @param string the string that led to the current state
   * @param state the state
   * @param <State> the type of the state
   * @param <Symbol> the type of the symbols in the automaton
   */
  private record StateDescriptor<State, Symbol> (List<Symbol> string, State state) {}
}