package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.CatchBlock;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.ExpressionStatement;
import io.github.martinschneider.orzo.parser.productions.FieldAssignment;
import io.github.martinschneider.orzo.parser.productions.ForEachStatement;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.IncrementStatement;
import io.github.martinschneider.orzo.parser.productions.LoopStatement;
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
import io.github.martinschneider.orzo.parser.tree.NameNode;
import io.github.martinschneider.orzo.parser.tree.SelectNode;
import io.github.martinschneider.orzo.parser.tree.TernaryNode;
import io.github.martinschneider.orzo.parser.tree.UnaryNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Walks statements. */
public class Statements {
  private Statements() {}

  /**
   * All expressions of the statements (also of nested ones), e.g. conditions, values and the
   * targets of assignments; calls and constructor calls as statements become expressions, too.
   */
  public static List<Expression> expressions(List<Statement> statements) {
    List<Expression> exprs = new ArrayList<>();
    addAll(statements, exprs, new HashSet<>(), new HashSet<>());
    return exprs;
  }

  /** The names of the variables the statements declare (also in nested ones). */
  public static Set<String> declaredNames(List<Statement> statements) {
    Set<String> names = new HashSet<>();
    addAll(statements, new ArrayList<>(), names, new HashSet<>());
    return names;
  }

  /**
   * The names of the variables or fields the statements assign (also nested ones), e.g. "x" for "x
   * = 1;" and "this.x = 1;".
   */
  public static Set<String> assignedNames(List<Statement> statements) {
    Set<String> assigned = new HashSet<>();
    List<Expression> exprs = new ArrayList<>();
    addAll(statements, exprs, new HashSet<>(), assigned);
    // (also assignments in expressions, e.g. "b" in "a = b = 0")
    for (Expression expr : exprs) {
      if (expr.tree != null) {
        addAssigned(expr.tree, assigned);
      }
    }
    return assigned;
  }

  private static void addAssigned(ExprNode node, Set<String> assigned) {
    if (node instanceof BinaryNode) {
      BinaryNode binary = (BinaryNode) node;
      if (binary.op == Operators.ASSIGN && binary.left instanceof NameNode) {
        assigned.add(((NameNode) binary.left).name);
      } else if (binary.op == Operators.ASSIGN
          && binary.left instanceof SelectNode
          && ((SelectNode) binary.left).target instanceof NameNode
          && ((NameNode) ((SelectNode) binary.left).target).name.equals("this")) {
        assigned.add(((SelectNode) binary.left).name);
      }
      addAssigned(binary.left, assigned);
      addAssigned(binary.right, assigned);
    } else if (node instanceof UnaryNode) {
      addAssigned(((UnaryNode) node).operand, assigned);
    } else if (node instanceof TernaryNode) {
      addAssigned(((TernaryNode) node).condition, assigned);
      addAssigned(((TernaryNode) node).ifTrue, assigned);
      addAssigned(((TernaryNode) node).ifFalse, assigned);
    } else if (node instanceof CallNode) {
      addAssigned(((CallNode) node).target, assigned);
      for (ExprNode arg : ((CallNode) node).args) {
        addAssigned(arg, assigned);
      }
    } else if (node instanceof CastNode) {
      addAssigned(((CastNode) node).operand, assigned);
    }
  }

