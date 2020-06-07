package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
import static io.github.martinschneider.orzo.codegen.OpCodes.NEWARRAY;
import static io.github.martinschneider.orzo.codegen.statement.PushGenerator.bipush;
import static io.github.martinschneider.orzo.codegen.statement.PushGenerator.sipush;
import static io.github.martinschneider.orzo.codegen.statement.TypeUtils.getArrayType;
import static io.github.martinschneider.orzo.codegen.statement.TypeUtils.getStoreOpCode;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.ArrayInitialiser;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;

public class DeclarationGenerator implements StatementGenerator {
  private static final int INTEGER_DEFAULT_VALUE = 0;
  private static final float DOUBLE_DEFAULT_VALUE = 0.0f;
  private static final String LOG_NAME = "generate declaration code";

  public DeclarationGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  private CGContext ctx;

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    Declaration decl = (Declaration) stmt;
    if (decl.arrDim > 0) {
      return generateArray(out, variables, method, decl);
    }
    if (decl.val != null) {
      ctx.exprGenerator.eval(out, variables, decl.type, decl.val);
    } else {
      if (decl.type.equals(INT)) {
        ctx.opsGenerator.pushInteger(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.type.equals(SHORT)) {
        sipush(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.type.equals(BYTE)) {
        bipush(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.type.equals(LONG)) {
        ctx.opsGenerator.pushLong(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.type.equals(FLOAT)) {
        ctx.opsGenerator.pushDouble(out, DOUBLE_DEFAULT_VALUE);
      } else if (decl.type.equals(DOUBLE)) {
        ctx.opsGenerator.pushFloat(out, DOUBLE_DEFAULT_VALUE);
      }
    }
    ctx.opsGenerator.assign(out, variables, decl.type, decl.name);
    return out;
  }

  public HasOutput generateArray(
      DynamicByteArray out, VariableMap variables, Method method, Declaration decl) {
    if (decl.val == null || !(decl.val instanceof ArrayInitialiser)) {
      ctx.errors.addError(LOG_NAME, "invalid array initialiser" + decl.val);
      return out;
    }
    String type = decl.type;
    byte arrayType = getArrayType(type);
    byte storeOpCode = getStoreOpCode(type);
    ArrayInitialiser arrInit = (ArrayInitialiser) decl.val;
    createArray(out, arrayType, arrInit.dims.get(0));
    for (int i = 0; i < arrInit.vals.get(0).size(); i++) {
      out.write(DUP);
      ctx.opsGenerator.pushInteger(out, i);
      ctx.exprGenerator.eval(out, variables, type, arrInit.vals.get(0).get(i));
      out.write(storeOpCode);
    }
    ctx.opsGenerator.assignArray(out, variables, type, decl.arrDim, decl.name);
    return out;
  }

  private void createArray(DynamicByteArray out, byte arrayType, int size) {
    ctx.opsGenerator.pushInteger(out, size);
    out.write(NEWARRAY);
    out.write(arrayType);
  }
}
