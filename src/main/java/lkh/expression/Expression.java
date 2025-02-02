package lkh.expression;

import lkh.expression.parser.ParseException;
import lkh.expression.parser.Parser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Expression {
  private ExpressionType tokenType;
  private String name;
  private Expression left;
  private Expression right;
  private int size;

  public Expression(ExpressionType tokenType, String name) {
    this(tokenType, name, null, null);
  }

  public Expression(ExpressionType tokenType, String name, Expression left, Expression right) {
    this.tokenType = tokenType;
    this.name=name;
    this.left=left;
    this.right=right;

    this.size = 1;
    if (left != null)
      this.size += left.size;
    if (right != null)
      this.size += right.size;
  }

  public static Expression kh(Expression left, Expression right) {
    return new Expression(ExpressionType.KH, "kh", left, right);
  }

  public static Expression implies(Expression left, Expression right) {
    return new Expression(ExpressionType.IMPLIES, "implies", left, right);
  }

  public static Expression or(Expression left, Expression right) {
    return new Expression(ExpressionType.OR, "or", left, right);
  }

  public static Expression and(Expression left, Expression right) {
    return new Expression(ExpressionType.AND, "and", left, right);
  }

  public static Expression and(Expression ...expressions) {
    if (expressions == null || expressions.length == 0)
      throw new IllegalArgumentException("pass at least one expression");

    return Arrays.stream(expressions).reduce(Expression::and).get();
  }

  public static Expression not(Expression expr) {
    return new Expression(ExpressionType.NOT, "not", null, expr);
  }

  public static Expression prop(String name) {
    return new Expression(ExpressionType.PROP, name);
  }

  public Expression not() {
    return not(this);
  }

  public static Expression of(String expression) throws ParseException {
    Parser parser = new Parser(new StringReader(expression));
    return parser.Expression();
  }

  public void toDotFile(String filename) {
    try {
      PrintWriter f = new PrintWriter(filename);
      f.print("digraph Tree{\n");
      writeSons(f, this, 0);
      f.print("}");
      f.close();
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  private void writeSons(PrintWriter f, Expression tree, int id) {
    writeLabel(f,tree, id);
    if(tree.left != null){
      int left_id = id + 1;
      f.printf("%d -> %d;\n", id, left_id);
      writeSons(f, tree.left, left_id);
    }
    if(tree.right != null){
      int right_id = id + 1;
      if (tree.left != null)
        right_id += tree.left.size;
      f.printf("%d -> %d;\n", id, right_id);
      writeSons(f, tree.right, right_id);
    }
  }

  private void writeLabel(PrintWriter f, Expression tree, int id) {
    f.printf("%d [label=\"%s\"];\n", id, tree.name);
  }

}
