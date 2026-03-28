package lkh.planning;

import java.util.List;

public interface Problem {
  List<? extends Fluent> getFluents();

  List<? extends Action> getActions();

  State getInitialState();

  Condition getGoalCondition();
}
