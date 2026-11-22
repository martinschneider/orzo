package io.github.martinschneider.orzo.parser.tree;

import java.util.List;

/**
 * A node of an expression tree, as built by the ExpressionParser: e.g. "a + b * c" is the
 * BinaryNode "+" of the NameNode "a" and the BinaryNode "b * c". The nodes are immutable (and don't
 * share any state with the tokens they were parsed from).
 */
public class ExprNode {
  // e.g. "(a, b)" (for toString())
  static String arguments(List<ExprNode> args) {
    StringBuilder str = new StringBuilder("(");
    for (int i = 0; i < args.size(); i++) {
      if (i > 0) {
        str.append(", ");
      }
      str.append(args.get(i));
    }
    return str.append(')').toString();
  }
}
