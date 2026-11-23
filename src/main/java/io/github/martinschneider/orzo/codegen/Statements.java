package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
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
    addAll(statements, exprs, new HashSet<>());
    return exprs;
  }

  /** The names of the variables the statements declare (also in nested ones). */
  public static Set<String> declaredNames(List<Statement> statements) {
    Set<String> names = new HashSet<>();
    addAll(statements, new ArrayList<>(), names);
    return names;
  }

  private static void addStatement(Statement stmt, List<Expression> exprs, Set<String> names) {
    if (stmt instanceof ParallelDeclaration) {
      for (Declaration decl : ((ParallelDeclaration) stmt).declarations) {
        names.add(decl.name.val.toString());
        addExpression(decl.val, exprs);
      }
    } else if (stmt instanceof Assignment) {
      Assignment assignment = (Assignment) stmt;
      for (Identifier target : assignment.left) {
        addToken(target, exprs);
      }
      for (Expression value : assignment.right) {
        addExpression(value, exprs);
      }
    } else if (stmt instanceof FieldAssignment) {
      addToken(((FieldAssignment) stmt).target, exprs);
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
        addAll(block.body, exprs, names);
      }
    } else if (stmt instanceof ForStatement) {
      ForStatement loop = (ForStatement) stmt;
      if (loop.init != null) {
        addStatement(loop.init, exprs, names);
      }
      addExpression(loop.cond, exprs);
      if (loop.loopStmt != null) {
        addStatement(loop.loopStmt, exprs, names);
      }
      addAll(loop.body, exprs, names);
    } else if (stmt instanceof LoopStatement) {
      addExpression(((LoopStatement) stmt).cond, exprs);
      addAll(((LoopStatement) stmt).body, exprs, names);
    } else if (stmt instanceof ForEachStatement) {
      names.add(((ForEachStatement) stmt).elemVar.val.toString());
      addExpression(((ForEachStatement) stmt).iterable, exprs);
      addAll(((ForEachStatement) stmt).body, exprs, names);
    } else if (stmt instanceof SwitchStatement) {
      SwitchStatement switchStmt = (SwitchStatement) stmt;
      addExpression(switchStmt.switchExpr, exprs);
      for (SwitchCase switchCase : switchStmt.cases) {
        addAll(switchCase.body, exprs, names);
      }
    } else if (stmt instanceof TryStatement) {
      TryStatement tryStmt = (TryStatement) stmt;
      addAll(tryStmt.tryBody, exprs, names);
      for (CatchBlock catchBlock : tryStmt.catchBlocks) {
        names.add(catchBlock.varName);
        addAll(catchBlock.body, exprs, names);
      }
      addAll(tryStmt.finallyBody, exprs, names);
    } else if (stmt instanceof Block) {
      addAll(((Block) stmt).body, exprs, names);
    }
  }

  private static void addAll(
      List<Statement> statements, List<Expression> exprs, Set<String> names) {
    if (statements != null) {
      for (Statement stmt : statements) {
        addStatement(stmt, exprs, names);
      }
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
