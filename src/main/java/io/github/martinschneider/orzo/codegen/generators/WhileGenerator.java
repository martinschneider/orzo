package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;

public class WhileGenerator implements StatementGenerator<WhileStatement> {
  private CGContext ctx;

  public WhileGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, WhileStatement whileStmt) {
    int etStart = ctx.exceptionTable.size();
    DynamicByteArray bodyOut = new DynamicByteArray();
    for (Statement innerStmt : whileStmt.body) {
      ctx.delegator.generate(bodyOut, method, innerStmt);
    }
    DynamicByteArray conditionOut = new DynamicByteArray();
    // A literal-"true" condition can never branch out, so skip emitting a runtime check for it
    // entirely (matching javac) - unlike a real condition, it has no exit branch target, which
    // would otherwise land out of bounds when nothing follows the loop (e.g. "while (true) { ...;
    // return x; }" as a method's last statement).
    if (!MethodGenerator.isLiteralTrue(whileStmt.cond)) {
      short branchBytes = (short) (3 + bodyOut.getBytes().length + 3);
      ctx.exprGen.eval(conditionOut, null, whileStmt.cond, false, true);
      conditionOut.write(branchBytes);
    }
    // Fix up ET entries: body starts after condition
    TryGenerator.adjustExceptionTableEntries(
        ctx, etStart, ctx.exceptionTable.size(), out.size() + conditionOut.getBytes().length);
    out.write(conditionOut.getBytes());
    byte[] bodyBytes = bodyOut.getBytes();
    int condLen = conditionOut.getBytes().length;
    // break -> past the trailing backward GOTO (loop exit); continue -> top of loop (condition
    // check, which is condLen bytes before the body)
    for (int pos : bodyOut.pendingBreaks) {
      byte[] off = shortToByteArray((short) (bodyBytes.length - pos + 3));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    for (int pos : bodyOut.pendingContinues) {
      byte[] off = shortToByteArray((short) (-condLen - pos));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    out.write(bodyBytes);
    out.write(GOTO);
    out.write(shortToByteArray((short) (-(bodyBytes.length + condLen))));
    return out;
  }
}
