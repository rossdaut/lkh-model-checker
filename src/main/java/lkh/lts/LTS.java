package lkh.lts;

public interface LTS {
  boolean addState(State state);

  boolean addTransition(State src, Action action, State dest);
}
