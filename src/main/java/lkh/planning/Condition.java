package lkh.planning;

import java.util.Collection;

public interface Condition {
  Collection<Fluent> getPositiveFluents();

  Collection<Fluent> getNegativeFluents();
}
