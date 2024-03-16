package lkh.automata;

import lkh.lts.Action;
import lkh.lts.LTS;
import lkh.lts.State;
import lombok.AllArgsConstructor;

import java.util.Set;

@AllArgsConstructor
public class LTSAutomaton implements Automaton {
  private LTS lts;
  private Set<State> initialStates;
  private Set<State> finalStates;

  @Override
  public Set<State> delta(State from, Action action) {
    return lts.destinations(from, action);
  }
}
