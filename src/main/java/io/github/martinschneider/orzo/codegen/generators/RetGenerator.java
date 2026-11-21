package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ARETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.DRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.FRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.IRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.LRETURN;
import static io.github.martinschneider.orzo.codegen.OpCodes.RETURN;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;
import static java.util.Collections.emptyList;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class RetGenerator implements StatementGenerator<ReturnStatement> {
  private CGContext ctx;

  public RetGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ReturnStatement retStmt) {
    if (ctx.finallyStack.isEmpty()) {
      ret(out, method.type, retStmt.retValue);
      return out;
    }
    // Inside try block(s) with finally: evaluate the return value into a temporary first, then
    // run the enclosing finally blocks (innermost first), then return the saved value.
    VariableInfo tmp = null;
    if (retStmt.retValue != null) {
      ctx.exprGen.eval(out, evalType(method.type), retStmt.retValue);
      ctx.basicGen.convert1(out, ctx.opStack.type(), method.type);
      String tmpName = "$ret_" + ctx.classIdMap.variables.localSize;
      tmp =
          new VariableInfo(
              tmpName,
              method.type,
              emptyList(),
              false,
              (short) ctx.classIdMap.variables.localSize,
              null);
      ctx.classIdMap.variables.putLocal(id(tmpName), tmp);
      ctx.storeGen.store(out, tmp);
    }
    List<List<Statement>> pending = new ArrayList<>(ctx.finallyStack);
    ctx.finallyStack.clear();
    for (List<Statement> finallyBody : pending) {
      for (Statement stmt : finallyBody) {
        ctx.delegator.generate(out, method, stmt);
      }
    }
    for (int i = pending.size() - 1; i >= 0; i--) {
      ctx.finallyStack.push(pending.get(i));
    }
    if (tmp == null) {
      out.write(RETURN);
      return out;
    }
    ctx.loadGen.load(out, tmp);
    returnOpcode(out, method.type);
    return out;
  }

  private HasOutput ret(DynamicByteArray out, String type, Expression retValue) {
    if (retValue == null) {
      // "return;" - nothing to evaluate or convert (the operand-stack model's top entry, if any,
      // belongs to an earlier statement)
      out.write(RETURN);
      return out;
    }
    ctx.exprGen.eval(out, evalType(type), retValue);
    ctx.basicGen.convert1(out, ctx.opStack.type(), type);
    return returnOpcode(out, type);
  }

  // The type to evaluate a returned expression in: a general reference return type (e.g. Object)
  // must not be imposed on the expression ("return a + b;" is still a String concatenation).
  private static String evalType(String returnType) {
    return (TypeUtils.isPrimitive(returnType) || STRING.equals(returnType)) ? returnType : null;
  }

  private HasOutput returnOpcode(DynamicByteArray out, String type) {
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
