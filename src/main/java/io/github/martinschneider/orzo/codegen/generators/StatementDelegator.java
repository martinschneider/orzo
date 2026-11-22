package io.github.martinschneider.orzo.codegen.generators;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Block;
import io.github.martinschneider.orzo.parser.productions.Break;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Continue;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import io.github.martinschneider.orzo.parser.productions.EmptyStatement;
import io.github.martinschneider.orzo.parser.productions.ExpressionStatement;
import io.github.martinschneider.orzo.parser.productions.FieldAssignment;
import io.github.martinschneider.orzo.parser.productions.ForEachStatement;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.IncrementStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.SwitchStatement;
import io.github.martinschneider.orzo.parser.productions.ThrowStatement;
import io.github.martinschneider.orzo.parser.productions.TryStatement;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;
import java.util.HashMap;
import java.util.Map;

public class StatementDelegator {
  public Map<Class<? extends Statement>, StatementGenerator> reg = new HashMap<>();
  public CGContext ctx;

  public void init() {
    reg.put(ParallelDeclaration.class, new DeclarationGenerator(ctx));
    reg.put(DoStatement.class, new DoGenerator(ctx));
    reg.put(ForEachStatement.class, new ForEachGenerator(ctx));
    reg.put(ForStatement.class, new ForGenerator(ctx));
    reg.put(IfStatement.class, new IfGenerator(ctx));
    reg.put(MethodCall.class, new MethodCallGenerator(ctx));
    reg.put(ConstructorCall.class, new ConstructorCallGenerator(ctx));
    reg.put(Assignment.class, new AssignmentGenerator(ctx));
    reg.put(ReturnStatement.class, new RetGenerator(ctx));
    reg.put(WhileStatement.class, new WhileGenerator(ctx));
    reg.put(IncrementStatement.class, new IncrementGenerator(ctx));
    reg.put(SwitchStatement.class, new SwitchGenerator(ctx));
    reg.put(ThrowStatement.class, new ThrowGenerator(ctx));
    reg.put(TryStatement.class, new TryGenerator(ctx));
    reg.put(EmptyStatement.class, new DoNothingGenerator());
    reg.put(Break.class, new BreakGenerator(ctx));
    reg.put(Continue.class, new ContinueGenerator(ctx));
    reg.put(ExpressionStatement.class, new ExpressionStatementGenerator(ctx));
    reg.put(FieldAssignment.class, new FieldAssignmentGenerator(ctx));
    reg.put(Block.class, new BlockGenerator(ctx));
  }

  public HasOutput generate(DynamicByteArray out, Method method, Statement stmt) {
    if (stmt == null) {
      return out;
    }
    // errors are reported at the start of the statement (restored for an enclosing statement)
    Token outer = ctx.errors.position;
    Token start = ctx.errors.startTokens.get(stmt);
    if (start != null) {
      ctx.errors.position = start;
    }
    boolean jumpTarget =
        CGContext.isLoop(stmt) || stmt instanceof SwitchStatement || CGContext.label(stmt) != null;
    if (jumpTarget) {
      ctx.jumpTargets.push(stmt);
    }
    try {
      return reg.get(stmt.getClass()).generate(out, method, stmt);
    } finally {
      ctx.errors.position = outer;
      if (jumpTarget) {
        ctx.jumpTargets.pop();
      }
    }
  }
}
