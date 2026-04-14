package lkh.planning;

import java.util.Collection;
import java.util.List;

public interface Problem {
  List<? extends Fluent> getFluents();

  List<? extends Action> getActions();

  default Collection<? extends Action> getApplicableActions(State state) {
    return getActions().stream()
        .filter(action -> action.isApplicable(state))
        .toList();
  }

  State getInitialState();

  Condition getGoalCondition();
}
