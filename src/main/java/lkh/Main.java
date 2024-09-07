package lkh;

import lkh.dot.DotReader;
import lkh.lts.LTS;
import lkh.lts.parser.ParseException;

import java.io.FileNotFoundException;

public class Main {
  public static void main(String[] args) {
    LTS<String, String> lts;

    if (args.length != 1) {
      System.out.println("Usage: Main <lts-file>");
      System.exit(1);
    }

    String ltsFilename = args[0];
    try {
      lts = DotReader.readLTS(ltsFilename);

      System.out.println();
    } catch (FileNotFoundException | ParseException e) {
      throw new RuntimeException(e);
    }
  }
}
