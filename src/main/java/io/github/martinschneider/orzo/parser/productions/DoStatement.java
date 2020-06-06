package io.github.martinschneider.orzo.parser.productions;

import java.util.List;
import java.util.stream.Collectors;

public class DoStatement extends LoopStatement {
  public DoStatement(Condition condition, List<Statement> body) {
    super.cond = condition;
    super.body = body;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + ((cond == null) ? 0 : cond.hashCode());
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
    DoStatement other = (DoStatement) obj;
    if (body == null) {
      if (other.body != null) {
        return false;
      }
    } else if (!body.equals(other.body)) {
      return false;
    }
    if (cond == null) {
      if (other.cond != null) {
        return false;
      }
    } else if (!cond.equals(other.cond)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("do ");
    strBuilder.append(" {");
    strBuilder.append(body.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("}");
    strBuilder.append(" while ");
    strBuilder.append(cond);
    return strBuilder.toString();
  }
}
