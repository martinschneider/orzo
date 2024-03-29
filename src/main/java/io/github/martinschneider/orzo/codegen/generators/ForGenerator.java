package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.Break;
import io.github.martinschneider.orzo.parser.productions.ForStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class ForGenerator implements StatementGenerator<ForStatement> {
  private CGContext ctx;

  public ForGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ForStatement forStmt) {
    ctx.delegator.generate(out, method, forStmt.init);
    DynamicByteArray bodyOut = new DynamicByteArray();
    // keep track of break statements
    List<Byte> breaks = new ArrayList<>();
    for (Statement innerStmt : forStmt.body) {
      if (innerStmt instanceof Break) {
        breaks.add((byte) (bodyOut.getBytes().length + 1));
        bodyOut.write(GOTO);
        bodyOut.write((short) 0); // temporary placeholder
      } else {
        ctx.delegator.generate(bodyOut, method, innerStmt);
      }
    }
    ctx.delegator.generate(bodyOut, method, forStmt.loopStmt);
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) (3 + bodyOut.getBytes().length + 3);
    ctx.exprGen.eval(conditionOut, null, forStmt.cond, false, true);
    conditionOut.write(branchBytes);
    out.write(conditionOut.getBytes());
    byte[] bodyBytes = bodyOut.getBytes();
    for (byte idx : breaks) {
      byte[] jmpOffset = shortToByteArray((short) (bodyBytes.length - idx + 4));
      bodyBytes[idx] = jmpOffset[0];
      bodyBytes[idx + 1] = jmpOffset[1];
    }
    out.write(bodyBytes);
    out.write(GOTO);
    out.write(
        shortToByteArray((short) (-(bodyOut.getBytes().length + conditionOut.getBytes().length))));
    return out;
  }
}
