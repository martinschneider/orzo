package io.github.martinschneider.orzo.parser.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * A copy of a tree with a name replaced, e.g. "this" by the enclosing instance "this$0" of a lambda
 * body (not in nested lambdas or anonymous class bodies, where it is another object).
 */
public class Renaming {
  private final String from;
  private final String to;

  public Renaming(String from, String to) {
    this.from = from;
    this.to = to;
  }

  public ExprNode apply(ExprNode node) {
    if (node instanceof NameNode) {
      NameNode name = (NameNode) node;
      return from.equals(name.name) ? new NameNode(to, name.typeName) : name;
    } else if (node instanceof SelectNode) {
      SelectNode select = (SelectNode) node;
      return new SelectNode(apply(select.target), select.name, select.typeName);
    } else if (node instanceof CallNode) {
      CallNode call = (CallNode) node;
      return new CallNode(
          (call.target != null) ? apply(call.target) : null, call.name, apply(call.args));
    } else if (node instanceof IndexNode) {
      IndexNode index = (IndexNode) node;
      return new IndexNode(apply(index.target), apply(index.index));
    } else if (node instanceof NewNode) {
      NewNode creation = (NewNode) node;
      NewNode copy = new NewNode(creation.type, apply(creation.args));
      copy.body = creation.body;
      copy.outer = (creation.outer != null) ? apply(creation.outer) : null;
      return copy;
    } else if (node instanceof BinaryNode) {
      BinaryNode binary = (BinaryNode) node;
      return new BinaryNode(binary.op, apply(binary.left), apply(binary.right));
    } else if (node instanceof UnaryNode) {
      UnaryNode unary = (UnaryNode) node;
      return new UnaryNode(unary.op, apply(unary.operand));
    } else if (node instanceof TernaryNode) {
      TernaryNode ternary = (TernaryNode) node;
      return new TernaryNode(
          apply(ternary.condition), apply(ternary.ifTrue), apply(ternary.ifFalse));
    } else if (node instanceof CastNode) {
      CastNode cast = (CastNode) node;
      return new CastNode(cast.type, apply(cast.operand));
    } else if (node instanceof InstanceOfNode) {
      InstanceOfNode test = (InstanceOfNode) node;
      return new InstanceOfNode(apply(test.operand), test.type);
    }
    return node; // (a literal, e.g. a nested lambda, or an opaque expression)
  }

  private List<ExprNode> apply(List<ExprNode> nodes) {
    List<ExprNode> copies = new ArrayList<>();
    for (ExprNode node : nodes) {
      copies.add(apply(node));
    }
    return copies;
  }
}
