package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.IFNE;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.DoStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;

public class DoGenerator implements StatementGenerator<DoStatement> {
  private CGContext ctx;

  public DoGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, DoStatement doStmt) {
    int etStart = ctx.exceptionTable.size();
    DynamicByteArray bodyOut = new DynamicByteArray();
    for (Statement innerStmt : doStmt.body) {
      ctx.delegator.generate(bodyOut, method, innerStmt);
    }
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) -(bodyOut.getBytes().length + conditionOut.getBytes().length);
    ctx.exprGen.eval(conditionOut, null, doStmt.cond, false, false);
    branchBytes -= conditionOut.getBytes().length;
    byte[] condBytes = conditionOut.getBytes();
    int lastUnsigned = condBytes.length > 0 ? (condBytes[condBytes.length - 1] & 0xFF) : -1;
    if (lastUnsigned >= 153 && lastUnsigned <= 166) {
      // eval already emitted a branch opcode; offset is measured from its start
      branchBytes++;
    } else {
      // condition yields a value (0/1) on the stack; branch back if non-zero
      conditionOut.write(IFNE);
    }
    conditionOut.write(branchBytes);
    // Fix up ET entries: body starts at current out position (written first)
    TryGenerator.adjustExceptionTableEntries(ctx, etStart, ctx.exceptionTable.size(), out.size());
    byte[] bodyBytes = bodyOut.getBytes();
    int condLen = conditionOut.getBytes().length;
    // break -> past the condition check (loop exit); continue -> start of condition check
    for (int pos : bodyOut.breaksFor(doStmt.label)) {
      byte[] off = shortToByteArray((short) (bodyBytes.length + condLen - pos));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    for (int pos : bodyOut.continuesFor(doStmt.label)) {
      byte[] off = shortToByteArray((short) (bodyBytes.length - pos));
      bodyBytes[pos + 1] = off[0];
      bodyBytes[pos + 2] = off[1];
    }
    out.mergeLabeled(bodyOut, out.size()); // breaks/continues to outer loops
    out.write(bodyBytes);
    out.write(conditionOut.getBytes());
    return out;
  }
}
