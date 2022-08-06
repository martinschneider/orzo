package io.github.martinschneider.orzo.parser.productions;

import static java.lang.Math.toIntExact;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Str;
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

  public Object getConstantValue(String type) {
    if (tokens.size() == 1) {
      Token token = tokens.get(0);
      if (token instanceof IntLiteral) {
        if (ConstantPool.INT_CONSTANT_TYPES.contains(type)) {
          return toIntExact(((IntLiteral) token).intValue());
        } else if ("long".equals(type)) {
          return ((IntLiteral) token).intValue();
        }
      } else if (token instanceof FPLiteral) {
        if ("float".equals(type)) {
          return Float.valueOf((float) ((FPLiteral) token).doubleVal());
        } else if ("double".equals(type)) {
          return ((FPLiteral) token).doubleVal();
        }
      } else if (token instanceof Str) {
        return ((Str) token).strValue();
      }
    }
    return null;
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
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Expression other = (Expression) obj;
    if (cast == null) {
      if (other.cast != null) return false;
    } else if (!cast.equals(other.cast)) return false;
    if (tokens == null) {
      if (other.tokens != null) return false;
    } else if (!tokens.equals(other.tokens)) return false;
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    if (cast != null) {
      strBuilder.append('(');
      strBuilder.append(cast.name);
      strBuilder.append(')');
    }
    strBuilder.append(tokens.stream().map(Object::toString).collect(Collectors.joining(",")));
    return strBuilder.toString();
  }
}
