package lkh.lts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HashMapLTSTest {

  private HashMapLTS<String, String> lts;

  @BeforeEach
  public void setUp() {
    lts = new HashMapLTS<>();
  }

  // addState()

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
    assertThrows(
        NullPointerException.class,
        () -> lts.addState(null),
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
  public void testAddStateDoesntAddLabel() {
    String state1 = "State1";

    lts.addState(state1);
    var labels = lts.getLabels(state1);

    assertTrue(labels.isEmpty());
  }

  // addState(State, Set<String>)

  @Test
  public void testAddStateAndOneLabel() {
    String state = "State";
    String label = "Label";

    lts.addState(state, Set.of(label));
    var states = lts.getStates();
    var labels = lts.getLabels(state);

    assertEquals(1, states.size());
    assertEquals(1, labels.size());
    assertTrue(states.contains(state));
    assertTrue(labels.contains(label));
  }

  @Test
  public void testAddStateMultipleLabels() {
    String state = "State";
    Set<String> labels = Set.of("Label1", "Label2", "Label3");

    lts.addState(state, labels);

    assertTrue(lts.getLabels(state).contains("Label1"));
    assertTrue(lts.getLabels(state).contains("Label2"));
    assertTrue(lts.getLabels(state).contains("Label3"));
  }

  @Test
  public void testAddStateThrowsNullState() {
    Set<String> label = Set.of("Label");

    assertThrows(NullPointerException.class, () -> lts.addState(null, label));
  }

  @Test
  public void testAddStateThrowsNullLabels() {
    String state = "State";

    assertThrows(NullPointerException.class, () -> lts.addState(state, null));
  }

  // addLabel()
  @Test
  void testAddLabelWithValidStateAndLabel() {
    // Add a state and then add a label to it
    lts.addState("A");
    lts.addLabel("A", "label1");

    Set<String> labels = lts.getLabels("A");
    assertEquals(1, labels.size(), "Expected one label to be associated with 'A'");
    assertTrue(labels.contains("label1"), "Expected 'label1' to be associated with 'A'");
  }

  @Test
  void testAddLabelWithNullState() {
    // Verify that adding a label with a null state throws a NullPointerException
    assertThrows(
        NullPointerException.class,
        () -> lts.addLabel(null, "label1"),
        "Expected a NullPointerException when adding a label with null state");
  }

  @Test
  void testAddLabelWithNullLabel() {
    // Add a state and then verify that adding a null label throws a NullPointerException
    lts.addState("A");
    assertThrows(
        NullPointerException.class,
        () -> lts.addLabel("A", null),
        "Expected a NullPointerException when adding a null label");
  }

  @Test
  void testAddLabelToNonExistentState() {
    // Verify that adding a label to a non-existent state throws an IllegalArgumentException
    assertThrows(
        IllegalArgumentException.class,
        () -> lts.addLabel("A", "label1"),
        "Expected an IllegalArgumentException when adding a label to a non-existent state");
  }

  @Test
  void testAddLabelMultipleTimesToSameState() {
    // Add a state and then add the same label multiple times
    lts.addState("A");
    lts.addLabel("A", "label1");
    lts.addLabel("A", "label1"); // Add the same label again

    Set<String> labels = lts.getLabels("A");
    assertEquals(1, labels.size(), "Expected only one instance of the label to be associated with 'A'");
    assertTrue(labels.contains("label1"), "Expected 'label1' to be associated with 'A'");
  }

  @Test
  void testAddDifferentLabelsToSameState() {
    // Add a state and then add multiple different labels to it
    lts.addState("A");
    lts.addLabel("A", "label1");
    lts.addLabel("A", "label2");

    Set<String> labels = lts.getLabels("A");
    assertEquals(2, labels.size(), "Expected two labels to be associated with 'A'");
    assertTrue(labels.contains("label1"), "Expected 'label1' to be associated with 'A'");
    assertTrue(labels.contains("label2"), "Expected 'label2' to be associated with 'A'");
  }

  @Test
  void testAddLabelAfterAddingLabelsWithAddState() {
    // Add a state with labels using addState, then add another label with addLabel
    lts.addState("A", Set.of("label1", "label2"));
    lts.addLabel("A", "label3");

    Set<String> labels = lts.getLabels("A");
    assertEquals(3, labels.size(), "Expected three labels to be associated with 'A'");
    assertTrue(labels.contains("label1"), "Expected 'label1' to be associated with 'A'");
    assertTrue(labels.contains("label2"), "Expected 'label2' to be associated with 'A'");
    assertTrue(labels.contains("label3"), "Expected 'label3' to be associated with 'A'");
  }

  // addLabels()

  @Test
  void testAddLabelsWithValidStateAndLabels() {
    // Add a state and then add multiple labels to it
    lts.addState("A");
    lts.addLabels("A", Set.of("label1", "label2"));

    Set<String> labels = lts.getLabels("A");
    assertEquals(2, labels.size(), "Expected two labels to be associated with 'A'");
    assertTrue(labels.contains("label1"), "Expected 'label1' to be associated with 'A'");
    assertTrue(labels.contains("label2"), "Expected 'label2' to be associated with 'A'");
  }

  @Test
  void testAddLabelsWithEmptyLabels() {
    // Add a state and then add an empty set of labels to it
    lts.addState("A");
    lts.addLabels("A", Set.of());

    Set<String> labels = lts.getLabels("A");
    assertTrue(labels.isEmpty(), "Expected no labels to be associated with 'A'");
  }

  @Test
  void testAddLabelsToNonExistentState() {
    // Verify that adding labels to a non-existent state throws an IllegalArgumentException
    assertThrows(
        IllegalArgumentException.class,
        () -> lts.addLabels("A", Set.of("label1")),
        "Expected an IllegalArgumentException when adding labels to a non-existent state");
  }

  @Test
  void testAddLabelsWithNullState() {
    // Verify that adding labels with a null state throws a NullPointerException
    assertThrows(
        NullPointerException.class,
        () -> lts.addLabels(null, Set.of("label1")),
        "Expected a NullPointerException when adding labels with null state");
  }

  @Test
  void testAddLabelsWithNullLabels() {
    // Add a state and then verify that adding null labels throws a NullPointerException
    lts.addState("A");
    assertThrows(
        NullPointerException.class,
        () -> lts.addLabels("A", null),
        "Expected a NullPointerException when adding null labels");
  }

  @Test
  void testAddLabelsMultipleTimes() {
    // Add a state and then add multiple labels to it in separate calls
    lts.addState("A");
    lts.addLabels("A", Set.of("label1"));
    lts.addLabels("A", Set.of("label2", "label3"));

    Set<String> labels = lts.getLabels("A");
    assertEquals(3, labels.size(), "Expected three labels to be associated with 'A'");
    assertTrue(labels.contains("label1"), "Expected 'label1' to be associated with 'A'");
    assertTrue(labels.contains("label2"), "Expected 'label2' to be associated with 'A'");
    assertTrue(labels.contains("label3"), "Expected 'label3' to be associated with 'A'");
  }

  @Test
  void testAddLabelsWithDuplicateLabels() {
    // Add a state and then add labels, including duplicates
    lts.addState("A");
    lts.addLabels("A", Set.of("label1", "label2"));
    lts.addLabels("A", Set.of("label2", "label3")); // 'label2' is duplicate

    Set<String> labels = lts.getLabels("A");
    assertEquals(3, labels.size(), "Expected three labels to be associated with 'A'");
    assertTrue(labels.contains("label1"), "Expected 'label1' to be associated with 'A'");
    assertTrue(labels.contains("label2"), "Expected 'label2' to be associated with 'A'");
    assertTrue(labels.contains("label3"), "Expected 'label3' to be associated with 'A'");
  }

  // addTransition()

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
    assertThrows(
        NullPointerException.class,
        () -> lts.addTransition(source, target, null),
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
    assertEquals(
        1,
        targets.size(),
        "There should be only one instance of the target state for the given action.");
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

    assertEquals(
        targetsAction1,
        Collections.singleton(target1),
        "Target1 only should be reachable via Action1.");
    assertEquals(
        targetsAction2,
        Collections.singleton(target2),
        "Target2 only should be reachable via Action2.");
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

  // getLabels()

  @Test
  void testGetLabelsForNonExistentState() {
    // Try to retrieve labels for a non-existent state
    assertThrows(
        IllegalArgumentException.class,
        () -> lts.getLabels("A"),
        "Expected labels to be null for non-existent state");
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

  // containState()

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

    assertTrue(
        lts.containsState("A"),
        "Expected 'A' to be present in the LTS after being implicitly added");
    assertTrue(
        lts.containsState("B"),
        "Expected 'B' to be present in the LTS after being implicitly added");
  }

  @Test
  void testContainsStateWithNullState() {
    lts.addState("A");
    // Check how the method behaves when null is passed
    assertFalse(lts.containsState(null), "Expected false when passing null as the state");
  }

  // targets()

  @Test
  void testTargetsWhenStateNotPresent() {
    // Expect an exception when the source state is not in the LTS
    assertThrows(
        IllegalArgumentException.class,
        () -> lts.targets("A", "action1"),
        "Expected exception for non-existent source state");
  }

  @Test
  void testTargetsWhenActionNotPresent() {
    // Add a state but no transitions
    lts.addState("A");

    // Expect an empty set when no transitions are associated with the action
    Set<String> targets = lts.targets("A", "action1");
    assertTrue(targets.isEmpty(), "Expected no target states for an action that doesn't exist");
  }

  @Test
  void testTargetsWithSingleTransition() {
    // Add a transition and verify the target state is correct
    lts.addTransition("A", "B", "action1");

    Set<String> targets = lts.targets("A", "action1");
    assertEquals(1, targets.size(), "Expected one target state");
    assertTrue(targets.contains("B"), "Expected 'B' to be the target state");
  }

  @Test
  void testTargetsWithMultipleTransitionsSameAction() {
    // Add multiple transitions from the same source with the same action
    lts.addTransition("A", "B", "action1");
    lts.addTransition("A", "C", "action1");

    Set<String> targets = lts.targets("A", "action1");
    assertEquals(2, targets.size(), "Expected two target states");
    assertTrue(targets.contains("B"), "Expected 'B' to be a target state");
    assertTrue(targets.contains("C"), "Expected 'C' to be a target state");
  }

  @Test
  void testTargetsWithDifferentActions() {
    // Add transitions with different actions
    lts.addTransition("A", "B", "action1");
    lts.addTransition("A", "C", "action2");

    Set<String> targets1 = lts.targets("A", "action1");
    Set<String> targets2 = lts.targets("A", "action2");

    assertEquals(1, targets1.size(), "Expected one target state for action1");
    assertEquals(1, targets2.size(), "Expected one target state for action2");
    assertTrue(targets1.contains("B"), "Expected 'B' to be the target for action1");
    assertTrue(targets2.contains("C"), "Expected 'C' to be the target for action2");
  }

  // targets(Set<State>, Action, boolean)

  @Test
  void testTargetsWithMultipleSourceStatesAndAction() {
    lts.addTransition("A", "X", "action1");
    lts.addTransition("B", "Y", "action1");
    lts.addTransition("C", "Z", "action2");

    // Test with multiple source states and an action
    Set<String> sourceStates = Set.of("A", "B");
    Optional<Set<String>> targets = lts.targets(sourceStates, "action1", false);

    assertTrue(targets.isPresent(), "Expected targets to be present");
    assertEquals(2, targets.get().size(), "Expected two target states");
    assertTrue(targets.get().contains("X"), "Expected 'X' to be a target state");
    assertTrue(targets.get().contains("Y"), "Expected 'Y' to be a target state");
  }

  @Test
  void testTargetsWithEmptySourceStates() {
    // Test with empty source states
    Set<String> sourceStates = new HashSet<>();
    Optional<Set<String>> targets = lts.targets(sourceStates, "action1", false);

    assertTrue(targets.isPresent(), "Expected targets to be present");
    assertTrue(targets.get().isEmpty(), "Expected no target states");
  }

  @Test
  void testTargetsWithNonExistentSourceState() {
    // Add state and transition
    lts.addState("B");
    lts.addTransition("A", "X", "action1");

    // Test with a non-existent source state
    Set<String> sourceStates = Set.of("B");
    Optional<Set<String>> targets = lts.targets(sourceStates, "action1", false);

    assertTrue(targets.isPresent(), "Expected targets to be present");
    assertTrue(targets.get().isEmpty(), "Expected no target states for non-existent source state");
  }

  @Test
  void testTargetsWithNonExistentAction() {
    lts.addState("B");
    lts.addTransition("A", "X", "action1");

    // Test with an action that doesn't exist
    Set<String> sourceStates = Set.of("A", "B");
    Optional<Set<String>> targets = lts.targets(sourceStates, "action2", false);

    assertTrue(targets.isPresent(), "Expected targets to be present");
    assertTrue(targets.get().isEmpty(), "Expected no target states for non-existent action");
  }

  @Test
  void testTargetsWithStronglyExecutableAndEmptyTarget() {
    lts.addState("B");
    lts.addTransition("A", "X", "action1");

    // Test with stronglyExecutable = true and one source state leading to no target
    Set<String> sourceStates = Set.of("A", "B");
    Optional<Set<String>> targets = lts.targets(sourceStates, "action1", true);

    assertFalse(targets.isPresent(), "Expected no targets due to strongly executable and empty target");
  }

  @Test
  void testTargetsWithStronglyExecutableAndNonEmptyTarget() {
    lts.addState("C");
    lts.addTransition("A", "X", "action1");
    lts.addTransition("B", "Y", "action1");

    // Test with stronglyExecutable = true and all source states having targets
    Set<String> sourceStates = Set.of("A", "B");
    Optional<Set<String>> targets = lts.targets(sourceStates, "action1", true);

    assertTrue(targets.isPresent(), "Expected targets to be present");
    assertEquals(2, targets.get().size(), "Expected two target states");
    assertTrue(targets.get().contains("X"), "Expected 'X' to be a target state");
    assertTrue(targets.get().contains("Y"), "Expected 'Y' to be a target state");
  }

  @Test
  void testTargetsWithSingleSourceStateAndStronglyExecutable() {
    // Add states and transitions
    lts.addState("A");
    lts.addTransition("A", "X", "action1");

    // Test with a single source state and stronglyExecutable = true
    Set<String> sourceStates = Set.of("A");
    Optional<Set<String>> targets = lts.targets(sourceStates, "action1", true);

    assertTrue(targets.isPresent(), "Expected targets to be present");
    assertEquals(1, targets.get().size(), "Expected one target state");
    assertTrue(targets.get().contains("X"), "Expected 'X' to be a target state");
  }
}
