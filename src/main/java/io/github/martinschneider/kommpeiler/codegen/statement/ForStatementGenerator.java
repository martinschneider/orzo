package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GOTO;

import io.github.martinschneider.kommpeiler.codegen.ConditionalCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.ForStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class ForStatementGenerator implements StatementGenerator {
  private StatementGeneratorDelegator statementGeneratorDelegator;
  private ConditionalCodeGenerator conditionalCodeGenerator;
  private ConstantPool constantPool;

  public ForStatementGenerator(
      StatementGeneratorDelegator statementGeneratorDelegator,
      ConditionalCodeGenerator conditionalCodeGenerator,
      ConstantPool constantPool) {
    this.statementGeneratorDelegator = statementGeneratorDelegator;
    this.conditionalCodeGenerator = conditionalCodeGenerator;
    this.constantPool = constantPool;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement statement,
      Method method,
      Clazz clazz) {
    ForStatement forStatement = (ForStatement) statement;
    statementGeneratorDelegator.generate(
        variables, out, forStatement.getInitialization(), method, clazz);
    DynamicByteArray bodyOut = new DynamicByteArray();
    for (Statement stmt : forStatement.getBody()) {
      statementGeneratorDelegator.generate(variables, bodyOut, stmt, method, clazz);
    }
    statementGeneratorDelegator.generate(
        variables, bodyOut, forStatement.getLoopStatement(), method, clazz);
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) (3 + bodyOut.getBytes().length + 3);
    conditionalCodeGenerator.generateCondition(
        conditionOut, clazz, variables, constantPool, forStatement.getCondition(), branchBytes);
    out.write(conditionOut.getBytes());
    out.write(bodyOut.getBytes());
    out.write(GOTO);
    out.write(shortToByteArray(-(bodyOut.getBytes().length + conditionOut.getBytes().length)));
    return out;
  }
}
