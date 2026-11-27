package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ATHROW;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ThrowStatement;

public class ThrowGenerator implements StatementGenerator<ThrowStatement> {
  private CGContext ctx;

  public ThrowGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ThrowStatement stmt) {
    if (stmt.constructorCall != null) {
      String type = ctx.exprGen.generateConstructorCall(out, ctx.classIdMap, stmt.constructorCall);
      ctx.exceptionChecker.thrown(ctx.exprGen.resolveJvmClassName(type), true);
    } else {
      ctx.exprGen.eval(out, null, stmt.expr);
      // (rethrowing a caught exception throws what its try block throws)
      boolean rethrow =
          stmt.expr.tokens.size() == 1
              && stmt.expr.tokens.get(0) instanceof Identifier
              && ctx.catchParameters.contains(stmt.expr.tokens.get(0).val.toString());
      ctx.exceptionChecker.thrown(ctx.opStack.type(), !rethrow);
    }
    out.write(ATHROW);
    ctx.opStack.pop();
    return out;
  }
}
