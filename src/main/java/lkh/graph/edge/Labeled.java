package lkh.graph.edge;

/**
 * This interface represents a labeled entity by providing a method
 * to retrieve its associated label.
 *
 * Classes implementing this interface are expected to return a label
 * that serves as an identifying or descriptive name.
 */
public interface Labeled {
  /**
   * Retrieves the label associated with this entity.
   *
   * @return The label as a string, serving as an identifier or description.
   */
  public String getLabel();
}
