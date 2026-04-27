package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

// extending Token is not the most elegant solution but it helps with parsing method calls as part
// of expressions
public class MethodCall extends Identifier implements Statement {
  public String name;
  public List<Expression> params;

  public MethodCall(String name, List<Expression> params) {
    super(name, null);
    this.name = name;
    this.params = params;
  }

  public MethodCall(String name, List<Expression> params, ArraySelector arrSel) {
    this(name, params);
    this.arrSel = arrSel;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MethodCall other = (MethodCall) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (params == null) {
      if (other.params != null) {
        return false;
      }
    } else if (!params.equals(other.params)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
