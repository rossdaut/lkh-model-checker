package lkh.expression.parser;

import lkh.expression.TreeNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTest {
  private static Parser parser;

  @BeforeAll
  public static void setUp() throws FileNotFoundException {
    String expression = "kh((a implies b) implies c, p)";
    parser = new Parser(new StringReader(expression));
  }

  @Test
  public void atomTest() throws ParseException {
    String expression = "p";
    parser.ReInit(new StringReader(expression));

    TreeNode expected = new TreeNode(TokenType.PROP, "p");
    TreeNode actual = parser.Atom();

    assertEquals(expected, actual);
  }

  @Test
  public void notTest() throws ParseException {
    String expression = "not p";
    TreeNode expected, actual, right;
    parser.ReInit(new StringReader(expression));

    right = new TreeNode(TokenType.PROP, "p");
    expected = new TreeNode(TokenType.NOT, "not", null, right);
    actual = parser.Not();

    assertEquals(expected, actual);
  }

  @Test
  public void notTestWithParens() throws ParseException {
    String expression = "not (p)";
    TreeNode expected, actual, right;
    parser.ReInit(new StringReader(expression));

    right = new TreeNode(TokenType.PROP, "p");
    expected = new TreeNode(TokenType.NOT, "not", null, right);
    actual = parser.Not();

    assertEquals(expected, actual);
  }

  @Test
  public void ConjuctionTest() throws ParseException {
    String expression = "p1 and p2";
    TreeNode expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new TreeNode(TokenType.PROP, "p1");
    right = new TreeNode(TokenType.PROP, "p2");
    expected = new TreeNode(TokenType.AND, "and", left, right);
    actual = parser.Conjunction();

    assertEquals(expected, actual);
  }


  @Test
  public void DisyunctionTest() throws ParseException {
    String expression = "p1 or p2";
    TreeNode expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new TreeNode(TokenType.PROP, "p1");
    right = new TreeNode(TokenType.PROP, "p2");
    expected = new TreeNode(TokenType.OR, "or", left, right);
    actual = parser.Disyunction();

    assertEquals(expected, actual);
  }

  @Test
  public void ImpliesTest() throws ParseException {
    String expression = "p1 implies p2";
    TreeNode expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new TreeNode(TokenType.PROP, "p1");
    right = new TreeNode(TokenType.PROP, "p2");
    expected = new TreeNode(TokenType.IMPLIES, "implies", left, right);
    actual = parser.Implies();

    assertEquals(expected, actual);
  }

  @Test
  public void KhTest() throws ParseException {
    String expression = "kh(p1, p2)";
    TreeNode expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new TreeNode(TokenType.PROP, "p1");
    right = new TreeNode(TokenType.PROP, "p2");
    expected = new TreeNode(TokenType.KH, "kh", left, right);
    actual = parser.Atom();

    assertEquals(expected, actual);
  }

  @Test
  public void ParensTest() throws ParseException {
    String expression = "(p1 or p2)";
    TreeNode expected, actual, left, right;

    parser.ReInit(new StringReader(expression));

    left = new TreeNode(TokenType.PROP, "p1");
    right = new TreeNode(TokenType.PROP, "p2");
    expected = new TreeNode(TokenType.OR, "or", left, right);
    actual = parser.Atom();

    assertEquals(expected, actual);
  }

  @Test
  public void ExpressionTest() throws ParseException {
    String expression = "kh((a implies b) or c, p and q)";
    TreeNode a, b, c, p, q, implies, or, and, expected, actual;
    parser.ReInit(new StringReader(expression));

    // Build expected TreeNode
    a = new TreeNode(TokenType.PROP, "a");
    b = new TreeNode(TokenType.PROP, "b");
    c = new TreeNode(TokenType.PROP, "c");
    implies = new TreeNode(TokenType.IMPLIES, "implies", a, b);
    or = new TreeNode(TokenType.OR, "or", implies, c);
    p = new TreeNode(TokenType.PROP, "p");
    q = new TreeNode(TokenType.PROP, "q");
    and = new TreeNode(TokenType.AND, "and", p, q);
    expected = new TreeNode(TokenType.KH, "kh", or, and);

    actual = parser.Expression();

    assertEquals(expected, actual);
  }
}