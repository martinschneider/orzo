package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GOTO;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Break;
import io.github.martinschneider.kommpeiler.parser.productions.ForStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class ForStatementGenerator implements StatementGenerator {
  private CGContext context;

  public ForStatementGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    ForStatement forStatement = (ForStatement) stmt;
    context.delegator.generate(variables, out, method, forStatement.getInitialization());
    DynamicByteArray bodyOut = new DynamicByteArray();
    // keep track of break statements
    List<Byte> breaks = new ArrayList<>();
    for (Statement innerStmt : forStatement.getBody()) {
      if (innerStmt instanceof Break) {
        breaks.add((byte) (bodyOut.getBytes().length + 1));
        bodyOut.write(GOTO);
        bodyOut.write((short) 0); // temporary placeholder
      } else {
        context.delegator.generate(variables, bodyOut, method, innerStmt);
      }
    }
    context.delegator.generate(variables, bodyOut, method, forStatement.getLoopStatement());
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) (3 + bodyOut.getBytes().length + 3);
    context.condGenerator.generateCondition(
        conditionOut, variables, forStatement.getCondition(), branchBytes);
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
