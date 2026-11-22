package io.github.martinschneider.orzo.error;

import java.util.Objects;

/** A compile error at a position in a source file ("A.java:12:5: error: ..."). */
public class CompilerError {
  public final String file;
  // 1-based; 0 if unknown
  public final int line;
  public final int col;
  // the compiler component that reported it, e.g. "parse while" (shown in verbose mode)
  public final String source;
  public final String msg;
  public final StackTraceElement[] trace;

  public CompilerError(
      String file, int line, int col, String source, String msg, StackTraceElement[] trace) {
    this.file = file;
    this.line = line;
    this.col = col;
    this.source = source;
    this.msg = msg;
    this.trace = trace;
  }

  public CompilerError(String msg, StackTraceElement[] trace) {
    this(null, 0, 0, null, msg, trace);
  }

  @Override
  public String toString() {
    StringBuilder str = new StringBuilder();
    if (file != null) {
      str.append(file).append(':');
    }
    if (line > 0) {
      str.append(line).append(':').append(col).append(':');
    }
    if (str.length() > 0) {
      str.append(' ');
    }
    return str.append("error: ").append(msg).toString();
  }

  // equals and hashCode do NOT consider the stack trace
  @Override
  public int hashCode() {
    return Objects.hash(file, line, col, msg);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    CompilerError other = (CompilerError) obj;
    return Objects.equals(file, other.file)
        && line == other.line
        && col == other.col
        && Objects.equals(msg, other.msg);
  }
}
