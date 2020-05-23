package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GOTO;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Break;
import io.github.martinschneider.kommpeiler.parser.productions.DoStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class DoStatementGenerator implements StatementGenerator {
  public DoStatementGenerator(CGContext context) {
    this.context = context;
  }

  private CGContext context;

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    DoStatement doStatement = (DoStatement) stmt;
    DynamicByteArray bodyOut = new DynamicByteArray();
    // keep track of break statements
    List<Byte> breaks = new ArrayList<>();
    for (Statement innerStmt : doStatement.getBody()) {
      if (innerStmt instanceof Break) {
        breaks.add((byte) (bodyOut.getBytes().length + 1));
        bodyOut.write(GOTO);
        bodyOut.write((short) 0); // placeholder
      } else {
        context.delegator.generate(variables, bodyOut, method, innerStmt);
      }
    }
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) -(bodyOut.getBytes().length + conditionOut.getBytes().length);
    context.condGenerator.generateCondition(
        conditionOut, variables, doStatement.getCondition(), branchBytes, true);
    byte[] bodyBytes = bodyOut.getBytes();
    for (byte idx : breaks) {
      byte[] jmpOffset =
          shortToByteArray((short) bodyBytes.length - idx + 1 + conditionOut.getBytes().length);
      bodyBytes[idx] = jmpOffset[0];
      bodyBytes[idx + 1] = jmpOffset[1];
    }
    out.write(bodyBytes);
    out.write(conditionOut.getBytes());
    return out;
  }
}
