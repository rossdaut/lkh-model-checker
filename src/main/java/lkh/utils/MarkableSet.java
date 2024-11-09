package lkh.utils;

import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * A set that can mark elements
 * @param <T> the type of the elements
 */
@EqualsAndHashCode(callSuper = false)
public class MarkableSet<T> implements Iterable<T>{
  // A map to store elements and their marked state
  private HashMap<T, Boolean> elements;

  public MarkableSet() {
    elements = new HashMap<>();
  }

  public MarkableSet(Collection<T> elements) {
    this();
    for (T element : elements) {
      add(element);
    }
  }

  public void add(T element) {
    elements.put(element, false);
  }

  public void mark(T element) {
    if (elements.containsKey(element)) {
      elements.put(element, true);
    }
  }

  public void unmark(T element) {
    if (elements.containsKey(element)) {
      elements.put(element, false);
    }
  }

  public boolean isMarked(T element) {
    return elements.getOrDefault(element, false);
  }

  public Set<T> getMarkedElements() {
    Set<T> markedElements = new HashSet<>();
    for (T element : elements.keySet()) {
      if (elements.get(element)) {
        markedElements.add(element);
      }
    }
    return markedElements;
  }

  public Set<T> getUnmarkedElements() {
    Set<T> unmarkedElements = new HashSet<>();
    for (T element : elements.keySet()) {
      if (!elements.get(element)) {
        unmarkedElements.add(element);
      }
    }
    return unmarkedElements;
  }

  public void remove(T element) {
    elements.remove(element);
  }

  public boolean contains(T state) {
    return elements.containsKey(state);
  }

  public HashSet<T> getElements() {
    return new HashSet<>(elements.keySet());
  }

  @Override
  public Iterator<T> iterator() {
    return elements.keySet().iterator();
  }
}
