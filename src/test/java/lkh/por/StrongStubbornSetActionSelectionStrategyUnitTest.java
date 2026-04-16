package lkh.por;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lkh.lts.builder.ActionSelectionStrategy;
import lkh.planning.Action;
import lkh.planning.Problem;
import lkh.planning.State;
import lkh.testutils.TestPlanningObjects.TestAction;
import lkh.testutils.TestPlanningObjects.TestFluent;
import org.junit.jupiter.api.Test;

import static lkh.testutils.TestPlanningObjects.*;

public class StrongStubbornSetActionSelectionStrategyUnitTest {
  private final ActionSelectionStrategy strategy = new StrongStubbornSetActionSelectionStrategy();

  @Test
  public void testRejectsNullState() {
    Problem problem = problem(List.of(), List.of(), state(), condition());

    assertThrows(IllegalArgumentException.class, () -> strategy.selectActions(null, null, problem));
  }

  @Test
  public void testRejectsNullProblem() {
    assertThrows(IllegalArgumentException.class, () -> strategy.selectActions(null, state(), null));
  }

  @Test
  public void testReturnsAllApplicableActionsWhenGoalAlreadyHolds() {
    TestFluent goal = fluent("goal");
    TestAction useful = action("useful", condition(), effect(goal));
    TestAction idle = action("idle", condition(), effect());
    Problem problem = problem(List.of(goal), List.of(useful, idle), state(goal), condition(goal));

    assertEquals(Set.of("useful", "idle"), selectedActions(problem, state(goal)));
  }

  @Test
  public void testBuildsSeedFromUnsatisfiedGoalLiteral() {
    TestFluent ready = fluent("ready");
    TestFluent goal = fluent("goal");
    TestAction prepare = action("prepare", condition(), effect(ready));
    TestAction finish = action("finish", condition(ready), effect(goal));
    TestAction idle = action("idle", condition(), effect());
    Problem problem = problem(List.of(ready, goal), List.of(prepare, finish, idle), state(), condition(goal));

    assertEquals(Set.of("prepare"), selectedActions(problem, state()));
  }

  @Test
  public void testClosesApplicableActionByDependency() {
    TestFluent p = fluent("p");
    TestFluent done = fluent("done");
    TestAction finish = action("finish", conditionNot(p), effect(done));
    TestAction makeP = action("make-p", condition(), effect(p));
    Problem problem = problem(List.of(p, done), List.of(finish, makeP), state(), condition(done));

    assertEquals(Set.of("finish", "make-p"), selectedActions(problem, state()));
  }

  @Test
  public void testClosesNonApplicableActionByNecessaryEnablingSet() {
    TestFluent ready = fluent("ready");
    TestFluent goal = fluent("goal");
    TestAction prepare = action("prepare", condition(), effect(ready));
    TestAction finish = action("finish", condition(ready), effect(goal));
    Problem problem = problem(List.of(ready, goal), List.of(prepare, finish), state(), condition(goal));

    assertEquals(Set.of("prepare"), selectedActions(problem, state()));
  }

  @Test
  public void testReturnsEmptySetForDeadEndGoalLiteralWithoutAchievers() {
    TestFluent goal = fluent("goal");
    TestAction idle = action("idle", condition(), effect());
    Problem problem = problem(List.of(goal), List.of(idle), state(), condition(goal));

    assertEquals(Set.of(), selectedActions(problem, state()));
  }

  private Set<String> selectedActions(Problem problem, State state) {
    return strategy.selectActions(null, state, problem).stream()
        .map(Action::getName)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

}
