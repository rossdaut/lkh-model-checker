package lkh.modelchecker;

import lkh.expression.Expression;
import lkh.expression.parser.ParseException;
import lkh.lts.HashMapLTS;
import lkh.lts.LTS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutomataModelCheckerKhTest {
  static AutomataModelChecker<Integer, Character> modelChecker;

  @BeforeEach
  void setUp() {
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

    modelChecker = new AutomataModelChecker<>(lts, 0);
  }

  @ParameterizedTest
  @CsvSource({ "'kh(p and q, s or t)', true", "'kh(p, p)', true", "'kh(q and r, r)', true"})
  void testKh(String expressionString, boolean expected) throws ParseException {
    Expression expression = Expression.of(expressionString);

    assertEquals(expected, modelChecker.check(expression), "Expected the proposition to be " + expected + " in state 'A'");
  }

  @ParameterizedTest
  @MethodSource({"witnessesTestProvider"})
  void testWitnesses(String initExprString, String endExprString, int witnessLengthLimit, Set<List<Character>> expectedWitnesses) throws ParseException {
    Expression initExpr = Expression.of(initExprString);
    Expression endExpr = Expression.of(endExprString);

    Iterator<List<Character>> it = modelChecker.witnesses(initExpr, endExpr, witnessLengthLimit);
    Set<List<Character>> actualWitnesses = new HashSet<>();
    it.forEachRemaining(actualWitnesses::add);

    assertEquals(expectedWitnesses, actualWitnesses);
  }

  private static Stream<Arguments> witnessesTestProvider() {
    return Stream.of(
        Arguments.of("p and q", "s or t", 3, Set.of(List.of('a', 'b'))),
        Arguments.of("p", "p", 3, Set.of(List.of())),
        Arguments.of("q and r", "r", 6, Set.of(List.of()))
        //Arguments.of("u", "q")
    );
  }
}
