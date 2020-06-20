package io.github.martinschneider.orzo.lexer.tokens;

public class BoolLiteral extends Token {
  public BoolLiteral(boolean val) {
    super(val);
  }

  public BoolLiteral wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder("BOOL(");
    strBuilder.append(val);
    strBuilder.append(')');
    return strBuilder.toString();
  }
}
