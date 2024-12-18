package lkh.expression.parser;

import lkh.expression.Expression;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {
  private static Parser parser;

  @BeforeAll
  public static void setUp() {
    String expression = "kh((a implies b) implies c, p)";
    parser = new Parser(new StringReader(expression));
  }

  @Test
  public void atomTest() throws ParseException {
    String expression = "p";
    parser.ReInit(new StringReader(expression));

    Expression expected = new Expression(TokenType.PROP, "p");
    Expression actual = parser.Atom();

    assertEquals(expected, actual);
  }

  @Test
  public void notTest() throws ParseException {
    String expression = "not p";
    Expression expected, actual, right;
    parser.ReInit(new StringReader(expression));

    right = new Expression(TokenType.PROP, "p");
    expected = new Expression(TokenType.NOT, "not", null, right);
    actual = parser.Not();

    assertEquals(expected, actual);
  }

  @Test
  public void notTestWithParens() throws ParseException {
    String expression = "not (p)";
    Expression expected, actual, right;
    parser.ReInit(new StringReader(expression));

    right = new Expression(TokenType.PROP, "p");
    expected = new Expression(TokenType.NOT, "not", null, right);
    actual = parser.Not();

    assertEquals(expected, actual);
  }

  @Test
  public void ConjuctionTest() throws ParseException {
    String expression = "p1 and p2";
    Expression expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new Expression(TokenType.PROP, "p1");
    right = new Expression(TokenType.PROP, "p2");
    expected = new Expression(TokenType.AND, "and", left, right);
    actual = parser.Conjunction();

    assertEquals(expected, actual);
  }


  @Test
  public void DisyunctionTest() throws ParseException {
    String expression = "p1 or p2";
    Expression expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new Expression(TokenType.PROP, "p1");
    right = new Expression(TokenType.PROP, "p2");
    expected = new Expression(TokenType.OR, "or", left, right);
    actual = parser.Disyunction();

    assertEquals(expected, actual);
  }

  @Test
  public void ImpliesTest() throws ParseException {
    String expression = "p1 implies p2";
    Expression expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new Expression(TokenType.PROP, "p1");
    right = new Expression(TokenType.PROP, "p2");
    expected = new Expression(TokenType.IMPLIES, "implies", left, right);
    actual = parser.Implies();

    assertEquals(expected, actual);
  }

  @Test
  public void KhTest() throws ParseException {
    String expression = "kh(p1, p2)";
    Expression expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new Expression(TokenType.PROP, "p1");
    right = new Expression(TokenType.PROP, "p2");
    expected = new Expression(TokenType.KH, "kh", left, right);
    actual = parser.Atom();

    assertEquals(expected, actual);
  }

  @Test
  public void ParensTest() throws ParseException {
    String expression = "(p1 or p2)";
    Expression expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new Expression(TokenType.PROP, "p1");
    right = new Expression(TokenType.PROP, "p2");
    expected = new Expression(TokenType.OR, "or", left, right);
    actual = parser.Atom();

    assertEquals(expected, actual);
  }

  @Test
  public void ExpressionTest() throws ParseException {
    String expression = "kh((a implies b) or c, p and q)";
    Expression a, b, c, p, q, implies, or, and, expected, actual;
    parser.ReInit(new StringReader(expression));

    // Build expected TreeNode
    a = new Expression(TokenType.PROP, "a");
    b = new Expression(TokenType.PROP, "b");
    c = new Expression(TokenType.PROP, "c");
    implies = new Expression(TokenType.IMPLIES, "implies", a, b);
    or = new Expression(TokenType.OR, "or", implies, c);
    p = new Expression(TokenType.PROP, "p");
    q = new Expression(TokenType.PROP, "q");
    and = new Expression(TokenType.AND, "and", p, q);
    expected = new Expression(TokenType.KH, "kh", or, and);

    actual = parser.Expression();

    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @CsvSource({"p(x)", "'p(y, z)'"})
  public void fluentTest(String expression) throws ParseException {
    Expression expected, actual;
    parser.ReInit(new StringReader(expression));

    expected = new Expression(TokenType.PROP, expression);
    actual = parser.Atom();

    assertEquals(expected, actual);
  }

  @Test
  public void expressionWithFluentsTest() throws ParseException {
    String expression = "p(x) and q(y, z)";
    Expression p, q, expected, actual;
    parser.ReInit(new StringReader(expression));

    p = new Expression(TokenType.PROP, "p(x)");
    q = new Expression(TokenType.PROP, "q(y, z)");
    expected = new Expression(TokenType.AND, "and", p, q);
    actual = parser.Expression();

    assertEquals(expected, actual);
  }

  @Test
  public void complexExpressionWithFluentsTest() throws ParseException {
    String expression = "kh((a(n, m, o) implies b) or c, p(x) and q(y, z))";
    Expression a, b, c, p, q, implies, or, and, expected, actual;
    parser.ReInit(new StringReader(expression));

    // Build expected TreeNode
    a = new Expression(TokenType.PROP, "a(n, m, o)");
    b = new Expression(TokenType.PROP, "b");
    c = new Expression(TokenType.PROP, "c");
    implies = new Expression(TokenType.IMPLIES, "implies", a, b);
    or = new Expression(TokenType.OR, "or", implies, c);
    p = new Expression(TokenType.PROP, "p(x)");
    q = new Expression(TokenType.PROP, "q(y, z)");
    and = new Expression(TokenType.AND, "and", p, q);
    expected = new Expression(TokenType.KH, "kh", or, and);

    actual = parser.Expression();

    assertEquals(expected, actual);
  }

  @Test
  public void parensAndFluentsTest() throws ParseException {
    String expression = "((p(x, y) or a) implies b)";
    Expression p, a, b, or, expected, actual;
    parser.ReInit(new StringReader(expression));

    p = new Expression(TokenType.PROP, "p(x, y)");
    a = new Expression(TokenType.PROP, "a");
    b = new Expression(TokenType.PROP, "b");
    or = new Expression(TokenType.OR, "or", p, a);
    expected = new Expression(TokenType.IMPLIES, "implies", or, b);

    actual = parser.Expression();

    assertEquals(expected, actual);
  }
}
