package lkh.lts;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class HashMapLTSTest {

  private HashMapLTS<String, String> lts;

  @BeforeEach
  public void setUp() {
    lts = new HashMapLTS<>();
  }

  @Test
  public void testAddState() {
    // Test adding a state to the LTS
    String state = "State1";
    lts.addState(state);
    Set<String> states = lts.getStates();

    assertTrue(states.contains(state), "State should be added to the LTS.");
  }

  @Test
  public void testAddStateNullState() {
    // Test adding a null state (should throw NullPointerException)
    assertThrows(NullPointerException.class, () -> lts.addState(null),
        "Adding a null state should throw NullPointerException.");
  }

  @Test
  public void testAddDuplicateState() {
    // Test adding a duplicate state (should not affect the LTS)
    String state = "State2";
    lts.addState(state);
    lts.addState(state); // Adding the same state again

    Set<String> states = lts.getStates();
    assertEquals(1, states.size(), "There should only be one instance of the state in the LTS.");
    assertTrue(states.contains(state), "State should be present in the LTS.");
  }

  @Test
  public void testAddStateAndCheckPresence() {
    // Test adding multiple states and ensure they are all present
    String state1 = "State1";
    String state2 = "State2";

    lts.addState(state1);
    lts.addState(state2);

    Set<String> states = lts.getStates();
    assertEquals(states, new HashSet<>(Arrays.asList(state1, state2)),
        "State 1 and State 2 should be present in the LTS.");
  }

  @Test
  public void testAddTransition() {
    String source = "State1";
    String target = "State2";
    String action = "Action1";

    lts.addTransition(source, target, action);

    Set<String> targets = lts.targets(source, action);
    assertTrue(targets.contains(target), "Target state should be added for the given action.");
  }

  @Test
  public void testAddTransitionNullAction() {
    String source = "State1";
    String target = "State2";

    // Expecting NullPointerException for null action
    assertThrows(NullPointerException.class, () -> lts.addTransition(source, target, null),
        "Adding a transition with a null action should throw NullPointerException.");
  }

  @Test
  public void testAddTransitionToNonExistentState() {
    String source = "State1";
    String target = "State2";
    String action = "Action1";

    lts.addTransition(source, target, action);

    assertTrue(lts.getStates().contains(source), "Source state should be present in the LTS.");
    assertTrue(lts.getStates().contains(target), "Target state should be present in the LTS.");
  }

  @Test
  public void testAddDuplicateTransition() {
    String source = "State1";
    String target = "State2";
    String action = "Action1";

    lts.addTransition(source, target, action);
    lts.addTransition(source, target, action); // Adding the same transition again

    Set<String> targets = lts.targets(source, action);
    assertEquals(1, targets.size(), "There should be only one instance of the target state for the given action.");
    assertTrue(targets.contains(target), "Target state should be present for the given action.");
  }

  @Test
  public void testTransitionWithDifferentActions() {
    String source = "State1";
    String target1 = "State2";
    String target2 = "State3";
    String action1 = "Action1";
    String action2 = "Action2";

    lts.addTransition(source, target1, action1);
    lts.addTransition(source, target2, action2);

    Set<String> targetsAction1 = lts.targets(source, action1);
    Set<String> targetsAction2 = lts.targets(source, action2);

    assertEquals(targetsAction1, Collections.singleton(target1), "Target1 only should be reachable via Action1.");
    assertEquals(targetsAction2, Collections.singleton(target2), "Target2 only should be reachable via Action2.");
  }

  @Test
  public void testTransitionWithUnaddedStates() {
    String source = "State1";
    String target = "State2";
    String action = "Action1";

    // Adding transition with states that have not been added explicitly
    lts.addTransition(source, target, action);

    assertTrue(lts.getStates().contains(source),
        "Source state should be added for the given action.");
    assertTrue(lts.getStates().contains(target),
        "Target state should be added for the given action.");
  }
}