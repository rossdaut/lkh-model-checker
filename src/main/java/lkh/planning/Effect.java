package lkh.planning;

import java.util.Collection;

public interface Effect {
  Collection<Fluent> getPositiveFluents();

  Collection<Fluent> getNegativeFluents();
}
