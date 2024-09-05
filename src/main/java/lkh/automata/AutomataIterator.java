package lkh.automata;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class AutomataIterator<State, Symbol> implements Iterator<List<Symbol>> {
  private final AbstractAutomaton<State, Symbol> automaton;
  private final int limit;
  private final Queue<StateDescriptor<State, Symbol>> queue;

  public AutomataIterator(AbstractAutomaton<State, Symbol> automaton, int limit) {
    this.automaton = automaton;
    this.limit = limit;
    queue = new LinkedList<>();
    queue.add(new StateDescriptor<>(new LinkedList<>(), automaton.getInitialState()));
  }

  @Override
  public boolean hasNext() {
    return findAccepted();
  }

  @Override
  public List<Symbol> next() {
    findAccepted();
    StateDescriptor<State, Symbol> state = queue.poll();
    if (state == null) return null;

    advance(state);
    return state.string;
  }

  @Override
  public void forEachRemaining(Consumer<? super List<Symbol>> action) {
    Iterator.super.forEachRemaining(action);
  }

  private boolean findAccepted() {
    while (!queue.isEmpty()) {
      if (automaton.isFinal(queue.peek().state)) {
        return true;
      }

      StateDescriptor<State, Symbol> state = queue.remove();
      advance(state);
    }

    return false;
  }

  private void advance(StateDescriptor<State, Symbol> state) {
    if (state.string.size() == limit) return;

    for (var transition : automaton.outgoingTransitions(state.state)) {
      List<Symbol> newString = new LinkedList<>(state.string);
      newString.add(transition.key());
      queue.add(new StateDescriptor<>(newString, transition.value()));
    }
  }

  private record StateDescriptor<State, Symbol> (List<Symbol> string, State state) {}
}