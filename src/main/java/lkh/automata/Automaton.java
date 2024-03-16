package lkh.automata;

import lkh.lts.Action;
import lkh.lts.State;
import java.util.Set;

public interface Automaton {
  Set<State> delta(State from, Action action);
}
