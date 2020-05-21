package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.ReturnStatement;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class ReturnStatementGenerator implements StatementGenerator {
  private CGContext context;

  public ReturnStatementGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out,
      Map<Identifier, VariableInfo> variables,
      Method method,
      Statement stmt) {
    ReturnStatement returnStatement = (ReturnStatement) stmt;
    context.opsGenerator.ret(
        out, variables, method.getType().getValue().toString(), returnStatement.getRetValue());
    return out;
  }
}
