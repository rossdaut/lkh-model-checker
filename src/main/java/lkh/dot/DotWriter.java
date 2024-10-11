package lkh.dot;

import lkh.automata.NonDeterministicAutomaton;
import lkh.lts.LTS;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class DotWriter {
  public static <State, Symbol> void writeNFA(NonDeterministicAutomaton<State, Symbol> automaton, String filename) {
    PrintWriter writer;
    try {
      writer = new PrintWriter(filename);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    writer.println("digraph {");

    writer.println("init_ [shape=\"point\"];");
    writer.printf("init_ -> %s;\n", automaton.getInitialState());
    for (State state : automaton.getFinalStates()) {
      writer.printf("%s [shape=\"doublecircle\"];\n", state);
    }

    for (State source : automaton.getStates()) {
      for (Symbol symbol : automaton.getAlphabet()) {
        for (State target : automaton.delta(source, symbol)) {
          writer.printf("%s -> %s [label=\"%s\"];\n",
              source,
              target,
              symbol.toString()
          );
        }
      }
      for (State target : automaton.emptyDelta(source)) {
        writer.printf("%s -> %s\n",
                source,
                target
        );
      }
    }
    writer.println("}");

    writer.close();
  }

  public static <State, Action> void writeLTS(LTS<State, Action> lts , String filename) {
    PrintWriter writer;
    try {
      writer = new PrintWriter(filename);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    writer.println("digraph {");

    for (State state : lts.getStates()) {
      writer.printf("%s [label = \"%s\"];\n", state, String.join(", ", lts.getLabels(state)));
    }

    for (State source : lts.getStates()) {
      for (Action action : lts.getActions()) {
        for (State target : lts.targets(source, action)) {
          writer.printf("%s -> %s [label=\"%s\"];\n",
              source,
              target,
              action.toString()
          );
        }
      }
    }

    writer.println("}");
    writer.close();
  }
}
