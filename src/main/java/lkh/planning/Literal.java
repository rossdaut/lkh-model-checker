package lkh.planning;

import java.util.Objects;

public record Literal(Fluent fluent, boolean positive) {
  public Literal {
    Objects.requireNonNull(fluent, "fluent");
  }

  public static Literal positive(Fluent fluent) {
    return new Literal(fluent, true);
  }

  public static Literal negative(Fluent fluent) {
    return new Literal(fluent, false);
  }

  public Literal negate() {
    return new Literal(fluent, !positive);
  }
}
