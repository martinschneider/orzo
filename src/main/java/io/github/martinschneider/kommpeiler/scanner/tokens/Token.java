package io.github.martinschneider.kommpeiler.scanner.tokens;

import io.github.martinschneider.kommpeiler.parser.productions.BasicType;
import io.github.martinschneider.kommpeiler.parser.productions.Type;

public class Token {
  private Object value;

  public Token(final Object value) {
    this.value = value;
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
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  public void setValue(final Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  public static Token of(Object value) {
    return new Token(value);
  }

  public static Str str(String value) {
    return new Str(value);
  }

  public static IntNum integer(Integer value) {
    return new IntNum(value);
  }

  public static IntNum integer(String value) {
    return new IntNum(Integer.valueOf(value));
  }

  public static DoubleNum fp(double value) {
    return new DoubleNum(value);
  }

  public static DoubleNum fp(String value) {
    return new DoubleNum(Double.valueOf(value));
  }

  public static Identifier id(String value) {
    return new Identifier(value);
  }

  public static Operator op(Operators value) {
    return new Operator(value);
  }

  public static Comparator cmp(Comparators value) {
    return new Comparator(value);
  }

  public static Keyword keyword(Keywords value) {
    return new Keyword(value);
  }

  public static Keyword keyword(String value) {
    return new Keyword(Keywords.valueOf(value.toUpperCase()));
  }

  public static Scope scope(Scopes value) {
    return new Scope(value);
  }

  public static Type type(String value) {
    return new Type(value);
  }

  public static Type type(BasicType value) {
    return new Type(value.name());
  }

  public static Sym sym(Symbols value) {
    return new Sym(value);
  }

  public static Token eof() {
    return new EOF();
  }

  public <T extends Token> boolean eq(T right) {
    return this.getValue().equals(right.getValue());
  }
}
