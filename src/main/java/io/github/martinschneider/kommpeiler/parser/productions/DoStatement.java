package io.github.martinschneider.kommpeiler.parser.productions;

import java.util.List;
import java.util.stream.Collectors;

/** DoStatement */
public class DoStatement extends ConditionalStatement {
  private List<Statement> body;
  private Condition condition;

  /**
   * @param condition condition
   * @param body body
   */
  public DoStatement(final Condition condition, final List<Statement> body) {
    this.condition = condition;
    this.body = body;
  }

  /** {@inheritDoc} * */
  @Override
  public List<Statement> getBody() {
    return body;
  }

  /** {@inheritDoc} * */
  @Override
  public Condition getCondition() {
    return condition;
  }

  /** {@inheritDoc} * */
  @Override
  public void setBody(final List<Statement> body) {
    this.body = body;
  }

  /** {@inheritDoc} * */
  @Override
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
    DoStatement other = (DoStatement) obj;
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
    strBuilder.append("do ");
    strBuilder.append(" {");
    strBuilder.append(body.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("}");
    strBuilder.append(" while ");
    strBuilder.append(condition);
    return strBuilder.toString();
  }
}
