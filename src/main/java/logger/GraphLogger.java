package logger;

public class GraphLogger implements Logger {
  private int nodes;
  private int edges;

  @Override
  public void log(String event) {
    switch (event) {
      case "add vertex" -> nodes++;
      case "add edge" -> edges++;
    }
  }

  @Override
  public void printLog() {
    System.out.println("Memory usage: " + nodes + " nodes and " + edges + " edges.");
  }

  public int getNodesCant() {
      return nodes;
  }

  public int getEdgesCant() {
      return edges;
  }


}
