package lkh.modelchecker;

import lkh.expression.Expression;
import lkh.expression.parser.ParseException;
import lkh.lts.HashMapLTS;
import lkh.lts.LTS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AutomataModelCheckerTest {
  private static AutomataModelChecker<String, String> modelChecker;

  @BeforeEach
  void setUp() {
    // Initialize LTS with predefined states and labels
    LTS<String, String> lts = new HashMapLTS<>();
    lts.addState("A", Set.of("p", "q"));
    lts.addState("B", Set.of("r"));

    modelChecker = new AutomataModelChecker<>(lts, "A");
  }

  @ParameterizedTest
  @CsvSource({ "p, true", "q, true", "r, false", "s, false" })
  void testCheckProp(String expression, boolean expected) throws ParseException {
    Expression expr = Expression.of(expression);

    assertEquals(expected, modelChecker.check(expr), "Expected the proposition to be " + expected + " in state 'A'");
  }

  @ParameterizedTest
  @CsvSource({ "not p, false", "not q, false", "not r, true", "not s, true" })
  void testCheckNot(String expression, boolean expected) throws ParseException {
    Expression expr = Expression.of(expression);

    assertEquals(expected, modelChecker.check(expr), "Expected the proposition to be " + expected + " in state 'A'");
  }

  @ParameterizedTest
  @CsvSource({ "p and q, true", "p and r, false" })
  void testCheckAnd(String expression, boolean expected) throws ParseException {
    Expression expr = Expression.of(expression);

    assertEquals(expected, modelChecker.check(expr), "Expected the proposition to be " + expected + " in state 'A'");
  }

  @ParameterizedTest
  @CsvSource({ "p or q, true", "p or r, true", "s or q, true", "s or r, false" })
  void testCheckOr(String expression, boolean expected) throws ParseException {
    Expression expr = Expression.of(expression);

    assertEquals(expected, modelChecker.check(expr), "Expected the proposition to be " + expected + " in state 'A'");
  }

  @ParameterizedTest
  @CsvSource({ "p implies q, true", "q implies p, true", "p implies r, false", "r implies p, true", "s implies r, true" })
  void testCheckImplies(String expression, boolean expected) throws ParseException {
    Expression expr = Expression.of(expression);

    assertEquals(expected, modelChecker.check(expr), "Expected the proposition to be " + expected + " in state 'A'");
  }

  @ParameterizedTest
  @CsvSource({ "((p implies q) or s) and not r, true", "((p implies q) or s) and r, false", "p and q implies r, false"})
  void testCheckComposite(String expression, boolean expected) throws ParseException {
    Expression expr = Expression.of(expression);

    assertEquals(expected, modelChecker.check(expr), "Expected the proposition to be " + expected + " in state 'A'");
  }
}
