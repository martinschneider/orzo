package io.github.martinschneider.orzo.parser.productions;

import java.util.List;
import java.util.stream.Collectors;

public class IfStatement implements Statement {
  private List<IfBlock> ifBlocks;
  private boolean hasElseBlock;

  public IfStatement(List<IfBlock> ifBlocks, boolean hasElseBlock) {
    super();
    this.ifBlocks = ifBlocks;
    this.hasElseBlock = hasElseBlock;
  }

  public List<IfBlock> getIfBlocks() {
    return ifBlocks;
  }

  public void setIfBlocks(List<IfBlock> ifBlocks) {
    this.ifBlocks = ifBlocks;
  }

  public boolean isHasElseBlock() {
    return hasElseBlock;
  }

  public void setHasElseBlock(boolean hasElseBlock) {
    this.hasElseBlock = hasElseBlock;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (hasElseBlock ? 1231 : 1237);
    result = prime * result + ((ifBlocks == null) ? 0 : ifBlocks.hashCode());
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
    if (hasElseBlock != other.hasElseBlock) {
      return false;
    }
    if (ifBlocks == null) {
      if (other.ifBlocks != null) {
        return false;
      }
    } else if (!ifBlocks.equals(other.ifBlocks)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    strBuilder.append("IF[");
    strBuilder.append(ifBlocks.stream().map(x -> x.toString()).collect(Collectors.joining(", ")));
    strBuilder.append("]");
    return strBuilder.toString();
  }
}
