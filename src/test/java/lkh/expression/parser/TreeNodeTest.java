package lkh.expression.parser;

import lkh.expression.TreeNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class TreeNodeTest {
  private static InputStream stream;
  private static Parser parser;
  private static TreeNode tree;

  @BeforeAll
  public static void setUp() throws FileNotFoundException {
    stream = new FileInputStream("src/prueba.txt");
    parser = new Parser(stream);
  }

  @Test
  public void ExpressionTest() throws ParseException {
    tree = parser.Expression();
    tree.toDotFile("prueba.dot");
    System.out.println();
  }
}
