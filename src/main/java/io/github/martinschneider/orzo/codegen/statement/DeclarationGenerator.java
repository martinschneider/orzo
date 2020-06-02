package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
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

  public DeclarationGenerator(CGContext context) {
    this.ctx = context;
  }

  private CGContext ctx;

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    Declaration decl = (Declaration) stmt;
    if (decl.getArray() > 0) {
      return generateArray(out, variables, method, decl);
    }
    if (decl.getValue() != null) {
      ctx.exprGenerator.eval(out, variables, decl.getType(), decl.getValue());
    } else {
      if (decl.getType().equals(INT)) {
        ctx.opsGenerator.pushInteger(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.getType().equals(SHORT)) {
        ctx.opsGenerator.sipush(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.getType().equals(BYTE)) {
        ctx.opsGenerator.bipush(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.getType().equals(LONG)) {
        ctx.opsGenerator.pushLong(out, INTEGER_DEFAULT_VALUE);
      } else if (decl.getType().equals(FLOAT)) {
        ctx.opsGenerator.pushDouble(out, DOUBLE_DEFAULT_VALUE);
      } else if (decl.getType().equals(DOUBLE)) {
        ctx.opsGenerator.pushFloat(out, DOUBLE_DEFAULT_VALUE);
      }
    }
    ctx.opsGenerator.assign(out, variables, decl.getType(), decl.getName());
    return out;
  }

  public HasOutput generateArray(
      DynamicByteArray out, VariableMap variables, Method method, Declaration decl) {
    if (decl.getValue() == null || !(decl.getValue() instanceof ArrayInitialiser)) {
      ctx.errors.addError(LOG_NAME, "invalid array initialiser" + decl.getValue());
      return out;
    }
    String type = decl.getType();
    byte arrayType = getArrayType(type);
    byte storeOpCode = getStoreOpCode(type);
    ArrayInitialiser arrInit = (ArrayInitialiser) decl.getValue();
    ctx.opsGenerator.createArray(out, arrayType, arrInit.getSize());
    for (int i = 0; i < arrInit.getValues().size(); i++) {
      out.write(DUP);
      ctx.opsGenerator.pushInteger(out, i);
      ctx.exprGenerator.eval(out, variables, type, arrInit.getValues().get(i));
      out.write(storeOpCode);
    }
    ctx.opsGenerator.assignArray(out, variables, type, decl.getArray(), decl.getName());
    return out;
  }
}
