package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GOTO;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Break;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.WhileStatement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WhileStatementGenerator implements StatementGenerator {
  private StatementDelegator stmtDelegator;
  private ConditionalGenerator conditionalGenerator;
  private ConstantPool constPool;

  public WhileStatementGenerator(
      StatementDelegator stmtDelegator,
      ConditionalGenerator conditionalGenerator,
      ConstantPool constPool) {
    this.stmtDelegator = stmtDelegator;
    this.conditionalGenerator = conditionalGenerator;
    this.constPool = constPool;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement stmt,
      Method method,
      Clazz clazz) {
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
        stmtDelegator.generate(variables, bodyOut, innerStmt, method, clazz);
      }
    }
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) (3 + bodyOut.getBytes().length + 3);
    conditionalGenerator.generateCondition(
        conditionOut, clazz, variables, constPool, whileStatement.getCondition(), branchBytes);
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
