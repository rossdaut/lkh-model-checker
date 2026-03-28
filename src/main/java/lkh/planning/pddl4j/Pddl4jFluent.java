package lkh.planning.pddl4j;

import java.util.Objects;
import lkh.planning.Fluent;

final class Pddl4jFluent implements Fluent {
  private final String name;

  Pddl4jFluent(String name) {
    this.name = name;
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
