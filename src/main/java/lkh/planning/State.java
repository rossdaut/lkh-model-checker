package lkh.planning;

import java.util.Collection;

public interface State {
  Collection<Fluent> getFluents();

  State copy();

  void apply(Action action);
}
