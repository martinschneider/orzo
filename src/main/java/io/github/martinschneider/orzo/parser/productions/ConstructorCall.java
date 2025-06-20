package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import java.util.List;

public class ConstructorCall extends Identifier implements Statement {

  public List<Expression> args;

  public ConstructorCall(String type, List<Expression> args) {
    super(type);
    this.args = args;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((args == null) ? 0 : args.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    ConstructorCall other = (ConstructorCall) obj;
    if (args == null) {
      if (other.args != null) return false;
    } else if (!args.equals(other.args)) return false;
    return true;
  }

  @Override
  public String toString() {
    return "new " + val + "(" + args + ")";
  }
}
