package lkh.planning;

public interface Action {
  String getName();

  Condition getPrecondition();

  Effect getEffects();

  boolean isApplicable(State state);
}
