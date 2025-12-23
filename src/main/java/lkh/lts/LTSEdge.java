package lkh.lts;

import lkh.graph.edge.Edge;
import lombok.Data;

/**
 * Represents an edge in a Labeled Transition System (LTS).
 * Each edge connects a source state to a target state and is labeled with an action.
 * This class implements the Edge interface to enable integration with the graph framework.
 *
 * @param <State> The type representing states in the LTS
 * @param <Action> The type representing actions that label transitions
 */
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
