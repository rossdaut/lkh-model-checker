package lkh.lts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class State {
  int id;
  HashSet<String> propositions;
}
