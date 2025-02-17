package lkh.automata;

import lkh.graph.edge.Edge;
import lombok.Data;

@Data
public class AutomatonEdge<State, Symbol> implements Edge<State> {
  private State source;
  private State target;
  private Symbol symbol;

  public AutomatonEdge(State source, State target, Symbol symbol) {
    this.source = source;
    this.target = target;
    this.symbol = symbol;
  }
}
