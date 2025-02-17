package lkh.lts;

import lkh.graph.edge.Edge;
import lombok.Data;

@Data
public class LTSEdge<State,Action> implements Edge<State> {
  private State source;
  private State target;
  private Action action;

  public LTSEdge(State source, State target, Action action) {
    this.source = source;
    this.target = target;
    this.action = action;
  }
}
