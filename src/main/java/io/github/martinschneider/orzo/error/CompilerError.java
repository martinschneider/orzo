package io.github.martinschneider.orzo.error;

import java.util.Objects;

public class CompilerError {

  public String msg;

  public StackTraceElement[] trace;

  public CompilerError(String msg, StackTraceElement[] trace) {
    this.msg = msg;
    this.trace = trace;
  }

  public String toString() {
    return msg;
  }

  @Override
  public int hashCode() {
    return Objects.hash(msg);
  }

  // equals and hashCode do NOT consider the stack trace
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CompilerError other = (CompilerError) obj;
    return Objects.equals(msg, other.msg);
  }
}
