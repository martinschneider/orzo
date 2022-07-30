package io.github.martinschneider.orzo.error;

import java.util.Objects;

public class CompilerError {
  @Override
  public int hashCode() {
    return Objects.hash(msg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CompilerError other = (CompilerError) obj;
    return Objects.equals(msg, other.msg);
  }

  public String msg;

  public CompilerError(String msg) {
    this.msg = msg;
  }

  public String toString() {
    return msg;
  }
}
