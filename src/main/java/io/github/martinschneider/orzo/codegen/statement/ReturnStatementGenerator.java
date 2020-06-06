package io.github.martinschneider.orzo.codegen.statement;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;

public class ReturnStatementGenerator implements StatementGenerator {
  private CGContext ctx;

  public ReturnStatementGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    ReturnStatement returnStatement = (ReturnStatement) stmt;
    ctx.opsGenerator.ret(out, variables, method.type, returnStatement.retValue);
    return out;
  }
}
