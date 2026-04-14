package lkh.por;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static lkh.testutils.TestPlanningObjects.action;
import static lkh.testutils.TestPlanningObjects.condition;
import static lkh.testutils.TestPlanningObjects.effect;
import static lkh.testutils.TestPlanningObjects.fluent;
import static lkh.testutils.TestPlanningObjects.problem;
import static lkh.testutils.TestPlanningObjects.state;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lkh.lts.builder.ActionSelectionStrategy;
import lkh.planning.Action;
import lkh.planning.Condition;
import lkh.planning.Effect;
import lkh.planning.Fluent;
import lkh.planning.Problem;
import lkh.planning.State;
import lkh.testutils.TestPlanningObjects.TestAction;
import lkh.testutils.TestPlanningObjects.TestFluent;
import org.junit.jupiter.api.Test;

public class StratifiedActionSelectionStrategyUnitTest {
  private final ActionSelectionStrategy strategy = new StratifiedActionSelectionStrategy();

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
  public void testReturnsOnlyApplicableActionsWhenThereIsNoPreviousAction() {
    TestFluent p = fluent("p");
    TestAction applicable = action("applicable", condition(p), effect(p));
    TestAction notApplicable = action("not-applicable", condition(fluent("q")), effect());
    Problem problem = problem(List.of(p), List.of(applicable, notApplicable), state(p), condition());

    assertEquals(Set.of("applicable"), selectedActions(null, state(p), problem));
  }

  @Test
  public void testFiltersLowerLayerActionWithoutFollowUpRelation() {
    TestFluent p = fluent("p");
    TestFluent q = fluent("q");
    TestFluent r = fluent("r");
    TestAction highLayer = action("high-layer", condition(p), effect(Set.of(p, q), Set.of()));
    TestAction lowLayer = action("low-layer", condition(r), effect(r));
    Problem problem = problem(List.of(p, q, r), List.of(highLayer, lowLayer), state(p, r), condition());

    assertEquals(Set.of("high-layer"), selectedActions(highLayer, state(p, r), problem));
  }

  @Test
  public void testKeepsLowerLayerActionWhenFollowUpRelationExists() {
    TestFluent p = fluent("p");
    TestFluent q = fluent("q");
    TestAction highLayer = action("high-layer", condition(p), effect(Set.of(p, q), Set.of()));
    TestAction lowLayerFollowUp = action("low-layer-follow-up", condition(q), effect(q));
    Problem problem = problem(List.of(p, q), List.of(highLayer, lowLayerFollowUp), state(p, q), condition());

    assertEquals(Set.of("high-layer", "low-layer-follow-up"), selectedActions(highLayer, state(p, q), problem));
  }

  @Test
  public void testRejectsActionsThatDoNotImplementAnalyzableAction() {
    TestFluent p = fluent("p");
    Action plainAction = new PlainAction("plain", condition(p), effect(p));
    Problem problem = problem(List.of(p), List.of(plainAction), state(p), condition());

    assertThrows(IllegalArgumentException.class, () -> strategy.selectActions(null, state(p), problem));
  }

  private Set<String> selectedActions(Action previousAction, State state, Problem problem) {
    return strategy.selectActions(previousAction, state, problem).stream()
        .map(Action::getName)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private static final class PlainAction implements Action {
    private final String name;
    private final Condition precondition;
    private final Effect effect;

    private PlainAction(String name, Condition precondition, Effect effect) {
      this.name = name;
      this.precondition = precondition;
      this.effect = effect;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public Condition getPrecondition() {
      return precondition;
    }

    @Override
    public Effect getEffects() {
      return effect;
    }

    @Override
    public boolean isApplicable(State state) {
      return precondition.getLiterals().stream().allMatch(state::holds);
    }
  }
}
