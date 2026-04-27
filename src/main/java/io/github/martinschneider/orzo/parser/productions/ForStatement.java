package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

public class ForStatement extends LoopStatement {
  public Statement init;
  public Statement loopStmt;

  public ForStatement(Statement init, Expression cond, Statement loopStmt, List<Statement> body) {
    super.body = body;
    super.cond = cond;
    this.init = init;
    this.loopStmt = loopStmt;
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
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ForStatement other = (ForStatement) obj;
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
    return ObjectUtils.toString(this);
  }
}
