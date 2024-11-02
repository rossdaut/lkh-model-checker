/* Parser.java */
/* Generated By:JavaCC: Do not edit this line. Parser.java */
package lkh.expression.parser;

import lkh.expression.Expression;
import java.util.List;
import java.util.LinkedList;

public class Parser implements ParserConstants {

  final public Expression Expression() throws ParseException {Expression t;
    t = Implies();
    jj_consume_token(0);
{if ("" != null) return t;}
    throw new Error("Missing return statement in function");
}

  final public Expression Implies() throws ParseException {Expression left, right;
    left = Disyunction();
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case IMPLIES:{
      jj_consume_token(IMPLIES);
      right = Implies();
left = new Expression(TokenType.IMPLIES, "implies", left, right);
      break;
      }
    default:
      jj_la1[0] = jj_gen;
      ;
    }
{if ("" != null) return left;}
    throw new Error("Missing return statement in function");
}

  final public Expression Disyunction() throws ParseException {Expression left, right;
    left = Conjunction();
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case OR:{
        ;
        break;
        }
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      jj_consume_token(OR);
      right = Conjunction();
left = new Expression(TokenType.OR, "or", left, right);
    }
{if ("" != null) return left;}
    throw new Error("Missing return statement in function");
}

  final public Expression Conjunction() throws ParseException {Expression left, right;
    left = Not();
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case AND:{
        ;
        break;
        }
      default:
        jj_la1[2] = jj_gen;
        break label_2;
      }
      jj_consume_token(AND);
      right = Not();
left = new Expression(TokenType.AND, "and", left, right);
    }
{if ("" != null) return left;}
    throw new Error("Missing return statement in function");
}

  final public Expression Not() throws ParseException {Expression t;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case NOT:{
      jj_consume_token(NOT);
      t = Atom();
{if ("" != null) return new Expression(TokenType.NOT, "not", null, t);}
      break;
      }
    case KH:
    case ID:
    case 11:{
      t = Atom();
{if ("" != null) return t;}
      break;
      }
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

  final public Expression Atom() throws ParseException {Token t;
    List<String> l = new LinkedList<>();
    Expression left, right;
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case ID:{
      t = jj_consume_token(ID);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 11:{
        jj_consume_token(11);
        ArgsList(l);
        jj_consume_token(12);
        break;
        }
      default:
        jj_la1[4] = jj_gen;
        ;
      }
if (l.isEmpty())
            {if ("" != null) return new Expression(TokenType.PROP, t.image);}

        {if ("" != null) return new Expression(TokenType.PROP, t.image + "(" + String.join(", ", l) + ")");}
      break;
      }
    case 11:{
      jj_consume_token(11);
      left = Implies();
      jj_consume_token(12);
{if ("" != null) return left;}
      break;
      }
    case KH:{
      jj_consume_token(KH);
      jj_consume_token(11);
      left = Implies();
      jj_consume_token(13);
      right = Implies();
      jj_consume_token(12);
{if ("" != null) return new Expression(TokenType.KH, "kh", left, right);}
      break;
      }
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
}

  final public void ArgsList(List<String> argsList) throws ParseException {Token t;
    t = jj_consume_token(ID);
argsList.add(t.image);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 13:{
      jj_consume_token(13);
      ArgsList(argsList);
      break;
      }
    default:
      jj_la1[6] = jj_gen;
      ;
    }
}

  /** Generated Token Manager. */
  public ParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[7];
  static private int[] jj_la1_0;
  static {
	   jj_la1_init_0();
	}
	private static void jj_la1_init_0() {
	   jj_la1_0 = new int[] {0x20,0x40,0x80,0xf00,0x800,0xe00,0x2000,};
	}

  /** Constructor with InputStream. */
  public Parser(java.io.InputStream stream) {
	  this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public Parser(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source = new ParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
	  ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public Parser(java.io.Reader stream) {
	 jj_input_stream = new SimpleCharStream(stream, 1, 1);
	 token_source = new ParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
	if (jj_input_stream == null) {
	   jj_input_stream = new SimpleCharStream(stream, 1, 1);
	} else {
	   jj_input_stream.ReInit(stream, 1, 1);
	}
	if (token_source == null) {
 token_source = new ParserTokenManager(jj_input_stream);
	}

	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public Parser(ParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(ParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
	 Token oldToken;
	 if ((oldToken = token).next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 if (token.kind == kind) {
	   jj_gen++;
	   return token;
	 }
	 token = oldToken;
	 jj_kind = kind;
	 throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
	 if (token.next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 jj_gen++;
	 return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
	 Token t = token;
	 for (int i = 0; i < index; i++) {
	   if (t.next != null) t = t.next;
	   else t = t.next = token_source.getNextToken();
	 }
	 return t;
  }

  private int jj_ntk_f() {
	 if ((jj_nt=token.next) == null)
	   return (jj_ntk = (token.next=token_source.getNextToken()).kind);
	 else
	   return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
	 jj_expentries.clear();
	 boolean[] la1tokens = new boolean[14];
	 if (jj_kind >= 0) {
	   la1tokens[jj_kind] = true;
	   jj_kind = -1;
	 }
	 for (int i = 0; i < 7; i++) {
	   if (jj_la1[i] == jj_gen) {
		 for (int j = 0; j < 32; j++) {
		   if ((jj_la1_0[i] & (1<<j)) != 0) {
			 la1tokens[j] = true;
		   }
		 }
	   }
	 }
	 for (int i = 0; i < 14; i++) {
	   if (la1tokens[i]) {
		 jj_expentry = new int[1];
		 jj_expentry[0] = i;
		 jj_expentries.add(jj_expentry);
	   }
	 }
	 int[][] exptokseq = new int[jj_expentries.size()][];
	 for (int i = 0; i < jj_expentries.size(); i++) {
	   exptokseq[i] = jj_expentries.get(i);
	 }
	 return new ParseException(token, exptokseq, tokenImage);
  }

  private boolean trace_enabled;

/** Trace enabled. */
  final public boolean trace_enabled() {
	 return trace_enabled;
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

                     }
