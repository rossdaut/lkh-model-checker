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
    // [p] -a-> [q] -b-> [r]    [p] -a-> [q]
    LTS<Integer, String> lts = new HashMapLTS<>();
    lts.addState(0, Set.of("p"));
    lts.addState(1, Set.of("q"));
    lts.addState(2, Set.of("r"));
    lts.addState(3, Set.of("p"));
    lts.addState(4, Set.of("q"));

    lts.addTransition(0, 1, "a");
    lts.addTransition(1, 2, "b");
    lts.addTransition(3, 4, "a");

    modelChecker = new ModelChecker<>(lts, 0);
  }

  @ParameterizedTest
  @CsvSource({ "'kh(p, q)', true", "'kh(p, r)', false"}) //kh(p, s), false
  void testKh(String expressionString, boolean expected) throws ParseException {
    Expression expression = Expression.of(expressionString);

    assertEquals(expected, modelChecker.check(expression), "Expected the proposition to be " + expected + " in state 'A'");
  }
}
