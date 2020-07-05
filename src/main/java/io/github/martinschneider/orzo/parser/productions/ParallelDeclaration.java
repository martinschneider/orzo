package io.github.martinschneider.orzo.parser.productions;

import java.util.List;

public class ParallelDeclaration implements Statement, ClassMember {
  public ParallelDeclaration(List<Declaration> declarations) {
    this.declarations = declarations;
  }

  public List<Declaration> declarations;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((declarations == null) ? 0 : declarations.hashCode());
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
    ParallelDeclaration other = (ParallelDeclaration) obj;
    if (declarations == null) {
      if (other.declarations != null) {
        return false;
      }
    } else if (!declarations.equals(other.declarations)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "[declarations=" + declarations + "]";
  }
}