  private static void addStatement(
      Statement stmt, List<Expression> exprs, Set<String> names, Set<String> assigned) {
    if (stmt instanceof ParallelDeclaration) {
      for (Declaration decl : ((ParallelDeclaration) stmt).declarations) {
        names.add(decl.name.val.toString());
        addExpression(decl.val, exprs);
      }
    } else if (stmt instanceof Assignment) {
      Assignment assignment = (Assignment) stmt;
      for (Identifier target : assignment.left) {
        addToken(target, exprs);
        addAssigned(target, assigned);
      }
      for (Expression value : assignment.right) {
        addExpression(value, exprs);
      }
    } else if (stmt instanceof FieldAssignment) {
      addToken(((FieldAssignment) stmt).target, exprs);
      addAssigned(((FieldAssignment) stmt).target, assigned);
      addExpression(((FieldAssignment) stmt).value, exprs);
    } else if (stmt instanceof MethodCall || stmt instanceof ConstructorCall) {
      addToken((Token) stmt, exprs);
    } else if (stmt instanceof ExpressionStatement) {
      addExpression(((ExpressionStatement) stmt).expr, exprs);
    } else if (stmt instanceof IncrementStatement) {
      addExpression(((IncrementStatement) stmt).expr, exprs);
    } else if (stmt instanceof ReturnStatement) {
      addExpression(((ReturnStatement) stmt).retValue, exprs);
    } else if (stmt instanceof ThrowStatement) {
      ThrowStatement throwStmt = (ThrowStatement) stmt;
      addExpression(throwStmt.expr, exprs);
      if (throwStmt.constructorCall != null) {
        addToken(throwStmt.constructorCall, exprs);
      }
    } else if (stmt instanceof IfStatement) {
      for (IfBlock block : ((IfStatement) stmt).ifBlks) {
        addExpression(block.cond, exprs);
        addAll(block.body, exprs, names, assigned);
      }
    } else if (stmt instanceof ForStatement) {
      ForStatement loop = (ForStatement) stmt;
      if (loop.init != null) {
        addStatement(loop.init, exprs, names, assigned);
      }
      addExpression(loop.cond, exprs);
      if (loop.loopStmt != null) {
        addStatement(loop.loopStmt, exprs, names, assigned);
      }
      addAll(loop.body, exprs, names, assigned);
    } else if (stmt instanceof LoopStatement) {
      addExpression(((LoopStatement) stmt).cond, exprs);
      addAll(((LoopStatement) stmt).body, exprs, names, assigned);
    } else if (stmt instanceof ForEachStatement) {
      names.add(((ForEachStatement) stmt).elemVar.val.toString());
      addExpression(((ForEachStatement) stmt).iterable, exprs);
      addAll(((ForEachStatement) stmt).body, exprs, names, assigned);
    } else if (stmt instanceof SwitchStatement) {
      SwitchStatement switchStmt = (SwitchStatement) stmt;
      addExpression(switchStmt.switchExpr, exprs);
      for (SwitchCase switchCase : switchStmt.cases) {
        addAll(switchCase.body, exprs, names, assigned);
      }
    } else if (stmt instanceof TryStatement) {
      TryStatement tryStmt = (TryStatement) stmt;
      addAll(tryStmt.tryBody, exprs, names, assigned);
      for (CatchBlock catchBlock : tryStmt.catchBlocks) {
        names.add(catchBlock.varName);
        addAll(catchBlock.body, exprs, names, assigned);
      }
      addAll(tryStmt.finallyBody, exprs, names, assigned);
    } else if (stmt instanceof Block) {
      addAll(((Block) stmt).body, exprs, names, assigned);
    }
  }

  private static void addAll(
      List<Statement> statements, List<Expression> exprs, Set<String> names, Set<String> assigned) {
    if (statements != null) {
      for (Statement stmt : statements) {
        addStatement(stmt, exprs, names, assigned);
      }
    }
  }

  // "x" or "this.x"
  private static void addAssigned(Identifier target, Set<String> assigned) {
    if (target.arrSel != null) {
      return;
    } else if (target.next == null) {
      assigned.add(target.val.toString());
    } else if ("this".equals(target.val.toString())
        && target.next.next == null
        && target.next.arrSel == null) {
      assigned.add(target.next.val.toString());
    }
  }

  private static void addExpression(Expression expr, List<Expression> exprs) {
    if (expr != null) {
      exprs.add(expr);
    }
  }

  private static void addToken(Token token, List<Expression> exprs) {
    List<Token> tokens = new ArrayList<>();
    tokens.add(token);
    exprs.add(new Expression(tokens));
  }
}
