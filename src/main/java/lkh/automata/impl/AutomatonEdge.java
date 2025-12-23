package lkh.automata.impl;

import lkh.graph.edge.Edge;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Represents a transition edge in an automaton.
 * The edge connects two states and is labeled with a symbol.
 *
 * @param <State> The type of the states in the automaton
 * @param <Symbol> The type of the symbols in the automaton (can be null for epsilon transitions)
 */
@EqualsAndHashCode
@ToString
@Getter
public class AutomatonEdge<State, Symbol> implements Edge<State> {
  private final State source;
  private final State target;
  private final Symbol symbol;

  public AutomatonEdge(State source, State target, Symbol symbol) {
    this.source = source;
    this.target = target;
    this.symbol = symbol;
  }

  public boolean hasSymbol(Symbol symbol) {
    if (this.symbol == null && symbol == null) return true;
    if (this.symbol == null || symbol == null) return false;
    return this.symbol.equals(symbol);
  }
}

