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

  public static Token of(Object val) {
    return new Token(val);
  }

  public static Chr chr(char val) {
    return new Chr(val);
  }

  public static Str str(String val) {
    return new Str(val);
  }

  public static IntNum integer(Long val) {
    return new IntNum(BigInteger.valueOf(val), true);
  }

  public static IntNum integer(Integer val) {
    return new IntNum(BigInteger.valueOf(val), false);
  }

  public static IntNum integer(BigInteger val, boolean isLong) {
    return new IntNum(val, isLong);
  }

  public static IntNum integer(BigInteger val) {
    return new IntNum(val, false);
  }

  public static IntNum integer(String val) {
    return new IntNum(new BigInteger(val), false);
  }

  public static IntNum integer(String val, boolean isLong) {
    return new IntNum(new BigInteger(val), isLong);
  }

  public static DoubleNum fp(double val, boolean isFloat) {
    return new DoubleNum(val, isFloat);
  }

  public static DoubleNum fp(String val, boolean isFloat) {
    return new DoubleNum(Double.valueOf(val), isFloat);
  }

  public static DoubleNum fp(double val) {
    return new DoubleNum(val, false);
  }

  public static DoubleNum fp(String val) {
    return new DoubleNum(Double.valueOf(val), false);
  }

  public static Identifier id(String val) {
    return new Identifier(val);
  }

  public static Operator op(Operators val) {
    return new Operator(val);
  }

  public static Comparator cmp(Comparators val) {
    return new Comparator(val);
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
