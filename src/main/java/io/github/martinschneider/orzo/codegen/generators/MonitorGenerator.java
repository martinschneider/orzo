package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.MONITORENTER;
import static io.github.martinschneider.orzo.codegen.OpCodes.MONITOREXIT;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MonitorStatement;

/** "monitorenter" or "monitorexit" on a local variable (of a synchronized statement). */
public class MonitorGenerator implements StatementGenerator<MonitorStatement> {
  private CGContext ctx;

  public MonitorGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, MonitorStatement stmt) {
    ctx.loadGen.load(out, ctx.classIdMap.variables.get(id(stmt.var)));
    out.write(stmt.enter ? MONITORENTER : MONITOREXIT);
    ctx.opStack.pop();
    return out;
  }
}
