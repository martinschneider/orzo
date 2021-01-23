package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class IfGenerator implements StatementGenerator<IfStatement> {
  private CGContext ctx;

  public IfGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, IfStatement ifStmt) {
    List<DynamicByteArray> bodyOutputs = new ArrayList<>();
    List<DynamicByteArray> condOutputs = new ArrayList<>();
    for (int i = 0; i < ifStmt.ifBlks.size(); i++) {
      IfBlock ifBlock = ifStmt.ifBlks.get(i);
      DynamicByteArray bodyOut = new DynamicByteArray();
      for (Statement innerStmt : ifBlock.body) {
        ctx.delegator.generate(variables, bodyOut, method, innerStmt);
      }
      DynamicByteArray conditionOut = new DynamicByteArray();
      if (ifBlock.cond != null) { // null for else blocks
        short branchBytes = (short) (3 + bodyOut.getBytes().length);
        if (i != ifStmt.ifBlks.size() - 1) {
          branchBytes += 3;
        }
        ctx.exprGen.eval(conditionOut, variables, null, ifBlock.cond, false, true);
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
      bodyOutputs.get(i).write(GOTO);
      bodyOutputs.get(i).write(shortToByteArray(offset));
      offset += condOutputs.get(i).size() + bodyOutputs.get(i).size();
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
