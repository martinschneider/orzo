package io.github.martinschneider.orzo.lexer.tokens;

import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
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
  // a block body ("x -> { ... }"), or null for an expression body
  public List<Statement> block;
  // the declared parameter types, e.g. "String" for "(String s) -> ..." (null for inferred ones)
  public List<String> paramTypes;
  // a method reference, e.g. "Math::min" (owner "Math", method "min"): expanded into a lambda once
  // the target functional interface's arity is known (params and body are empty until then)
  public String methodRefOwner;
  public String methodRefName;

  public LambdaExpr(List<String> params, Expression body) {
    super("->");
    this.params = params;
    this.body = body;
  }

  public static LambdaExpr methodRef(String owner, String method) {
    LambdaExpr ref = new LambdaExpr(new ArrayList<>(), new Expression(new ArrayList<>()));
    ref.methodRefOwner = owner;
    ref.methodRefName = method;
    return ref;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    LambdaExpr other = (LambdaExpr) obj;
    return params.equals(other.params)
        && body.equals(other.body)
        && java.util.Objects.equals(block, other.block)
        && java.util.Objects.equals(methodRefOwner, other.methodRefOwner)
        && java.util.Objects.equals(methodRefName, other.methodRefName);
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
    if (methodRefName != null) {
      return methodRefOwner + "::" + methodRefName;
    }
    return "(" + String.join(", ", params) + ") -> " + body;
  }
}
