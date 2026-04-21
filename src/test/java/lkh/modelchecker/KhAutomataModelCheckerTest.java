package lkh.modelchecker;

import lkh.expression.Expression;
import lkh.expression.parser.ParseException;
import lkh.lts.HashMapLTS;
import lkh.lts.LTS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class KhAutomataModelCheckerTest {
  protected AutomataModelChecker<Integer, Character> modelChecker;

  protected abstract AutomataModelChecker<Integer, Character> createModelChecker(LTS<Integer, Character> lts);

  @BeforeEach
  void setUp() {
    modelChecker = createModelChecker(buildLts());
  }

  @Test
  void khHoldsForKnownReachableGoal() throws ParseException {
    Expression expression = Expression.of("kh(p and q, s or t)");
    assertEquals(true, modelChecker.check(expression));
  }

  @Test
  void khHoldsForEmptyPlan() throws ParseException {
    Expression expression = Expression.of("kh(p, p)");
    assertEquals(true, modelChecker.check(expression));
  }

  @Test
  void witnessesMatchExpectedPlan() throws ParseException {
    Expression initExpr = Expression.of("p and q");
    Expression endExpr = Expression.of("s or t");

    Set<List<Character>> actualWitnesses = new HashSet<>();
    modelChecker.witnesses(initExpr, endExpr, 3).forEachRemaining(actualWitnesses::add);

    assertEquals(Set.of(List.of('a', 'b')), actualWitnesses);
  }

  @Test
  void witnessesAllowEmptyPlanWhenAlreadyAtGoal() throws ParseException {
    Expression initExpr = Expression.of("p");
    Expression endExpr = Expression.of("p");

    Set<List<Character>> actualWitnesses = new HashSet<>();
    modelChecker.witnesses(initExpr, endExpr, 3).forEachRemaining(actualWitnesses::add);

    assertEquals(Set.of(List.of()), actualWitnesses);
  }

  public static LTS<Integer, Character> buildLts() {
    LTS<Integer, Character> lts = new HashMapLTS<>();
    lts.addState(0, Set.of("p", "q", "s"));
    lts.addState(1, Set.of("q", "r"));
    lts.addState(2, Set.of("p", "s"));
    lts.addState(3, Set.of("t"));

    lts.addState(4, Set.of("p", "q"));
    lts.addState(5, Set.of("s"));
    lts.addState(6, Set.of("t"));

    lts.addTransition(0, 1, 'a');
    lts.addTransition(1, 2, 'b');
    lts.addTransition(1, 3, 'c');
    lts.addTransition(3, 0, 'b');

    lts.addTransition(4, 5, 'a');
    lts.addTransition(5, 6, 'b');

    return lts;
  }
}
