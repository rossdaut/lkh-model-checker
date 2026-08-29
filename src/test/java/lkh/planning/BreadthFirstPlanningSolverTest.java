package lkh.planning;

import static lkh.testutils.TestPlanningObjects.action;
import static lkh.testutils.TestPlanningObjects.condition;
import static lkh.testutils.TestPlanningObjects.effect;
import static lkh.testutils.TestPlanningObjects.fluent;
import static lkh.testutils.TestPlanningObjects.problem;
import static lkh.testutils.TestPlanningObjects.state;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lkh.testutils.TestPlanningObjects.TestFluent;
import org.junit.jupiter.api.Test;

class BreadthFirstPlanningSolverTest {
  @Test
  void solvesSimpleReachableProblem() {
    TestFluent a = fluent("a");
    TestFluent b = fluent("b");

    Problem problem = problem(
        List.of(a, b),
        List.of(
            action("step", condition(a), effect(b))),
        state(a),
        condition(b));

    BreadthFirstPlanningSolver.SearchResult result =
        BreadthFirstPlanningSolver.solve(problem, 100);

    assertTrue(result.solved());
    assertFalse(result.hitLimit());
  }

  @Test
  void reportsUnsolvedWhenGoalIsNotReachable() {
    TestFluent a = fluent("a");
    TestFluent b = fluent("b");

    Problem problem = problem(
        List.of(a, b),
        List.of(),
        state(a),
        condition(b));

    BreadthFirstPlanningSolver.SearchResult result =
        BreadthFirstPlanningSolver.solve(problem, 100);

    assertFalse(result.solved());
    assertFalse(result.hitLimit());
  }

  @Test
  void reportsLimitWhenFrontierGrowsPastBudget() {
    TestFluent start = fluent("start");
    TestFluent x = fluent("x");
    TestFluent y = fluent("y");
    TestFluent z = fluent("z");
    TestFluent goal = fluent("goal");

    Problem problem = problem(
        List.of(start, x, y, z, goal),
        List.of(
            action("to-x", condition(start), effect(x)),
            action("to-y", condition(start), effect(y)),
            action("to-z", condition(start), effect(z)),
            action("to-goal", condition(z), effect(goal))),
        state(start),
        condition(goal));

    BreadthFirstPlanningSolver.SearchResult result =
        BreadthFirstPlanningSolver.solve(problem, 3);

    assertFalse(result.solved());
    assertTrue(result.hitLimit());
  }
}
