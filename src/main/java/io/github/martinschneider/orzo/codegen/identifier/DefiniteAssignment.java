package io.github.martinschneider.orzo.codegen.identifier;

import static io.github.martinschneider.orzo.lexer.tokens.Operators.ASSIGN;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LOGICAL_AND;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LOGICAL_OR;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.NEGATE;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.LambdaExpr;
import io.github.martinschneider.orzo.lexer.tokens.TernaryExpression;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.ExpressionParser;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.Break;
import io.github.martinschneider.orzo.parser.productions.CastMethodCall;
import io.github.martinschneider.orzo.parser.productions.CatchBlock;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Continue;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ExpressionStatement;
import io.github.martinschneider.orzo.parser.productions.FieldAssignment;
import io.github.martinschneider.orzo.parser.productions.ForEachStatement;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.IncrementStatement;
import io.github.martinschneider.orzo.parser.productions.LoopStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.SwitchCase;
import io.github.martinschneider.orzo.parser.productions.SwitchStatement;
import io.github.martinschneider.orzo.parser.productions.ThrowStatement;
import io.github.martinschneider.orzo.parser.productions.TryStatement;
import io.github.martinschneider.orzo.parser.tree.BinaryNode;
import io.github.martinschneider.orzo.parser.tree.CallNode;
import io.github.martinschneider.orzo.parser.tree.CastNode;
import io.github.martinschneider.orzo.parser.tree.ExprNode;
import io.github.martinschneider.orzo.parser.tree.IndexNode;
import io.github.martinschneider.orzo.parser.tree.InstanceOfNode;
import io.github.martinschneider.orzo.parser.tree.LiteralNode;
import io.github.martinschneider.orzo.parser.tree.NameNode;
import io.github.martinschneider.orzo.parser.tree.NewNode;
import io.github.martinschneider.orzo.parser.tree.OpaqueNode;
import io.github.martinschneider.orzo.parser.tree.SelectNode;
import io.github.martinschneider.orzo.parser.tree.TernaryNode;
import io.github.martinschneider.orzo.parser.tree.UnaryNode;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reports a local variable used before it is definitely assigned (JLS 16), e.g. "y" in "int y; if
 * (c) y = 1; print(y);".
 *
 * <p>The state at a point of a method is the set of the variables definitely assigned there, null
 * where it can't be reached (every variable is definitely assigned there). Only variables declared
 * without an initializer can be unassigned.
 */
public class DefiniteAssignment {
  private static final String LOG_NAME = "definite assignment";
  private final CompilerErrors errors;
  // the variables declared without an initializer, by scope
  private final Deque<Set<String>> scopes = new ArrayDeque<>();
  private final Deque<Target> targets = new ArrayDeque<>();
  private final Set<String> reported = new HashSet<>();
  private Token position;

  // a statement "break" and "continue" jump to, with the states they jump from
  private static class Target {
    final Statement stmt;
    final boolean isLoop;
    Set<String> breaks;
    Set<String> continues;

    Target(Statement stmt, boolean isLoop) {
      this.stmt = stmt;
      this.isLoop = isLoop;
    }
  }

  // the states after a condition when it is true and when it is false
  private static class Cond {
    final Set<String> whenTrue;
    final Set<String> whenFalse;

    Cond(Set<String> whenTrue, Set<String> whenFalse) {
      this.whenTrue = whenTrue;
      this.whenFalse = whenFalse;
    }
  }

  public DefiniteAssignment(CompilerErrors errors) {
    this.errors = errors;
  }

  /**
   * Whether the statements (e.g. the body of a constructor) definitely assign a variable or field
   * (also if they can't complete normally, e.g. by throwing an exception).
   */
  public static boolean assigns(List<Statement> statements, String name) {
    // (errors aren't reported)
    DefiniteAssignment analysis = new DefiniteAssignment(new CompilerErrors());
    analysis.scopes.push(new HashSet<>(Set.of(name)));
    Set<String> out = analysis.block(statements, new HashSet<>());
    return out == null || out.contains(name);
  }

