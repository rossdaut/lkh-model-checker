package logger;

import lkh.utils.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GraphLogger implements Logger {
  private final String name;
  private int totalNodes;
  private int totalEdges;

  private int nodes;
  private int edges;

  public GraphLogger(String name) {
    this.name = name;
  }

  @Override
  public void log(String event) {
    switch (event) {
      case "add vertex" -> totalNodes++;
      case "add edge" -> totalEdges++;
    }
  }

  @Override
  public void printLog() {
    System.out.println("=== " + name + " ===");
    System.out.println("During generation: " + totalNodes + " nodes and " + totalEdges + " edges.");
    System.out.println("Size: " + nodes + " nodes and " + edges + " edges.");
  }

  public void writeCSV(String filename) {
    try {
      File file = new File(filename);
      boolean isNewFile = !file.exists();

      if (isNewFile) {
        file.createNewFile();
      }

      writeCSV(file, isNewFile);
    } catch (IOException e) {
      System.err.println("Error al escribir el archivo CSV: " + e.getMessage());
    }
  }

  private void writeCSV(File file, boolean isNewFile) throws IOException {
    try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
      if (isNewFile) {
        writer.println("TotalNodes,TotalEdges,Nodes,Edges");
      }
      writer.println(totalNodes + "," + totalEdges + "," + nodes + "," + edges);
    }
  }

  public int getNodesCant() {
      return nodes;
  }

  public int getEdgesCant() {
      return edges;
  }

  public void setSize(Pair<Integer, Integer> size) {
      this.nodes = size.key();
      this.edges = size.value();
  }

}
