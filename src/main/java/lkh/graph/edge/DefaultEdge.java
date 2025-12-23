package lkh.graph.edge;

import lombok.*;

@Data
public class DefaultEdge<V> implements Edge<V> {
  private V source;
  private V target;

  public DefaultEdge(V source, V target) {
    this.source = source;
    this.target = target;
  }
}
