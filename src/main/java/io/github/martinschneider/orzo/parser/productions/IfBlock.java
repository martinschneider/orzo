package io.github.martinschneider.orzo.parser.productions;

import java.util.List;
import java.util.stream.Collectors;

public class IfBlock {
  private List<Statement> body;
  private Condition condition;

  public IfBlock(final Condition condition, final List<Statement> body) {
    super();
    this.condition = condition;
    this.body = body;
  }

  public List<Statement> getBody() {
    return body;
  }

  public Condition getCondition() {
    return condition;
  }

  public void setBody(final List<Statement> body) {
    this.body = body;
  }

  public void setCondition(final Condition condition) {
    this.condition = condition;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((body == null) ? 0 : body.hashCode());
    result = prime * result + ((condition == null) ? 0 : condition.hashCode());
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
    IfBlock other = (IfBlock) obj;
    if (body == null) {
      if (other.body != null) {
        return false;
      }
    } else if (!body.equals(other.body)) {
      return false;
    }
    if (condition == null) {
      if (other.condition != null) {
        return false;
      }
    } else if (!condition.equals(other.condition)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    if (condition == null) {
      strBuilder.append("else{");
    } else {
      strBuilder.append("if(");
      strBuilder.append(condition);
      strBuilder.append("){");
    }
    strBuilder.append(body.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("}");
    return strBuilder.toString();
  }
}
