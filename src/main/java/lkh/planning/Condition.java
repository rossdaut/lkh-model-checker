package lkh.planning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface Condition {
  Collection<Fluent> getPositiveFluents();

  Collection<Fluent> getNegativeFluents();

  default Collection<Literal> getLiterals() {
    List<Literal> literals = new ArrayList<>();
    for (Fluent fluent : getPositiveFluents()) {
      literals.add(Literal.positive(fluent));
    }
    for (Fluent fluent : getNegativeFluents()) {
      literals.add(Literal.negative(fluent));
    }
    return List.copyOf(literals);
  }
}
