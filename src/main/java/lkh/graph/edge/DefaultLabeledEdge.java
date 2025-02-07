package lkh.graph.edge;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class DefaultLabeledEdge<V> extends DefaultEdge<V> implements Labeled {
  private String label;

  @Override
  public String getLabel() {
    return label;
  }
}
