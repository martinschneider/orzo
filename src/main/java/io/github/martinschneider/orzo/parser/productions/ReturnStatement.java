package io.github.martinschneider.orzo.parser.productions;

public class ReturnStatement implements Statement {
  public Expression retValue;

  public ReturnStatement(Expression retValue) {
    this.retValue = retValue;
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + ((retValue == null) ? 0 : retValue.hashCode());
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
    ReturnStatement other = (ReturnStatement) obj;
    if (retValue == null) {
      if (other.retValue != null) {
        return false;
      }
    } else if (!retValue.equals(other.retValue)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "RETURN " + retValue;
  }
}
