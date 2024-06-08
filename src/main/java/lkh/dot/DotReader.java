package lkh.dot;

import lkh.automata.NonDeterministicAutomaton;
import lkh.dot.parser.ParseException;
import lkh.dot.parser.Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class DotReader {
  public static NonDeterministicAutomaton<String, String> readNFA(String filename) throws FileNotFoundException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    Parser parser = new Parser(reader);

    try {
      return parser.Graph();
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }
}
