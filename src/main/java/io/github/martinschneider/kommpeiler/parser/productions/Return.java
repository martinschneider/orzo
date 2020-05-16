package io.github.martinschneider.kommpeiler.parser.productions;

public class Return implements Statement {
  private Expression retValue;

  public Expression getRetValue() {
    return retValue;
  }

  public void setRetValue(Expression retValue) {
    this.retValue = retValue;
  }

  public Return(Expression retValue) {
    super();
    this.retValue = retValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
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
    Return other = (Return) obj;
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
