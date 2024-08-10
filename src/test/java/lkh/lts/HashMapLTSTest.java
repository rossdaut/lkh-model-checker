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

  //addState()

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

  //addTransition()

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

  // getStates()

  @Test
  void testGetStatesInitiallyEmpty() {
    // Test that the LTS is empty initially
    Set<String> states = lts.getStates();
    assertTrue(states.isEmpty(), "Expected no states initially");
  }

  @Test
  void testGetStatesAfterAddingStates() {
    // Add a few states
    lts.addState("A");
    lts.addState("B");
    lts.addState("C");

    // Retrieve the states and check that they are correct
    Set<String> states = lts.getStates();
    assertEquals(3, states.size(), "Expected three states");
    assertTrue(states.contains("A"), "Expected state 'A' to be present");
    assertTrue(states.contains("B"), "Expected state 'B' to be present");
    assertTrue(states.contains("C"), "Expected state 'C' to be present");
  }

  @Test
  void testGetStatesAfterAddingTransition() {
    // Add a transition which implicitly adds states
    lts.addTransition("A", "B", "action1");

    // Retrieve the states and check that both states are present
    Set<String> states = lts.getStates();
    assertEquals(2, states.size(), "Expected two states after adding a transition");
    assertTrue(states.contains("A"), "Expected state 'A' to be present");
    assertTrue(states.contains("B"), "Expected state 'B' to be present");
  }

  @Test
  void testGetStatesNoDuplicateStates() {
    // Add the same state multiple times
    lts.addState("A");
    lts.addState("A");

    // Retrieve the states and check that there are no duplicates
    Set<String> states = lts.getStates();
    assertEquals(1, states.size(), "Expected only one state 'A' without duplicates");
    assertTrue(states.contains("A"), "Expected state 'A' to be present");
  }

  @Test
  void testGetStatesAfterMultipleTransitions() {
    // Add multiple transitions involving multiple states
    lts.addTransition("A", "B", "action1");
    lts.addTransition("B", "C", "action2");
    lts.addTransition("C", "D", "action3");

    // Retrieve the states and check that all states are present
    Set<String> states = lts.getStates();
    assertEquals(4, states.size(), "Expected four states after multiple transitions");
    assertTrue(states.contains("A"), "Expected state 'A' to be present");
    assertTrue(states.contains("B"), "Expected state 'B' to be present");
    assertTrue(states.contains("C"), "Expected state 'C' to be present");
    assertTrue(states.contains("D"), "Expected state 'D' to be present");
  }

  // getActions()

  @Test
  void testGetActionsInitiallyEmpty() {
    // Test that the actions set is empty initially
    Set<String> actions = lts.getActions();
    assertTrue(actions.isEmpty(), "Expected no actions initially");
  }

  @Test
  void testGetActionsAfterAddingSingleTransition() {
    // Add a transition and verify the action is in the actions set
    lts.addTransition("A", "B", "action1");

    Set<String> actions = lts.getActions();
    assertEquals(1, actions.size(), "Expected one action after adding a transition");
    assertTrue(actions.contains("action1"), "Expected 'action1' to be present");
  }

  @Test
  void testGetActionsAfterAddingMultipleTransitionsWithDifferentActions() {
    // Add multiple transitions with different actions
    lts.addTransition("A", "B", "action1");
    lts.addTransition("B", "C", "action2");
    lts.addTransition("C", "D", "action3");

    Set<String> actions = lts.getActions();
    assertEquals(3, actions.size(), "Expected three actions after adding multiple transitions");
    assertTrue(actions.contains("action1"), "Expected 'action1' to be present");
    assertTrue(actions.contains("action2"), "Expected 'action2' to be present");
    assertTrue(actions.contains("action3"), "Expected 'action3' to be present");
  }

  @Test
  void testGetActionsNoDuplicateActions() {
    // Add the same action multiple times in different transitions
    lts.addTransition("A", "B", "action1");
    lts.addTransition("A", "C", "action1");

    Set<String> actions = lts.getActions();
    assertEquals(1, actions.size(), "Expected no duplicate actions");
    assertTrue(actions.contains("action1"), "Expected 'action1' to be present");
  }

  @Test
  void testGetActionsWithNoTransitions() {
    // Add states but no transitions
    lts.addState("A");
    lts.addState("B");

    // Verify that the actions set is still empty
    Set<String> actions = lts.getActions();
    assertTrue(actions.isEmpty(), "Expected no actions since no transitions were added");
  }

  //containState()

  @Test
  void testContainsStateWhenStateNotPresent() {
    // Verify that a state that hasn't been added returns false
    assertFalse(lts.containsState("A"), "Expected 'A' to not be present in the LTS");
  }

  @Test
  void testContainsStateAfterAddingState() {
    // Add a state and check if it is contained in the LTS
    lts.addState("A");
    assertTrue(lts.containsState("A"), "Expected 'A' to be present in the LTS after being added");
  }

  @Test
  void testContainsStateAfterAddingMultipleStates() {
    // Add multiple states and verify that they are all contained in the LTS
    lts.addState("A");
    lts.addState("B");
    lts.addState("C");

    assertTrue(lts.containsState("A"), "Expected 'A' to be present in the LTS");
    assertTrue(lts.containsState("B"), "Expected 'B' to be present in the LTS");
    assertTrue(lts.containsState("C"), "Expected 'C' to be present in the LTS");
  }

  @Test
  void testContainsStateWithImplicitlyAddedState() {
    // Add a transition which should implicitly add states
    lts.addTransition("A", "B", "action1");

    assertTrue(lts.containsState("A"), "Expected 'A' to be present in the LTS after being implicitly added");
    assertTrue(lts.containsState("B"), "Expected 'B' to be present in the LTS after being implicitly added");
  }

  @Test
  void testContainsStateWithNullState() {
    lts.addState("A");
    // Check how the method behaves when null is passed
    assertFalse(lts.containsState(null), "Expected false when passing null as the state");
  }
}