  public void check(Method method) {
    scopes.clear();
    targets.clear();
    reported.clear();
    position = errors.startTokens.get(method);
    block(method.body, new HashSet<>());
  }

  private Set<String> block(List<Statement> statements, Set<String> in) {
    scopes.push(new HashSet<>());
    Set<String> out = statements(statements, in);
    scopes.pop();
    return out;
  }

  private Set<String> statements(List<Statement> statements, Set<String> in) {
    if (statements != null) {
      for (Statement stmt : statements) {
        in = statement(stmt, in);
      }
    }
    return in;
  }

  private Set<String> statement(Statement stmt, Set<String> in) {
    Token outer = position;
    position = errors.startTokens.getOrDefault(stmt, position);
    try {
      return analyze(stmt, in);
    } finally {
      position = outer;
    }
  }

  private Set<String> analyze(Statement stmt, Set<String> in) {
    if (stmt instanceof ParallelDeclaration) {
      for (Declaration decl : ((ParallelDeclaration) stmt).declarations) {
        in = expr(decl.val, in);
      }
      for (Declaration decl : ((ParallelDeclaration) stmt).declarations) {
        declare(decl.name.val.toString(), decl.val != null, in);
      }
      return in;
    } else if (stmt instanceof Assignment) {
      Assignment assignment = (Assignment) stmt;
      for (Expression value : assignment.right) {
        in = expr(value, in);
      }
      for (Identifier target : assignment.left) {
        if (target.next == null && target.arrSel == null && !(target instanceof MethodCall)) {
          assign(target.val.toString(), in);
        } else if (isThisField(target)) {
          assign(target.next.val.toString(), in); // e.g. "this.x = 1;"
        } else {
          in = token(target, in);
        }
      }
      return in;
    } else if (stmt instanceof FieldAssignment) {
      Identifier target = ((FieldAssignment) stmt).target;
      in = expr(((FieldAssignment) stmt).value, in);
      if (isThisField(target)) {
        assign(target.next.val.toString(), in);
        return in;
      }
      return token(target, in);
    } else if (stmt instanceof MethodCall || stmt instanceof ConstructorCall) {
      return token((Token) stmt, in);
    } else if (stmt instanceof ExpressionStatement) {
      return expr(((ExpressionStatement) stmt).expr, in);
    } else if (stmt instanceof IncrementStatement) {
      return expr(((IncrementStatement) stmt).expr, in);
    } else if (stmt instanceof ReturnStatement) {
      expr(((ReturnStatement) stmt).retValue, in);
      return null;
    } else if (stmt instanceof ThrowStatement) {
      ThrowStatement throwStmt = (ThrowStatement) stmt;
      in = expr(throwStmt.expr, in);
      if (throwStmt.constructorCall != null) {
        token(throwStmt.constructorCall, in);
      }
      return null;
    } else if (stmt instanceof Break) {
      Target target = target(((Break) stmt).label, false);
      if (target != null) {
        target.breaks = meet(target.breaks, in);
      }
      return null;
    } else if (stmt instanceof Continue) {
      Target target = target(((Continue) stmt).label, true);
      if (target != null) {
        target.continues = meet(target.continues, in);
      }
      return null;
    } else if (stmt instanceof IfStatement) {
      return ifStatement((IfStatement) stmt, in);
    } else if (stmt instanceof Block) {
      Target target = new Target(stmt, false);
      targets.push(target);
      Set<String> out = block(((Block) stmt).body, in);
      targets.pop();
      return meet(out, target.breaks);
    } else if (stmt instanceof ForStatement) {
      ForStatement loop = (ForStatement) stmt;
      scopes.push(new HashSet<>());
      in = statement(loop.init, in);
      Cond cond = cond(loop.cond, in);
      Target target = new Target(stmt, true);
      targets.push(target);
      Set<String> bodyOut = block(loop.body, cond.whenTrue);
      statement(loop.loopStmt, meet(bodyOut, target.continues));
      targets.pop();
      scopes.pop();
      return meet(cond.whenFalse, target.breaks);
    } else if (stmt instanceof DoStatement) {
      LoopStatement loop = (LoopStatement) stmt;
      Target target = new Target(stmt, true);
      targets.push(target);
      Set<String> bodyOut = block(loop.body, in);
      targets.pop();
      Cond cond = cond(loop.cond, meet(bodyOut, target.continues));
      return meet(cond.whenFalse, target.breaks);
    } else if (stmt instanceof LoopStatement) {
      LoopStatement loop = (LoopStatement) stmt;
      Cond cond = cond(loop.cond, in);
      Target target = new Target(stmt, true);
      targets.push(target);
      block(loop.body, cond.whenTrue);
      targets.pop();
      return meet(cond.whenFalse, target.breaks);
    } else if (stmt instanceof ForEachStatement) {
      ForEachStatement loop = (ForEachStatement) stmt;
      in = expr(loop.iterable, in);
      Target target = new Target(stmt, true);
      targets.push(target);
      scopes.push(new HashSet<>());
      Set<String> body = copy(in);
      declare(loop.elemVar.val.toString(), true, body);
      block(loop.body, body);
      scopes.pop();
      targets.pop();
      return meet(in, target.breaks);
    } else if (stmt instanceof SwitchStatement) {
      return switchStatement((SwitchStatement) stmt, in);
    } else if (stmt instanceof TryStatement) {
      return tryStatement((TryStatement) stmt, in);
    }
    return in; // e.g. an empty statement or a local class
  }

