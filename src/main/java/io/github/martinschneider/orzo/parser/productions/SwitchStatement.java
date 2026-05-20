package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;
import java.util.List;

public class SwitchStatement implements Statement {
  public Expression switchExpr;
  public List<SwitchCase> cases;

  public SwitchStatement(Expression switchExpr, List<SwitchCase> cases) {
    this.switchExpr = switchExpr;
    this.cases = cases;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return ObjectUtils.equals(this, obj);
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
