package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class IfGenerator implements StatementGenerator<IfStatement> {
  private CGContext ctx;

  public IfGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  private boolean blockAlwaysReturns(List<Statement> body) {
    return !body.isEmpty() && body.get(body.size() - 1) instanceof ReturnStatement;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, IfStatement ifStmt) {
    List<DynamicByteArray> bodyOutputs = new ArrayList<>();
    List<DynamicByteArray> condOutputs = new ArrayList<>();
    List<Integer> bodyEtStarts = new ArrayList<>();
    List<Integer> bodyEtEnds = new ArrayList<>();
    for (int i = 0; i < ifStmt.ifBlks.size(); i++) {
      IfBlock ifBlock = ifStmt.ifBlks.get(i);
      int etStart = ctx.exceptionTable.size();
      DynamicByteArray bodyOut = new DynamicByteArray();
      for (Statement innerStmt : ifBlock.body) {
        ctx.delegator.generate(bodyOut, method, innerStmt);
      }
      bodyEtStarts.add(etStart);
      bodyEtEnds.add(ctx.exceptionTable.size());
      DynamicByteArray conditionOut = new DynamicByteArray();
      if (ifBlock.cond != null) { // null for else blocks
        boolean bodyReturns = blockAlwaysReturns(ifBlock.body);
        short branchBytes = (short) (3 + bodyOut.getBytes().length);
        if (i != ifStmt.ifBlks.size() - 1 && !bodyReturns) {
          branchBytes += 3;
        }
        ctx.exprGen.eval(conditionOut, null, ifBlock.cond, false, true);
        conditionOut.write(branchBytes);
      }
      bodyOutputs.add(bodyOut);
      condOutputs.add(conditionOut);
    }
    int blocks = bodyOutputs.size();
    short offset = (short) (3 + bodyOutputs.get(blocks - 1).size());
    if (!ifStmt.hasElse) {
      offset += condOutputs.get(blocks - 1).size();
    }
    // if there's no "else" then the last "else if" can fall through (doesn't require a goto)
    for (int i = blocks - 2; i >= 0; i--) {
      if (!blockAlwaysReturns(ifStmt.ifBlks.get(i).body)) {
        bodyOutputs.get(i).write(GOTO);
        bodyOutputs.get(i).write(shortToByteArray(offset));
      }
      offset += condOutputs.get(i).size() + bodyOutputs.get(i).size();
    }
    // Fix up exception table entries: add each body's absolute base offset
    int absPC = out.size();
    for (int i = 0; i < blocks; i++) {
      absPC += condOutputs.get(i).size();
      TryGenerator.adjustExceptionTableEntries(ctx, bodyEtStarts.get(i), bodyEtEnds.get(i), absPC);
      absPC += bodyOutputs.get(i).size();
    }
    for (int i = 0; i < bodyOutputs.size(); i++) {
      if (condOutputs.get(i) != null) {
        out.write(condOutputs.get(i).getBytes());
      }
      out.write(bodyOutputs.get(i).getBytes());
    }
    return out;
  }
}
