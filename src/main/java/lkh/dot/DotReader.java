package lkh.dot;

import lkh.automata.AutomataOperations;
import lkh.automata.DeterministicAutomaton;
import lkh.automata.NonDeterministicAutomaton;
import lkh.dot.parser.ParseException;
import lkh.dot.parser.Parser;
import lkh.lts.HashMapLTS;
import lkh.lts.parser.LTSParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class DotReader {
  public static NonDeterministicAutomaton<String, String> readNFA(String filename) throws FileNotFoundException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    try {
      Parser.ReInit(reader);
      return Parser.Graph();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static DeterministicAutomaton<String, String> readDFA(String filename) throws FileNotFoundException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    try {
      Parser.ReInit(reader);
      return AutomataOperations.asDeterministic(Parser.Graph());
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public static HashMapLTS<String, String> readLTS(String filename) throws FileNotFoundException, lkh.lts.parser.ParseException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));

    LTSParser parser = new LTSParser(reader);
    return parser.LTS();
  }
}
