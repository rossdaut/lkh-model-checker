package lkh.lts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class State {
  HashSet<Proposition> propositions;
}
