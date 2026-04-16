package lkh.planning;

import java.util.Collection;

public interface State {
  Collection<Fluent> getFluents();

  default boolean holds(Literal literal) {
    boolean present = getFluents().contains(literal.fluent());
    return literal.positive() ? present : !present;
  }

  State copy();

  void apply(Action action);
}
