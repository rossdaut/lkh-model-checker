package lkh.automata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AutomataIteratorTest {
  private DeterministicAutomaton<Integer, Character> automaton;

  @BeforeEach
  void setUp() {
    automaton = new DeterministicAutomaton<>();

    automaton.setInitialState(0);
    automaton.addFinalState(2);
    automaton.addFinalState(3);

    automaton.addTransition(0, 1, 'a');
    automaton.addTransition(0, 2, 'b');
    automaton.addTransition(1, 3, 'c');
    automaton.addTransition(3, 1, 'a');
    automaton.addTransition(3, 2, 'b');
    automaton.addTransition(2, 1, 'a');
    automaton.addTransition(2, 2, 'b');
  }

  @Test
  void testHasNextWithAcceptedString() {
    AutomataIterator<Integer, Character> iterator = new AutomataIterator<>(automaton, 2);

    assertTrue(iterator.hasNext());
    List<Character> nextString = iterator.next();
    assertNotNull(nextString);
    assertEquals(List.of('b'), nextString);
    assertTrue(automaton.evaluate(nextString));

    assertTrue(iterator.hasNext());
    nextString = iterator.next();
    assertNotNull(nextString);
    assertEquals(List.of('a', 'c'), nextString);
    assertTrue(automaton.evaluate(nextString));

    assertTrue(iterator.hasNext());
    nextString = iterator.next();
    assertNotNull(nextString);
    assertEquals(List.of('b', 'b'), nextString);
    assertTrue(automaton.evaluate(nextString));

    assertFalse(iterator.hasNext()); // No more accepted strings within the limit
  }

  @Test
  void testNextWithNoAcceptedStringWithinLimit() {
    AutomataIterator<Integer, Character> iterator = new AutomataIterator<>(automaton, 1);

    assertTrue(iterator.hasNext());
    List<Character> nextString = iterator.next();
    assertNotNull(nextString);
    assertEquals(List.of('b'), nextString); // 'b' leads to final state 2

    assertFalse(iterator.hasNext()); // Limit of 1 prevents reaching any other final state
  }

  @Test
  void testForEachRemaining() {
    AutomataIterator<Integer, Character> iterator = new AutomataIterator<>(automaton, 3);

    Set<List<Character>> collectedStrings = new HashSet<>();
    iterator.forEachRemaining(collectedStrings::add);

    assertEquals(6, collectedStrings.size());
    Set<List<Character>> expectedStrings = Set.of(
        List.of('b'),
        List.of('a', 'c'), List.of('b', 'b'),
        List.of('a', 'c', 'b'), List.of('b', 'a', 'c'), List.of('b', 'b', 'b')
    );
    assertEquals(expectedStrings, collectedStrings);
  }

  @Test
  void testEmptyAutomaton() {
    DeterministicAutomaton<Integer, Character> emptyAutomaton = new DeterministicAutomaton<>();
    emptyAutomaton.setInitialState(0);

    AutomataIterator<Integer, Character> iterator = new AutomataIterator<>(emptyAutomaton, 2);

    assertFalse(iterator.hasNext());
    assertNull(iterator.next());
  }

  @Test
  void testExcessiveLimit() {
    DeterministicAutomaton<Integer, Character> automaton = new DeterministicAutomaton<>();
    /* 0 -a-> [1] -b-> [2] -a-> 3 ─┐
                                └──┘a/b */
    automaton.setInitialState(0);
    automaton.addFinalState(1);
    automaton.addFinalState(2);
    automaton.addState(3);

    automaton.addTransition(0, 1, 'a');
    automaton.addTransition(1, 2, 'b');
    automaton.addTransition(2, 3, 'a');
    automaton.addTransition(3, 3, 'a');
    automaton.addTransition(3, 3, 'b');

    AutomataIterator<Integer, Character> iterator = new AutomataIterator<>(automaton, 10);
    Set<List<Character>> strings = new HashSet<>();
    // iterator.forEachRemaining(strings::add);
    while (iterator.hasNext()) {
      List<Character> nextString = iterator.next();
      strings.add(nextString);
    }

    Set<List<Character>> expected = Set.of(List.of('a'), List.of('a', 'b'));

    assertEquals(expected, strings);
  }
}
