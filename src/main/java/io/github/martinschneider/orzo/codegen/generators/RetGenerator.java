package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ARETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.DRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.FRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.IRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.LRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.RETURN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;

public class RetGenerator implements StatementGenerator<ReturnStatement> {
  private CGContext ctx;

  public RetGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ReturnStatement retStmt) {
    ret(out, method.type, retStmt.retValue);
    return out;
  }

  private HasOutput ret(DynamicByteArray out, String type, Expression retValue) {
    ctx.exprGen.eval(out, type, retValue);
    ctx.basicGen.convert1(out, ctx.opStack.peek(), type);
    switch (type) {
      case INT:
        out.write(IRETURN);
        return out;
      case LONG:
        out.write(LRETURN);
        return out;
      case BOOLEAN:
        out.write(IRETURN);
        return out;
      case SHORT:
        out.write(IRETURN);
        return out;
      case BYTE:
        out.write(IRETURN);
        return out;
      case CHAR:
        out.write(IRETURN);
        return out;
      case DOUBLE:
        out.write(DRETURN);
        return out;
      case FLOAT:
        out.write(FRETURN);
        return out;
      case VOID:
        out.write(RETURN);
        return out;
    }
    // else (array or general type)
    out.write(ARETURN);
    return out;
  }
}
