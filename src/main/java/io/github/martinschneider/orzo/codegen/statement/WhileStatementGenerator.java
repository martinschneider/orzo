package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.GOTO;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Break;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;
import java.util.ArrayList;
import java.util.List;

public class WhileStatementGenerator implements StatementGenerator {
  private CGContext context;

  public WhileStatementGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    WhileStatement whileStatement = (WhileStatement) stmt;
    DynamicByteArray bodyOut = new DynamicByteArray();
    // keep track of break statements
    List<Byte> breaks = new ArrayList<>();
    for (Statement innerStmt : whileStatement.getBody()) {
      if (innerStmt instanceof Break) {
        breaks.add((byte) (bodyOut.getBytes().length + 1));
        bodyOut.write(GOTO);
        bodyOut.write((short) 0); // placeholder
      } else {
        context.delegator.generate(variables, bodyOut, method, innerStmt);
      }
    }
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) (3 + bodyOut.getBytes().length + 3);
    context.condGenerator.generateCondition(
        conditionOut, variables, whileStatement.getCondition(), branchBytes);
    out.write(conditionOut.getBytes());
    byte[] bodyBytes = bodyOut.getBytes();
    for (byte idx : breaks) {
      byte[] jmpOffset = shortToByteArray((short) bodyBytes.length - idx + 4);
      bodyBytes[idx] = jmpOffset[0];
      bodyBytes[idx + 1] = jmpOffset[1];
    }
    out.write(bodyBytes);
    out.write(GOTO);
    out.write(shortToByteArray(-(bodyOut.getBytes().length + conditionOut.getBytes().length)));
    return out;
  }
}
