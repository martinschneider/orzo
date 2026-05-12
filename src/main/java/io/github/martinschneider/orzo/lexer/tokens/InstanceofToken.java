package io.github.martinschneider.orzo.lexer.tokens;

public class InstanceofToken extends Token {
  public String targetType;

  public InstanceofToken(String targetType) {
    super("instanceof " + targetType);
    this.targetType = targetType;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    InstanceofToken other = (InstanceofToken) obj;
    return targetType.equals(other.targetType);
  }

  @Override
  public int hashCode() {
    return targetType.hashCode();
  }

  @Override
  public String toString() {
    return "instanceof " + targetType;
  }
}
