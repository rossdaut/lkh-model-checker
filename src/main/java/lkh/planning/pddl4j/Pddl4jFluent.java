package lkh.planning.pddl4j;

import java.util.Objects;
import lkh.planning.Fluent;

final class Pddl4jFluent implements Fluent {
  private final String name;
  private final int index;

  Pddl4jFluent(String name, int index) {
    this.name = name;
    this.index = index;
  }

  int index() {
    return index;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Pddl4jFluent other)) {
      return false;
    }
    return name.equals(other.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
