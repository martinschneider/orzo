package io.github.martinschneider.orzo.codegen.identifier;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.CatchBlock;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.ForEachStatement;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.LoopStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.SwitchCase;
import io.github.martinschneider.orzo.parser.productions.SwitchStatement;
import io.github.martinschneider.orzo.parser.productions.TryStatement;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reports a local variable declared while another one of the same name (or a parameter) is in scope
 * ("variable x is already defined"); variables of sibling blocks may share a name.
 */
public class LocalVariableChecker {
  private static final String LOG_NAME = "local variables";
  private final CompilerErrors errors;
  private final Deque<Set<String>> scopes = new ArrayDeque<>();

  public LocalVariableChecker(CompilerErrors errors) {
    this.errors = errors;
  }

  public void check(Method method) {
    scopes.clear();
    scopes.push(new HashSet<>());
    Token start = errors.startTokens.get(method);
    for (Argument arg : method.args) {
      declare(arg.name.val.toString(), start);
    }
    block(method.body);
    scopes.clear();
  }

  private void block(List<Statement> statements) {
    scopes.push(new HashSet<>());
    statements(statements);
    scopes.pop();
  }

  private void statements(List<Statement> statements) {
    if (statements != null) {
      for (Statement stmt : statements) {
        statement(stmt);
      }
    }
  }

  private void statement(Statement stmt) {
    Token start = errors.startTokens.get(stmt);
    if (stmt instanceof ParallelDeclaration) {
      for (Declaration decl : ((ParallelDeclaration) stmt).declarations) {
        if (!decl.isField) {
          declare(decl.name.val.toString(), start);
        }
      }
    } else if (stmt instanceof Block) {
      block(((Block) stmt).body);
    } else if (stmt instanceof IfStatement) {
      for (IfBlock ifBlock : ((IfStatement) stmt).ifBlks) {
        block(ifBlock.body);
      }
    } else if (stmt instanceof ForStatement) {
      // (the variables of the initialization are in scope in the loop)
      scopes.push(new HashSet<>());
      statement(((ForStatement) stmt).init);
      block(((ForStatement) stmt).body);
      scopes.pop();
    } else if (stmt instanceof LoopStatement) {
      block(((LoopStatement) stmt).body);
    } else if (stmt instanceof ForEachStatement) {
      ForEachStatement forEach = (ForEachStatement) stmt;
      scopes.push(new HashSet<>());
      declare(forEach.elemVar.val.toString(), start);
      block(forEach.body);
      scopes.pop();
    } else if (stmt instanceof TryStatement) {
      TryStatement tryStmt = (TryStatement) stmt;
      block(tryStmt.tryBody);
      if (tryStmt.catchBlocks != null) {
        for (CatchBlock catchBlock : tryStmt.catchBlocks) {
          scopes.push(new HashSet<>());
          declare(catchBlock.varName, start);
          block(catchBlock.body);
          scopes.pop();
        }
      }
      if (tryStmt.finallyBody != null) {
        block(tryStmt.finallyBody);
      }
    } else if (stmt instanceof SwitchStatement) {
      // (the cases share one block)
      scopes.push(new HashSet<>());
      for (SwitchCase switchCase : ((SwitchStatement) stmt).cases) {
        statements(switchCase.body);
      }
      scopes.pop();
    }
  }

  private void declare(String name, Token at) {
    for (Set<String> scope : scopes) {
      if (scope.contains(name)) {
        errors.addError(
            at,
            LOG_NAME,
            "variable " + name + " is already defined",
            new RuntimeException().getStackTrace());
        return;
      }
    }
    scopes.peek().add(name);
  }
}
