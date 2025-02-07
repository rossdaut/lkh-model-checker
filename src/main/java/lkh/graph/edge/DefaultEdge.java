package lkh.graph.edge;

import lombok.Data;

@Data
public class DefaultEdge<V> implements Edge<V> {
  private V source;
  private V target;

  public DefaultEdge() {
  }

  public DefaultEdge(V a, V b) {
    source = a;
    target = b;
  }
}
