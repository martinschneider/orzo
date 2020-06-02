package io.github.martinschneider.orzo.codegen.statement;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;

public class ReturnStatementGenerator implements StatementGenerator {
  private CGContext context;

  public ReturnStatementGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    ReturnStatement returnStatement = (ReturnStatement) stmt;
    context.opsGenerator.ret(out, variables, method.getType(), returnStatement.getRetValue());
    return out;
  }
}
