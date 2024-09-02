package lkh.modelchecker;

import lkh.expression.Expression;
import lkh.expression.parser.ParseException;
import lkh.lts.HashMapLTS;
import lkh.lts.LTS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ModelCheckerKhTest {
  static ModelChecker<Integer, String> modelChecker;

  @BeforeEach
  void setUp() {
    LTS<Integer, String> lts = new HashMapLTS<>();
    lts.addState(0, Set.of("p", "q", "s"));
    lts.addState(1, Set.of("q", "r"));
    lts.addState(2, Set.of("p", "s"));
    lts.addState(3, Set.of("t"));

    lts.addState(4, Set.of("p", "q"));
    lts.addState(5, Set.of("s"));
    lts.addState(6, Set.of("t"));

    lts.addTransition(0, 1, "a");
    lts.addTransition(1, 2, "b");
    lts.addTransition(1, 3, "c");
    lts.addTransition(3, 0, "b");

    lts.addTransition(4, 5, "a");
    lts.addTransition(5, 6, "b");

    modelChecker = new ModelChecker<>(lts, 0);
  }

  @ParameterizedTest
  @CsvSource({ "'kh(p and q, s or t)', true", "'kh(p, p)', true", "'kh(u,p)', true", "'kh(q and r, r)', true"})
  void testKh(String expressionString, boolean expected) throws ParseException {
    Expression expression = Expression.of(expressionString);

    assertEquals(expected, modelChecker.check(expression), "Expected the proposition to be " + expected + " in state 'A'");
  }
}
