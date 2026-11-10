package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;

public class ForGenerator implements StatementGenerator<ForStatement> {
  private CGContext ctx;

  public ForGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ForStatement forStmt) {
    ctx.delegator.generate(out, method, forStmt.init);
    int etStart = ctx.exceptionTable.size();
    DynamicByteArray bodyOut = new DynamicByteArray();
    for (Statement innerStmt : forStmt.body) {
      ctx.delegator.generate(bodyOut, method, innerStmt);
    }
    // continue jumps here: past the body, straight to the increment step
    int incStart = bodyOut.size();
    ctx.delegator.generate(bodyOut, method, forStmt.loopStmt);
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) (3 + bodyOut.getBytes().length + 3);
    ctx.exprGen.eval(conditionOut, null, forStmt.cond, false, true);
    conditionOut.write(branchBytes);
    // Fix up ET entries: init bytes already in out; body starts after condition
    TryGenerator.adjustExceptionTableEntries(
        ctx, etStart, ctx.exceptionTable.size(), out.size() + conditionOut.getBytes().length);
    out.write(conditionOut.getBytes());
    byte[] bodyBytes = bodyOut.getBytes();
    // break -> past the trailing backward GOTO (loop exit); continue -> increment step
    for (int pos : bodyOut.pendingBreaks) {
      byte[] off = shortToByteArray((short) (bodyBytes.length - pos + 3));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    for (int pos : bodyOut.pendingContinues) {
      byte[] off = shortToByteArray((short) (incStart - pos));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    out.write(bodyBytes);
    out.write(GOTO);
    out.write(
        shortToByteArray((short) (-(bodyOut.getBytes().length + conditionOut.getBytes().length))));
    return out;
  }
}
