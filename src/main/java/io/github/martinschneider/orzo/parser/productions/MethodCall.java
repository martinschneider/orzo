package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Location;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.List;
import java.util.stream.Collectors;

// extending Token is not the most elegant solution but it helps with parsing method calls as part
// of expressions
public class MethodCall extends Token implements Statement {
  public Identifier name;
  public List<Expression> params;

  public MethodCall(Identifier name, List<Expression> params) {
    super(name);
    this.name = name;
    this.params = params;
  }

  public MethodCall wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((params == null) ? 0 : params.hashCode());
    return result;
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
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(name);
    strBuilder.append('(');
    strBuilder.append(params.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append(')');
    return strBuilder.toString();
  }
}
