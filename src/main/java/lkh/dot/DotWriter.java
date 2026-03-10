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

  public static void toDotFile(String filename, Expression expression) {
    try {
      PrintWriter f = new PrintWriter(filename);
      f.print("digraph Tree{\n");
      writeSons(f, expression, 0);
      f.print("}");
      f.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }

  }

  private static void writeSons(PrintWriter f, Expression tree, int id) {
    writeLabel(f,tree, id);
    if(tree.getLeft() != null){
      int left_id = id + 1;
      f.printf("%d -> %d;\n", id, left_id);
      writeSons(f, tree.getLeft(), left_id);
    }
    if(tree.getRight() != null){
      int right_id = id + 1;
      if (tree.getLeft() != null)
        right_id += tree.getLeft().getSize();
      f.printf("%d -> %d;\n", id, right_id);
      writeSons(f, tree.getRight(), right_id);
    }
  }

  private static void writeLabel(PrintWriter f, Expression tree, int id) {
    f.printf("%d [label=\"%s\"];\n", id, tree.getName());
  }
}
