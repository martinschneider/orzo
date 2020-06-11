package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.OpCodes.DRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.FRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.IRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.LRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.RETURN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Expression;
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
    ret(out, variables, method.type, returnStatement.retValue);
    return out;
  }

  private HasOutput ret(
      DynamicByteArray out, VariableMap variables, String type, Expression retValue) {
    ctx.exprGenerator.eval(out, variables, type, retValue);
    switch (type) {
      case INT:
        out.write(IRETURN);
        break;
      case LONG:
        out.write(LRETURN);
        break;
      case DOUBLE:
        out.write(DRETURN);
        break;
      case FLOAT:
        out.write(FRETURN);
        break;
      case VOID:
        out.write(RETURN);
    }
    return out;
  }
}
