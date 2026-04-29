package lkh.planning;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lkh.planning.pddl4j.Pddl4jProblem;
import org.junit.jupiter.api.Test;

class Pddl4jPlanningTest {
  private static final String TEST_RESOURCES = "src/test/resources/pddl";
  private static final String TEST_DOMAIN = TEST_RESOURCES + "/domain.pddl";
  private static final String TEST_PROBLEM = TEST_RESOURCES + "/problem.pddl";
  private static final String TIRE_DOMAIN = TEST_RESOURCES + "/strategies/tire-domain.pddl";
  private static final String TIRE_PROBLEM = TEST_RESOURCES + "/strategies/tire-problem.pddl";

  @Test
  void testInitialApplicabilityForSimpleDomain() throws Exception {
    Problem problem = new Pddl4jProblem(TEST_DOMAIN, TEST_PROBLEM);
    State state = problem.getInitialState();
    Map<String, Action> actions = actionsByName(problem);

    assertTrue(actions.get("a").isApplicable(state));
    assertTrue(actions.get("b").isApplicable(state));
    assertTrue(actions.get("c").isApplicable(state));
    assertTrue(actions.get("d").isApplicable(state));
  }

  @Test
  void testApplyActionADeletesPYAndKeepsPX() throws Exception {
    Problem problem = new Pddl4jProblem(TEST_DOMAIN, TEST_PROBLEM);
    State state = problem.getInitialState().copy();
    Map<String, Action> actions = actionsByName(problem);

    state.apply(actions.get("a"));

    assertTrue(holds(state, "p(x)"));
    assertFalse(holds(state, "p(y)"));
    assertTrue(actions.get("a").isApplicable(state));
    assertFalse(actions.get("b").isApplicable(state));
    assertTrue(actions.get("c").isApplicable(state));
    assertFalse(actions.get("d").isApplicable(state));
  }

  @Test
  void testApplyActionCKeepsBothFluentsTrue() throws Exception {
    Problem problem = new Pddl4jProblem(TEST_DOMAIN, TEST_PROBLEM);
    State state = problem.getInitialState().copy();
    Map<String, Action> actions = actionsByName(problem);

    state.apply(actions.get("c"));

    assertTrue(holds(state, "p(x)"));
    assertTrue(holds(state, "p(y)"));
  }

  @Test
  void testTireRemoveFromGroundKeepsObjectOnGround() throws Exception {
    Problem problem = new Pddl4jProblem(TIRE_DOMAIN, TIRE_PROBLEM);
    Map<String, Action> actions = actionsByName(problem);
    State state = problem.getInitialState().copy();

    state.apply(actions.get("remove(spare, trunk)"));
    assertTrue(holds(state, "at(spare, ground)"));

    state.apply(actions.get("remove(spare, ground)"));
    assertTrue(holds(state, "at(spare, ground)"));
    assertFalse(holds(state, "at(spare, trunk)"));
  }

  @Test
  void testTirePutOnSpareMovesItFromGroundToAxle() throws Exception {
    Problem problem = new Pddl4jProblem(TIRE_DOMAIN, TIRE_PROBLEM);
    Map<String, Action> actions = actionsByName(problem);
    State state = problem.getInitialState().copy();

    state.apply(actions.get("remove(spare, trunk)"));
    state.apply(actions.get("remove(flat, axle)"));
    state.apply(actions.get("put-on(spare)"));

    assertFalse(holds(state, "at(spare, ground)"));
    assertTrue(holds(state, "at(spare, axle)"));
    assertFalse(holds(state, "at(flat, axle)"));
  }

  private static Map<String, Action> actionsByName(Problem problem) {
    return problem.getActions().stream()
        .collect(Collectors.toMap(Action::getName, Function.identity()));
  }

  private static boolean holds(State state, String fluentName) {
    return state.getFluents().stream().anyMatch(fluent -> fluent.toString().equals(fluentName));
  }
}
