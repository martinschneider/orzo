package io.github.martinschneider.kommpeiler.error;

public class CompilerError {
  private ErrorType type;
  private String msg;

  public CompilerError(final String msg, final ErrorType type) {
    this.msg = msg;
    this.type = type;
  }

  public ErrorType getType() {
    return type;
  }

  public void setType(final ErrorType type) {
    this.type = type;
  }

  public String getMessage() {
    return msg;
  }

  public void setMessage(final String msg) {
    this.msg = msg;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((msg == null) ? 0 : msg.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
    CompilerError other = (CompilerError) obj;
    if (msg == null) {
      if (other.msg != null) {
        return false;
      }
    } else if (!msg.equals(other.msg)) {
      return false;
    }
    if (type != other.type) {
      return false;
    }
    return true;
  }
}
