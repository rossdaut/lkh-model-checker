package lkh.lts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.Set;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class State {
  Set<Proposition> propositions;

  /**
   * Return whether the given propositions hold
   * @param propositions a collection of propositions
   * @return true iff all propositions hold
   */
  boolean satisfiesAll(Collection<Proposition> propositions) {
    return this.propositions.containsAll(propositions);
  }
}
