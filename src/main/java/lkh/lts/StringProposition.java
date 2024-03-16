package lkh.lts;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@AllArgsConstructor
@Getter
public class StringProposition implements Proposition {
  String name;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StringProposition that = (StringProposition) o;
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
