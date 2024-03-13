package lkh.parser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ParserTest {
    private static InputStream stream;
    private static Parser parser;
    private static TreeNode tree;

    @BeforeAll
    public static void setUp() throws FileNotFoundException {
        stream = new FileInputStream("src/prueba.txt");
        parser = new Parser(stream);
    }

    @Test
    public void atomTest() throws ParseException {
        tree = parser.Atom();
        System.out.println();
    }

    @Test
    public void notTest() throws ParseException {
        tree = parser.Not();
        System.out.println();
    }

    @Test
    public void ConjuctionTest() throws ParseException {
        tree = parser.Conjunction();
        System.out.println();
    }

    @Test
    public void DisyunctionTest() throws ParseException {
        tree = parser.Disyunction();
        System.out.println();
    }

    @Test
    public void ImpliesTest() throws ParseException {
        tree = parser.Implies();
        System.out.println();
    }

    @Test
    public void ExpressionTest() throws ParseException {
        tree = parser.Expression();
        System.out.println();
    }
}