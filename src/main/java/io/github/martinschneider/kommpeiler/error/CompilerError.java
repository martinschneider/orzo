package io.github.martinschneider.kommpeiler.error;

public class CompilerError {
  private String msg;

  public CompilerError(final String msg) {
    this.msg = msg;
  }

  public String getMessage() {
    return msg;
  }
}
