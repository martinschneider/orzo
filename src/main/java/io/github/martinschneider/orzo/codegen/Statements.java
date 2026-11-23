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
import java.util.List;

/** Walks statements. */
public class Statements {
  private Statements() {}

  /**
   * All expressions of the statements (also of nested ones), e.g. conditions, values and the
   * targets of assignments; calls and constructor calls as statements become expressions, too.
   */
  public static List<Expression> expressions(List<Statement> statements) {
    List<Expression> exprs = new ArrayList<>();
    for (Statement stmt : statements) {
      addStatement(stmt, exprs);
    }
    return exprs;
  }

  private static void addStatement(Statement stmt, List<Expression> exprs) {
    if (stmt instanceof ParallelDeclaration) {
      for (Declaration decl : ((ParallelDeclaration) stmt).declarations) {
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
        addAll(block.body, exprs);
      }
    } else if (stmt instanceof ForStatement) {
      ForStatement loop = (ForStatement) stmt;
      if (loop.init != null) {
        addStatement(loop.init, exprs);
      }
      addExpression(loop.cond, exprs);
      if (loop.loopStmt != null) {
        addStatement(loop.loopStmt, exprs);
      }
      addAll(loop.body, exprs);
    } else if (stmt instanceof LoopStatement) {
      addExpression(((LoopStatement) stmt).cond, exprs);
      addAll(((LoopStatement) stmt).body, exprs);
    } else if (stmt instanceof ForEachStatement) {
      addExpression(((ForEachStatement) stmt).iterable, exprs);
      addAll(((ForEachStatement) stmt).body, exprs);
    } else if (stmt instanceof SwitchStatement) {
      SwitchStatement switchStmt = (SwitchStatement) stmt;
      addExpression(switchStmt.switchExpr, exprs);
      for (SwitchCase switchCase : switchStmt.cases) {
        addAll(switchCase.body, exprs);
      }
    } else if (stmt instanceof TryStatement) {
      TryStatement tryStmt = (TryStatement) stmt;
      addAll(tryStmt.tryBody, exprs);
      for (CatchBlock catchBlock : tryStmt.catchBlocks) {
        addAll(catchBlock.body, exprs);
      }
      addAll(tryStmt.finallyBody, exprs);
    } else if (stmt instanceof Block) {
      addAll(((Block) stmt).body, exprs);
    }
  }

  private static void addAll(List<Statement> statements, List<Expression> exprs) {
    if (statements != null) {
      for (Statement stmt : statements) {
        addStatement(stmt, exprs);
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