  private Set<String> ifStatement(IfStatement stmt, Set<String> in) {
    Set<String> out = null;
    boolean hasElse = false;
    for (IfBlock ifBlock : stmt.ifBlks) {
      if (ifBlock.cond == null) {
        hasElse = true;
        out = meet(out, block(ifBlock.body, copy(in)));
      } else {
        Cond cond = cond(ifBlock.cond, in);
        out = meet(out, block(ifBlock.body, cond.whenTrue));
        in = cond.whenFalse;
      }
    }
    return hasElse ? out : meet(out, in);
  }

  private Set<String> switchStatement(SwitchStatement stmt, Set<String> in) {
    in = expr(stmt.switchExpr, in);
    Target target = new Target(stmt, false);
    targets.push(target);
    scopes.push(new HashSet<>()); // (the cases share one block)
    Set<String> fallthrough = null;
    boolean hasDefault = false;
    for (SwitchCase switchCase : stmt.cases) {
      hasDefault |= switchCase.labels == null || switchCase.labels.isEmpty();
      fallthrough = statements(switchCase.body, meet(copy(in), fallthrough));
    }
    scopes.pop();
    targets.pop();
    Set<String> out = meet(fallthrough, target.breaks);
    return hasDefault ? out : meet(out, in);
  }

  private Set<String> tryStatement(TryStatement stmt, Set<String> in) {
    Set<String> out = block(stmt.tryBody, copy(in));
    if (stmt.catchBlocks != null) {
      for (CatchBlock catchBlock : stmt.catchBlocks) {
        scopes.push(new HashSet<>());
        Set<String> caught = copy(in);
        declare(catchBlock.varName, true, caught);
        out = meet(out, block(catchBlock.body, caught));
        scopes.pop();
      }
    }
    if (stmt.finallyBody != null) {
      // (assigned after the try statement if assigned after the try and catch blocks or after the
      // finally block)
      Set<String> finallyOut = block(stmt.finallyBody, copy(in));
      if (out == null || finallyOut == null) {
        return null;
      }
      out.addAll(finallyOut);
    }
    return out;
  }

  // the target of an unlabeled break (the innermost loop or switch) or continue (the innermost
  // loop), or of a labeled one
  private Target target(String label, boolean isContinue) {
    for (Target target : targets) {
      String targetLabel = CGContext.label(target.stmt);
      boolean unlabeledTarget =
          target.isLoop || (!isContinue && target.stmt instanceof SwitchStatement);
      if (label != null ? label.equals(targetLabel) : unlabeledTarget) {
        return target;
      }
    }
    return null;
  }

