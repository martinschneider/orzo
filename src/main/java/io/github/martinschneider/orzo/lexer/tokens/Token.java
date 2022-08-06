package io.github.martinschneider.orzo.lexer.tokens;

import java.math.BigInteger;

public class Token {
  public Object val;
  public Location loc;

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
    return new IntLiteral(new BigInteger(val), false);
  }

  public static IntLiteral integer(String val, boolean isLong) {
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
