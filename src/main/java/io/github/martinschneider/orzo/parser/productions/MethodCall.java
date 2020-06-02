package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Location;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.List;
import java.util.stream.Collectors;

// extending Token is not the most elegant solution but it helps with parsing method calls as part
// of expressions
public class MethodCall extends Token implements Statement {
  private Identifier name;
  private List<Expression> parameters;

  public MethodCall(final Identifier name, final List<Expression> parameters) {
    super(name);
    this.name = name;
    this.parameters = parameters;
  }

  public MethodCall wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  public Identifier getName() {
    return name;
  }

  public List<Expression> getParameters() {
    return parameters;
  }

  public void setName(final Identifier name) {
    this.name = name;
  }

  public void setParameters(final List<Expression> parameters) {
    this.parameters = parameters;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
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
    if (parameters == null) {
      if (other.parameters != null) {
        return false;
      }
    } else if (!parameters.equals(other.parameters)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(name);
    strBuilder.append('(');
    strBuilder.append(parameters.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append(')');
    return strBuilder.toString();
  }
}