  private Cond cond(Expression expr, Set<String> in) {
    if (expr == null) {
      return new Cond(in, null);
    } else if (expr.tree == null
        && expr.tokens.size() == 1
        && expr.tokens.get(0) instanceof BoolLiteral) {
      // e.g. the "true" of "for (;;)"
      return constant((Boolean) expr.tokens.get(0).val, in);
    }
    return cond(ExpressionParser.tree(expr), in);
  }

  private Cond cond(ExprNode node, Set<String> in) {
    if (node instanceof LiteralNode && ((LiteralNode) node).value instanceof BoolLiteral) {
      return constant((Boolean) ((LiteralNode) node).value.val, in);
    } else if (node instanceof UnaryNode && ((UnaryNode) node).op == NEGATE) {
      Cond cond = cond(((UnaryNode) node).operand, in);
      return new Cond(cond.whenFalse, cond.whenTrue);
    } else if (node instanceof BinaryNode && ((BinaryNode) node).op == LOGICAL_AND) {
      Cond left = cond(((BinaryNode) node).left, in);
      Cond right = cond(((BinaryNode) node).right, left.whenTrue);
      return new Cond(right.whenTrue, meet(left.whenFalse, right.whenFalse));
    } else if (node instanceof BinaryNode && ((BinaryNode) node).op == LOGICAL_OR) {
      Cond left = cond(((BinaryNode) node).left, in);
      Cond right = cond(((BinaryNode) node).right, left.whenFalse);
      return new Cond(meet(left.whenTrue, right.whenTrue), right.whenFalse);
    }
    Set<String> out = node(node, in);
    return new Cond(out, copy(out));
  }

  private static Cond constant(boolean value, Set<String> in) {
    return value ? new Cond(in, null) : new Cond(null, in);
  }

  private Set<String> expr(Expression expr, Set<String> in) {
    if (expr == null) {
      return in;
    } else if (expr instanceof ArrayInit) {
      ArrayInit array = (ArrayInit) expr;
      for (Expression dim : array.dims) {
        in = expr(dim, in);
      }
      for (List<Expression> values : array.vals) {
        for (Expression value : values) {
          in = expr(value, in);
        }
      }
      return in;
    }
    return node(ExpressionParser.tree(expr), in);
  }

  private Set<String> node(ExprNode node, Set<String> in) {
    if (node instanceof NameNode) {
      if (!((NameNode) node).typeName) {
        use(((NameNode) node).name, in);
      }
    } else if (node instanceof BinaryNode) {
      BinaryNode binary = (BinaryNode) node;
      if (binary.op == ASSIGN && binary.left instanceof NameNode) {
        in = node(binary.right, in);
        assign(((NameNode) binary.left).name, in);
      } else if (binary.op == ASSIGN
          && binary.left instanceof SelectNode
          && ((SelectNode) binary.left).target instanceof NameNode
          && ((NameNode) ((SelectNode) binary.left).target).name.equals("this")) {
        in = node(binary.right, in);
        assign(((SelectNode) binary.left).name, in); // e.g. "this.x = 1"
      } else if (binary.op == LOGICAL_AND || binary.op == LOGICAL_OR) {
        Cond cond = cond(node, in);
        in = meet(cond.whenTrue, cond.whenFalse);
      } else {
        // (also a compound assignment, e.g. "x += 1", which uses x)
        in = node(binary.right, node(binary.left, in));
      }
    } else if (node instanceof UnaryNode) {
      in = node(((UnaryNode) node).operand, in);
    } else if (node instanceof TernaryNode) {
      TernaryNode ternary = (TernaryNode) node;
      Cond cond = cond(ternary.condition, in);
      in = meet(node(ternary.ifTrue, cond.whenTrue), node(ternary.ifFalse, cond.whenFalse));
    } else if (node instanceof CallNode) {
      CallNode call = (CallNode) node;
      in = node(call.target, in);
      for (ExprNode arg : call.args) {
        in = node(arg, in);
      }
    } else if (node instanceof SelectNode) {
      if (!((SelectNode) node).typeName) {
        in = node(((SelectNode) node).target, in);
      }
    } else if (node instanceof IndexNode) {
      in = node(((IndexNode) node).index, node(((IndexNode) node).target, in));
    } else if (node instanceof CastNode) {
      in = node(((CastNode) node).operand, in);
    } else if (node instanceof InstanceOfNode) {
      in = node(((InstanceOfNode) node).operand, in);
    } else if (node instanceof NewNode) {
      for (ExprNode arg : ((NewNode) node).args) {
        in = node(arg, in);
      }
    } else if (node instanceof OpaqueNode) {
      Expression expr = ((OpaqueNode) node).expr;
      if (expr instanceof ArrayInit) {
        in = expr(expr, in);
      } else {
        for (Token token : expr.tokens) {
          in = token(token, in);
        }
      }
    }
    return in;
  }

