package lkh.dot;

import lkh.automata.impl.GraphDeterministicAutomaton;
import lkh.automata.impl.GraphNonDeterministicAutomaton;
import lkh.expression.Expression;
import lkh.lts.LTS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DotWriter {
  public static <State, Symbol> void writeNFA(GraphNonDeterministicAutomaton<State, Symbol> automaton, String filename) {
    PrintWriter writer;
    try {
      writer = new PrintWriter(filename);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    List<String> stmts = new ArrayList<>();
    stmts.add("init_ [shape=\"point\"]");
    stmts.add(String.format("init_ -> %s", automaton.getInitialState()));
    for (State state : automaton.getFinalStates()) {
      stmts.add(String.format("%s [shape=\"doublecircle\"]", state));
    }
    for (State source : automaton.getStates()) {
      for (Symbol symbol : automaton.getAlphabet()) {
        for (State target : automaton.delta(source, symbol)) {
          stmts.add(String.format("%s -> %s [label=\"%s\"]", source, target, symbol));
        }
      }
      for (State target : automaton.emptyDelta(source)) {
        stmts.add(String.format("%s -> %s", source, target));
      }
    }

    writer.println("digraph {");
    for (int i = 0; i < stmts.size(); i++) {
      if (i < stmts.size() - 1) {
        writer.println("    " + stmts.get(i) + ";");
      } else {
        writer.println("    " + stmts.get(i));
      }
    }
    writer.println("}");
    writer.flush();
    writer.close();
  }

  public static <State, Symbol> void writeDFA(GraphDeterministicAutomaton<State, Symbol> automaton, String filename) {
    PrintWriter writer;
    try {
      writer = new PrintWriter(filename);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    List<String> stmts = new ArrayList<>();
    stmts.add("init_ [shape=\"point\"]");
    stmts.add(String.format("init_ -> %s", automaton.getInitialState()));
    for (State state : automaton.getFinalStates()) {
      stmts.add(String.format("%s [shape=\"doublecircle\"]", state));
    }
    for (State source : automaton.getStates()) {
      for (Symbol symbol : automaton.getAlphabet()) {
        State target = automaton.delta(source, symbol).orElse(null);
        if (target != null) {
          stmts.add(String.format("%s -> %s [label=\"%s\"]", source, target, symbol));
        }
      }
    }

    writer.println("digraph {");
    for (int i = 0; i < stmts.size(); i++) {
      if (i < stmts.size() - 1) {
        writer.println("    " + stmts.get(i) + ";");
      } else {
        writer.println("    " + stmts.get(i));
      }
    }
    writer.println("}");

    writer.flush();
    writer.close();
  }

  public static <State, Action> void writeLTS(LTS<State, Action> lts, String filename) {
    PrintWriter writer;
    try {
      writer = new PrintWriter(filename);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }

    List<String> stmts = new ArrayList<>();

    for (State state : lts.getStates()) {
      stmts.add(String.format("%s [label = \"%s\"]", state, String.join(", ", lts.getLabels(state))));
    }
    for (State source : lts.getStates()) {
      for (Action action : lts.getActions()) {
        for (State target : lts.targets(source, action)) {
          stmts.add(String.format("%s -> %s [label = \"%s\"]", source, target, action));
        }
      }
    }

    writer.println("digraph {");
    for (int i = 0; i < stmts.size(); i++) {
      if (i < stmts.size() - 1) {
        writer.println("    " + stmts.get(i) + ";");
      } else {
        writer.println("    " + stmts.get(i));
      }
    }
    writer.println("}");
    writer.flush();
    writer.close();
  }
}
