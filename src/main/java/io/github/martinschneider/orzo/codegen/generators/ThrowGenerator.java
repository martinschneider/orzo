package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ATHROW;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
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
      ctx.exprGen.generateConstructorCall(out, ctx.classIdMap, stmt.constructorCall);
    } else {
      ctx.exprGen.eval(out, null, stmt.expr);
    }
    out.write(ATHROW);
    ctx.opStack.pop();
    return out;
  }
}