  // a token of an expression without a tree (e.g. a lambda), or a call or constructor call as a
  // statement
  private Set<String> token(Token token, Set<String> in) {
    if (token instanceof LambdaExpr) {
      // (its body may only use variables assigned before it)
      LambdaExpr lambda = (LambdaExpr) token;
      scopes.push(new HashSet<>());
      Set<String> body = copy(in);
      for (String param : lambda.params) {
        declare(param, true, body);
      }
      Deque<Target> outer = new ArrayDeque<>(targets);
      targets.clear();
      if (lambda.block != null) {
        block(lambda.block, body);
      } else {
        expr(lambda.body, body);
      }
      targets.addAll(outer);
      scopes.pop();
    } else if (token instanceof TernaryExpression) {
      TernaryExpression ternary = (TernaryExpression) token;
      Cond cond = cond(ternary.condition, in);
      in = meet(expr(ternary.trueBranch, cond.whenTrue), expr(ternary.falseBranch, cond.whenFalse));
    } else if (token instanceof CastMethodCall) {
      in = expr(((CastMethodCall) token).innerExpr, in);
    } else if (token instanceof Identifier) {
      Identifier root = (Identifier) token;
      if (root instanceof MethodCall) {
        String name = ((MethodCall) root).name;
        if (name.contains(".")) {
          use(name.substring(0, name.indexOf('.')), in);
        }
      } else if (!(root instanceof ConstructorCall)) {
        use(root.val.toString(), in);
      }
      for (Identifier link = root; link != null; link = link.next) {
        if (link instanceof MethodCall) {
          for (Expression param : ((MethodCall) link).params) {
            in = expr(param, in);
          }
        } else if (link instanceof ConstructorCall) {
          for (Expression arg : ((ConstructorCall) link).args) {
            in = expr(arg, in);
          }
        }
        if (link.arrSel != null) {
          for (Expression index : link.arrSel.exprs) {
            in = expr(index, in);
          }
        }
      }
    }
    return in;
  }

  private static boolean isThisField(Identifier target) {
    return "this".equals(target.val.toString())
        && target.next != null
        && target.next.next == null
        && target.next.arrSel == null
        && !(target.next instanceof MethodCall);
  }

  private void declare(String name, boolean assigned, Set<String> in) {
    if (in != null) {
      if (assigned) {
        in.add(name);
      } else {
        in.remove(name);
      }
    }
    if (!assigned) {
      scopes.peek().add(name);
    }
  }

  private static void assign(String name, Set<String> in) {
    if (in != null) {
      in.add(name);
    }
  }

  private void use(String name, Set<String> in) {
    if (in == null || in.contains(name) || !isTracked(name) || !reported.add(name)) {
      return;
    }
    errors.addError(
        position,
        LOG_NAME,
        "variable " + name + " might not have been initialized",
        new RuntimeException().getStackTrace());
  }

  private boolean isTracked(String name) {
    for (Set<String> scope : scopes) {
      if (scope.contains(name)) {
        return true;
      }
    }
    return false;
  }

  private static Set<String> copy(Set<String> state) {
    return (state == null) ? null : new HashSet<>(state);
  }

  // where two paths join: assigned on both (null, an unreachable path, is the identity)
  private static Set<String> meet(Set<String> a, Set<String> b) {
    if (a == null) {
      return copy(b);
    } else if (b == null) {
      return copy(a);
    }
    Set<String> both = new HashSet<>(a);
    both.retainAll(b);
    return both;
  }
}
