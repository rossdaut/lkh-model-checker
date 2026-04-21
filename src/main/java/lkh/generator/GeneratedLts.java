package lkh.generator;

import lkh.lts.HashMapLTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record GeneratedLts(
    LtsGeneratorConfig config,
    HashMapLTS<Integer, String> lts,
    List<List<String>> implantedWitnesses,
    Set<Integer> initialStates,
    Set<Integer> goalStates
) {
  public GeneratedLts {
    if (config == null) {
      throw new IllegalArgumentException("config must not be null");
    }
    if (lts == null) {
      throw new IllegalArgumentException("lts must not be null");
    }
    if (implantedWitnesses == null) {
      throw new IllegalArgumentException("implantedWitnesses must not be null");
    }
    if (initialStates == null) {
      throw new IllegalArgumentException("initialStates must not be null");
    }
    if (goalStates == null) {
      throw new IllegalArgumentException("goalStates must not be null");
    }
  }

  public String report() {
    List<String> lines = new ArrayList<>();
    lines.add("Generation report");
    lines.add("States: " + lts.getSize().key() + " (requested >= " + config.minNodeCount() + ")");
    lines.add("Edges: " + lts.getSize().value() + " (requested >= " + config.minEdgeCount() + ")");
    lines.add("Initial states: " + initialStates.size());
    lines.add("Goal states: " + goalStates.size() + " (requested >= " + config.goalStateCount() + ")");
    lines.add("Witnesses: " + implantedWitnesses.size() + " " + witnessLengthSummary());

    for (String warning : warningMessages()) {
      lines.add("Warning: " + warning);
    }

    return String.join(System.lineSeparator(), lines);
  }

  private String witnessLengthSummary() {
    int min = implantedWitnesses.stream().mapToInt(List::size).min().orElse(0);
    int max = implantedWitnesses.stream().mapToInt(List::size).max().orElse(0);
    return min == max ? "(length " + min + ")" : "(lengths " + min + "-" + max + ")";
  }

  private List<String> warningMessages() {
    List<String> warnings = new ArrayList<>();

    if (goalStates.size() > config.goalStateCount()) {
      warnings.add(
          "generated "
              + goalStates.size()
              + " goal states, above the requested minimum of "
              + config.goalStateCount()
      );
    }

    return warnings;
  }
}
