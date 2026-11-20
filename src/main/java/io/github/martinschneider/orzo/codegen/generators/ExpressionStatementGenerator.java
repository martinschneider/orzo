package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.POP;
import static io.github.martinschneider.orzo.codegen.OpCodes.POP2;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.ExpressionStatement;
import io.github.martinschneider.orzo.parser.productions.Method;

public class ExpressionStatementGenerator implements StatementGenerator<ExpressionStatement> {
  private CGContext ctx;

  public ExpressionStatementGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ExpressionStatement stmt) {
    ExpressionResult result = ctx.exprGen.eval(out, null, stmt.expr);
    // discard the (unused) result
    if (result != null
        && result.type != null
        && !result.type.isEmpty()
        && !VOID.equals(result.type)) {
      out.write((LONG.equals(result.type) || DOUBLE.equals(result.type)) ? POP2 : POP);
      ctx.opStack.pop();
    }
    return out;
  }
}
