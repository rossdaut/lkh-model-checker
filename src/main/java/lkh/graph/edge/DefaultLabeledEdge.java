package lkh.graph.edge;

import lombok.*;

@EqualsAndHashCode(callSuper = false)
@ToString
@Getter
@Setter
public class DefaultLabeledEdge<V> extends DefaultEdge<V> implements Labeled {
  private String label;

  public DefaultLabeledEdge(V source, V target, String label) {
    super(source, target);
    this.label = label;
  }
}
