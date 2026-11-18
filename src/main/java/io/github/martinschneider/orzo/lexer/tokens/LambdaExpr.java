package io.github.martinschneider.orzo.lexer.tokens;

import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.List;

// A lambda expression (e.g. "x -> x.toString()"), parsed as a single leaf token wherever a value
// is expected (method call argument, assignment RHS, etc.) - same role TernaryExpression plays
// for "a ? b : c". Only an expression body is supported for now, not a "{ ... }" block body.
public class LambdaExpr extends Token {
  // Sentinel "type" for a lambda argument: unlike every other expression kind, a lambda has no
  // type of its own to infer (it's a Java "poly expression" - its type comes FROM the target
  // functional-interface parameter, not the other way around). NumExprTypeDecider.getType()
  // returns this for a lambda so overload-resolution code can recognize "any reference/interface
  // parameter is a match here" instead of failing a normal type-compatibility check.
  public static final String PSEUDO_TYPE = "<lambda>";

  public List<String> params;
  public Expression body;

  public LambdaExpr(List<String> params, Expression body) {
    super("->");
    this.params = params;
    this.body = body;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    LambdaExpr other = (LambdaExpr) obj;
    return params.equals(other.params) && body.equals(other.body);
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + params.hashCode();
    result = prime * result + body.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "(" + String.join(", ", params) + ") -> " + body;
  }
}
