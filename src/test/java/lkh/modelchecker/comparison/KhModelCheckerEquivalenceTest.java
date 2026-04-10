package lkh.modelchecker.comparison;

import lkh.expression.Expression;
import lkh.expression.parser.ParseException;
import lkh.lts.LTS;
import lkh.modelchecker.AutomataModelChecker;
import lkh.modelchecker.ClassicAutomataModelChecker;
import lkh.modelchecker.KhAutomataModelCheckerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KhModelCheckerEquivalenceTest {
  private AutomataModelChecker<Integer, Character> directModelChecker;
  private ClassicAutomataModelChecker<Integer, Character> classicModelChecker;

  @BeforeEach
  void setUp() {
    LTS<Integer, Character> lts = KhAutomataModelCheckerTest.buildLts();
    directModelChecker = new AutomataModelChecker<>(lts, 0);
    classicModelChecker = new ClassicAutomataModelChecker<>(lts, 0);
  }

  @Test
  void directAndClassicAgreeOnKhChecks() throws ParseException {
    Set<Expression> expressions = Set.of(
        Expression.of("kh(p and q, s or t)"),
        Expression.of("kh(p, p)"),
        Expression.of("kh(q and r, r)"),
        Expression.of("kh(p, s)"),
        Expression.of("kh(p and q, q)")
    );

    for (Expression expression : expressions) {
      assertEquals(classicModelChecker.check(expression), directModelChecker.check(expression));
    }
  }

  @Test
  void directAndClassicAgreeOnWitnesses() throws ParseException {
    Expression initExpr = Expression.of("p and q");
    Expression endExpr = Expression.of("s or t");

    Set<List<Character>> directWitnesses = new HashSet<>();
    directModelChecker.witnesses(initExpr, endExpr, 3).forEachRemaining(directWitnesses::add);

    Set<List<Character>> classicWitnesses = new HashSet<>();
    classicModelChecker.witnesses(initExpr, endExpr, 3).forEachRemaining(classicWitnesses::add);

    assertEquals(classicWitnesses, directWitnesses);
  }
}
