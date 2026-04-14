package lkh.lts.builder;

import java.util.Collection;
import lkh.planning.Action;
import lkh.planning.Problem;
import lkh.planning.State;

public class DefaultActionSelectionStrategy implements ActionSelectionStrategy {
  @Override
  public Collection<? extends Action> selectActions(Action previousAction, State state, Problem problem) {
    if (state == null) {
      throw new IllegalArgumentException("Null state");
    }
    if (problem == null) {
      throw new IllegalArgumentException("Null problem");
    }

    return problem.getActions().stream()
        .filter(action -> action.isApplicable(state))
        .toList();
  }
}
