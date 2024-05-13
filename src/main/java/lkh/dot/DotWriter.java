package lkh.dot;

import lkh.automata.NonDeterministicAutomaton;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class DotWriter {
  public static <State, Symbol> void writeNFA(NonDeterministicAutomaton<State, Symbol> automaton, String filename) {
    PrintWriter writer;
    try {
      writer = new PrintWriter(filename);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    writer.println("digraph {");

    Queue<State> queue = new LinkedList<>();
    queue.add(automaton.getInitialState());

    Set<State> visited = new HashSet<>();
    //ArrayList<State> indexMap = new ArrayList<>(automaton.getStates());
    State currentState;

    while (!queue.isEmpty()) {
      currentState = queue.poll();
      if (visited.contains(currentState)) continue;
      visited.add(currentState);

      for (Symbol symbol : automaton.getAlphabet()) {
        for (State target : automaton.delta(currentState, symbol)) {
          writer.printf("%s -> %s [label=\"%s\"];\n",
              currentState,
              target,
              symbol.toString());
        }
      }
      for (State target : automaton.emptyDelta(currentState)) {
        writer.printf("%s -> %s\n",
            currentState,
            target);
      }

      writer.println("}");

      writer.close();
    }
  }
}
