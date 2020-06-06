package io.github.martinschneider.orzo.parser.productions;

import java.util.List;
import java.util.stream.Collectors;

public class IfStatement implements Statement {
  public List<IfBlock> ifBlks;
  public boolean hasElse;

  public IfStatement(List<IfBlock> ifBlks, boolean hasElse) {
    this.ifBlks = ifBlks;
    this.hasElse = hasElse;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + (hasElse ? 1231 : 1237);
    result = prime * result + ((ifBlks == null) ? 0 : ifBlks.hashCode());
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
    IfStatement other = (IfStatement) obj;
    if (hasElse != other.hasElse) {
      return false;
    }
    if (ifBlks == null) {
      if (other.ifBlks != null) {
        return false;
      }
    } else if (!ifBlks.equals(other.ifBlks)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("IF[");
    strBuilder.append(ifBlks.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("]");
    return strBuilder.toString();
  }
}
