package io.github.martinschneider.orzo.parser.tree;

import io.github.martinschneider.orzo.lexer.tokens.Token;

/** A literal, e.g. "1", "2.5f", "true", "'c'" or "\"s\"". */
public class LiteralNode extends ExprNode {
  public final Token value;

  public LiteralNode(Token value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
