package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GOTO;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Break;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.DoStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DoStatementGenerator implements StatementGenerator {
  private StatementDelegator stmtDelegator;
  private ConditionalGenerator condGenerator;
  private ConstantPool constPool;

  public DoStatementGenerator(
      StatementDelegator stmtDelegator,
      ConditionalGenerator condGenerator,
      ConstantPool constPool) {
    this.stmtDelegator = stmtDelegator;
    this.condGenerator = condGenerator;
    this.constPool = constPool;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement stmt,
      Method method,
      Clazz clazz) {
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
        stmtDelegator.generate(variables, bodyOut, innerStmt, method, clazz);
      }
    }
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) -(bodyOut.getBytes().length + conditionOut.getBytes().length);
    condGenerator.generateCondition(
        conditionOut, clazz, variables, constPool, doStatement.getCondition(), branchBytes, true);
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
