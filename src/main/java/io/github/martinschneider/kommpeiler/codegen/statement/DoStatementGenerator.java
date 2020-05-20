package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.ConditionalCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.DoStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class DoStatementGenerator implements StatementGenerator {
  private StatementGeneratorDelegator statementGeneratorDelegator;
  private ConditionalCodeGenerator conditionalCodeGenerator;
  private ConstantPool constantPool;

  public DoStatementGenerator(
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
    DoStatement doStatement = (DoStatement) statement;
    DynamicByteArray bodyOut = new DynamicByteArray();
    for (Statement stmt : doStatement.getBody()) {
      statementGeneratorDelegator.generate(variables, bodyOut, stmt, method, clazz);
    }
    DynamicByteArray conditionOut = new DynamicByteArray();
    short branchBytes = (short) -(bodyOut.getBytes().length + conditionOut.getBytes().length);
    conditionalCodeGenerator.generateCondition(
        conditionOut,
        clazz,
        variables,
        constantPool,
        doStatement.getCondition(),
        branchBytes,
        true);
    out.write(bodyOut.getBytes());
    out.write(conditionOut.getBytes());
    return out;
  }
}
