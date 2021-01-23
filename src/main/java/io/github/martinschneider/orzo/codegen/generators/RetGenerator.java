package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ARETURN;
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

public class RetGenerator implements StatementGenerator<ReturnStatement> {
  private CGContext ctx;

  public RetGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, ReturnStatement retStmt) {
    ret(out, variables, method.type, retStmt.retValue);
    return out;
  }

  private HasOutput ret(
      DynamicByteArray out, VariableMap variables, String type, Expression retValue) {
    ctx.exprGen.eval(out, variables, type, retValue);
    ctx.basicGen.convert1(out, ctx.opStack.type(), type);
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
    if (type.contains("[")) {
      out.write(ARETURN);
    }
    return out;
  }
}
