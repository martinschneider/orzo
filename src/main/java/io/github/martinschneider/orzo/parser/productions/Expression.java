package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Expression {
  public List<Token> tokens = new ArrayList<>();
  public Type cast;

  public Expression() {
    tokens = new ArrayList<>();
  }

  public Expression(List<Token> tokens) {
    this.tokens = tokens;
  }

  public Expression(List<Token> tokens, Type cast) {
    this.tokens = tokens;
    this.cast = cast;
  }

  public Token getLast() {
    if (tokens.isEmpty()) {
      return null;
    }
    return tokens.get(tokens.size() - 1);
  }

  public int size() {
    return tokens.size();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((cast == null) ? 0 : cast.hashCode());
    result = prime * result + ((tokens == null) ? 0 : tokens.hashCode());
    return result;
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
    Expression other = (Expression) obj;
    if (cast == null) {
      if (other.cast != null) {
        return false;
      }
    } else if (!cast.equals(other.cast)) {
      return false;
    }
    if (tokens == null) {
      if (other.tokens != null) {
        return false;
      }
    } else if (!tokens.equals(other.tokens)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return tokens.stream().map(Object::toString).collect(Collectors.joining(","));
  }
}
