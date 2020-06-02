package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Expression {
  public Expression() {
    tokens = new ArrayList<>();
  }

  public Expression(Token token) {
    new Expression();
    tokens.add(token);
  }

  public Expression(List<Token> tokens) {
    this.tokens = tokens;
  }

  private List<Token> tokens = new ArrayList<>();

  public void addToken(Token token) {
    tokens.add(token);
  }

  public Token getLast() {
    if (tokens.isEmpty()) {
      return null;
    }
    return tokens.get(tokens.size() - 1);
  }

  public List<Token> getInfix() {
    return tokens;
  }

  public int size() {
    return tokens.size();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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
