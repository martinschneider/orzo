package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.OpsCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.ReturnStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class ReturnStatementGenerator implements StatementGenerator {
  private OpsCodeGenerator opsCodeGenerator;
  private ConstantPool constantPool;

  public ReturnStatementGenerator(OpsCodeGenerator opsCodeGenerator, ConstantPool constantPool) {
    this.opsCodeGenerator = opsCodeGenerator;
    this.constantPool = constantPool;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement statement,
      Method method,
      Clazz clazz) {
    ReturnStatement returnStatement = (ReturnStatement) statement;
    opsCodeGenerator.ret(
        out,
        clazz,
        variables,
        constantPool,
        method.getType().getValue().toString(),
        returnStatement.getRetValue());
    return out;
  }
}
