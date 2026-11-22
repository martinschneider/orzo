package io.github.martinschneider.orzo.lexer.tokens;

import java.math.BigInteger;

public class Token {
  public Object val;
  // the position (1-based, 0 for a token that isn't from the source) and text in the source; not
  // part of equals()
  public int line;
  public int col;
  public String text;

  public Token(Object val) {
    this.val = val;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Token other = (Token) obj;
    if (val == null) {
      if (other.val != null) {
        return false;
      }
    } else if (!val.equals(other.val)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((val == null) ? 0 : val.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return val.toString();
  }

  /** The token as written in the source, e.g. "')'", for error messages. */
  public String display() {
    if (text != null && !text.isBlank()) {
      return "'" + text.strip() + "'";
    }
    if (this instanceof EOF) {
      return "end of file";
    }
    if (val instanceof Symbols) {
      return "'" + SYMBOLS.getOrDefault(val, val.toString()) + "'";
    }
    if (val instanceof Operators && OPERATORS.containsKey(val)) {
      return "'" + OPERATORS.get(val) + "'";
    }
    return "'" + val + "'";
  }

  // (for an operator without source text, e.g. one inserted by the parser)
  private static final java.util.Map<Operators, String> OPERATORS =
      java.util.Map.ofEntries(
          java.util.Map.entry(Operators.ASSIGN, "="),
          java.util.Map.entry(Operators.PLUS_ASSIGN, "+="),
          java.util.Map.entry(Operators.MINUS_ASSIGN, "-="),
          java.util.Map.entry(Operators.TIMES_ASSIGN, "*="),
          java.util.Map.entry(Operators.DIV_ASSIGN, "/="),
          java.util.Map.entry(Operators.MOD_ASSIGN, "%="),
          java.util.Map.entry(Operators.LSHIFT_ASSIGN, "<<="),
          java.util.Map.entry(Operators.RSHIFT_ASSIGN, ">>="),
          java.util.Map.entry(Operators.RSHIFTU_ASSIGN, ">>>="),
          java.util.Map.entry(Operators.BITWISE_OR_ASSIGN, "|="),
          java.util.Map.entry(Operators.BITWISE_XOR_ASSIGN, "^="),
          java.util.Map.entry(Operators.BITWISE_AND_ASSIGN, "&="),
          java.util.Map.entry(Operators.LOGICAL_OR, "||"),
          java.util.Map.entry(Operators.LOGICAL_AND, "&&"),
          java.util.Map.entry(Operators.BITWISE_OR, "|"),
          java.util.Map.entry(Operators.BITWISE_XOR, "^"),
          java.util.Map.entry(Operators.BITWISE_AND, "&"),
          java.util.Map.entry(Operators.EQUAL, "=="),
          java.util.Map.entry(Operators.NOTEQUAL, "!="),
          java.util.Map.entry(Operators.GREATER, ">"),
          java.util.Map.entry(Operators.LESS, "<"),
          java.util.Map.entry(Operators.LESSEQ, "<="),
          java.util.Map.entry(Operators.GREATEREQ, ">="),
          java.util.Map.entry(Operators.MINUS, "-"),
          java.util.Map.entry(Operators.PLUS, "+"),
          java.util.Map.entry(Operators.TIMES, "*"),
          java.util.Map.entry(Operators.POW, "**"),
          java.util.Map.entry(Operators.DIV, "/"),
          java.util.Map.entry(Operators.REAL_DIV, "÷"),
          java.util.Map.entry(Operators.MOD, "%"),
          java.util.Map.entry(Operators.LSHIFT, "<<"),
          java.util.Map.entry(Operators.RSHIFT, ">>"),
          java.util.Map.entry(Operators.RSHIFTU, ">>>"),
          java.util.Map.entry(Operators.NEGATE, "!"),
          java.util.Map.entry(Operators.BITWISE_NOT, "~"),
          java.util.Map.entry(Operators.PRE_INCREMENT, "++"),
          java.util.Map.entry(Operators.PRE_DECREMENT, "--"),
          java.util.Map.entry(Operators.POST_INCREMENT, "++"),
          java.util.Map.entry(Operators.POST_DECREMENT, "--"));

  private static final java.util.Map<Symbols, String> SYMBOLS =
      java.util.Map.ofEntries(
          java.util.Map.entry(Symbols.LPAREN, "("),
          java.util.Map.entry(Symbols.RPAREN, ")"),
          java.util.Map.entry(Symbols.LBRACE, "{"),
          java.util.Map.entry(Symbols.RBRACE, "}"),
          java.util.Map.entry(Symbols.LBRAK, "["),
          java.util.Map.entry(Symbols.RBRAK, "]"),
          java.util.Map.entry(Symbols.DOT, "."),
          java.util.Map.entry(Symbols.COMMA, ","),
          java.util.Map.entry(Symbols.SEMICOLON, ";"),
          java.util.Map.entry(Symbols.COLON, ":"),
          java.util.Map.entry(Symbols.QUESTION, "?"),
          java.util.Map.entry(Symbols.ARROW, "->"));

  public static Chr chr(char val) {
    return new Chr(val);
  }

  public static Str str(String val) {
    return new Str(val);
  }

  public static IntLiteral integer(Integer val) {
    return new IntLiteral(BigInteger.valueOf(val), false);
  }

  public static IntLiteral integer(String val) {
    return integer(val, false);
  }

  public static IntLiteral integer(String val, boolean isLong) {
    // Handle hexadecimal literals
    if (val.startsWith("0x") || val.startsWith("0X")) {
      String hexValue = val.substring(2); // Remove 0x prefix
      if (hexValue.isEmpty()) {
        throw new NumberFormatException("Invalid hexadecimal literal: " + val);
      }
      BigInteger value = new BigInteger(hexValue, 16);
      // A hex literal denotes the bits of a 32-bit int (64-bit long), e.g. 0xCAFEBABE is the
      // negative int -889275714, not the long 3405691582.
      if (!isLong && value.bitLength() <= 32) {
        value = BigInteger.valueOf(value.intValue());
      } else if (isLong && value.bitLength() <= 64) {
        value = BigInteger.valueOf(value.longValue());
      }
      return new IntLiteral(value, isLong);
    }
    return new IntLiteral(new BigInteger(val), isLong);
  }

  public static FPLiteral fp(double val, boolean isFloat) {
    return new FPLiteral(val, isFloat);
  }

  public static FPLiteral fp(String val, boolean isFloat) {
    return new FPLiteral(Double.valueOf(val), isFloat);
  }

  public static FPLiteral fp(double val) {
    return new FPLiteral(val, false);
  }

  public static FPLiteral fp(String val) {
    return new FPLiteral(Double.valueOf(val), false);
  }

  public static Identifier id(String val) {
    return new Identifier(val);
  }

  public static Operator op(Operators val) {
    return new Operator(val);
  }

  public static Keyword keyword(Keywords val) {
    return new Keyword(val);
  }

  public static Keyword keyword(String val) {
    return new Keyword(Keywords.valueOf(val.toUpperCase()));
  }

  public static Scope scope(Scopes val) {
    return new Scope(val);
  }

  public static Sym sym(Symbols val) {
    return new Sym(val);
  }

  public static Type type(String val) {
    return new Type(val);
  }

  public static BoolLiteral bool(String val) {
    return new BoolLiteral(Boolean.parseBoolean(val));
  }

  public static Token eof() {
    return new EOF();
  }

  public <T extends Token> boolean eq(T val) {
    return equals(val);
  }

  public <T extends Token> boolean eq(String val) {
    return this.val.equals(val);
  }
}
