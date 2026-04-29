package lkh.planning.pddl4j;

import fr.uga.pddl4j.util.BitVector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import lkh.planning.Fluent;

final class Pddl4jFluentRegistry {
  private final List<Pddl4jFluent> byIndex;
  private final Map<String, Pddl4jFluent> byName;

  Pddl4jFluentRegistry(fr.uga.pddl4j.problem.Problem problem) {
    this.byIndex = new ArrayList<>();
    this.byName = new HashMap<>();
    int index = 0;
    for (fr.uga.pddl4j.problem.Fluent fluent : problem.getFluents()) {
      Pddl4jFluent wrapped = new Pddl4jFluent(render(problem, fluent), index++);
      byIndex.add(wrapped);
      byName.put(wrapped.toString(), wrapped);
    }
  }

  List<? extends Fluent> all() {
    return Collections.unmodifiableList(byIndex);
  }

  Pddl4jFluent resolve(String name) {
    Pddl4jFluent fluent = byName.get(name);
    if (fluent == null) {
      throw new IllegalArgumentException("Unknown fluent: " + name);
    }
    return fluent;
  }

  Collection<Fluent> wrap(BitVector bits) {
    LinkedHashSet<Fluent> wrapped = new LinkedHashSet<>();
    bits.stream().forEach(index -> wrapped.add(byIndex.get(index)));
    return Collections.unmodifiableCollection(wrapped);
  }

  Collection<Fluent> wrap(fr.uga.pddl4j.problem.State state) {
    LinkedHashSet<Fluent> wrapped = new LinkedHashSet<>();
    state.stream().forEach(index -> wrapped.add(byIndex.get(index)));
    return Collections.unmodifiableCollection(wrapped);
  }

  private static String render(fr.uga.pddl4j.problem.Problem problem, fr.uga.pddl4j.problem.Fluent fluent) {
    StringBuilder rendered = new StringBuilder(problem.getPredicateSymbols().get(fluent.getSymbol()));
    List<String> arguments = new ArrayList<>();
    for (int argument : fluent.getArguments()) {
      arguments.add(problem.getConstantSymbols().get(argument));
    }
    if (!arguments.isEmpty()) {
      rendered.append("(").append(String.join(", ", arguments)).append(")");
    }
    return rendered.toString();
  }
}
