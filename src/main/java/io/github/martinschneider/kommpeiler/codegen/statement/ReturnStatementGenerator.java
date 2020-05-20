package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.ReturnStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class ReturnStatementGenerator implements StatementGenerator {
  private OpsCodeGenerator opsGenerator;
  private ConstantPool constPool;

  public ReturnStatementGenerator(OpsCodeGenerator opsGenerator, ConstantPool constPool) {
    this.opsGenerator = opsGenerator;
    this.constPool = constPool;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement stmt,
      Method method,
      Clazz clazz) {
    ReturnStatement returnStatement = (ReturnStatement) stmt;
    opsGenerator.ret(
        out,
        clazz,
        variables,
        constPool,
        method.getType().getValue().toString(),
        returnStatement.getRetValue());
    return out;
  }
}
