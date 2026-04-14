package lkh.lts.builder;

import java.util.Collection;
import lkh.planning.Action;
import lkh.planning.Problem;
import lkh.planning.State;

public interface ActionSelectionStrategy {
  Collection<? extends Action> selectActions(Action previousAction, State state, Problem problem);
}
