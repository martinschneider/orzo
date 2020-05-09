package io.github.martinschneider.kommpeiler.error;

/**
 * Class for compiler error
 *
 * @author Martin Schneider
 */
public class CompilerError {
  /**
   * @param message error-message
   * @param type error-type
   */
  public CompilerError(final String message, final ErrorType type) {
    this.message = message;
    this.type = type;
  }

  public ErrorType getType() {
    return type;
  }

  public void setType(final ErrorType type) {
    this.type = type;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  private ErrorType type;
  private String message;
}
