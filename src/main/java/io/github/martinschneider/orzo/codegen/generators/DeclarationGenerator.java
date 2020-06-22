package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
import static io.github.martinschneider.orzo.codegen.OpCodes.NEWARRAY;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getArrayType;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getStoreOpCode;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.List;

public class DeclarationGenerator implements StatementGenerator {
  private static final int ZERO = 0;
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
      ctx.exprGen.eval(out, variables, decl.type, decl.val);
    } else {
      ctx.pushGen.push(out, decl.type, ZERO);
    }
    ctx.assignGen.assign(out, variables, decl.type, decl.name);
    return out;
  }

  public HasOutput generateArray(
      DynamicByteArray out, VariableMap variables, Method method, Declaration decl) {
    if (decl.val == null || !(decl.val instanceof ArrayInit)) {
      ctx.errors.addError(LOG_NAME, "invalid array initialiser" + decl.val);
      return out;
    }
    String type = decl.type;
    byte arrayType = getArrayType(type);
    byte storeOpCode = getStoreOpCode(type);
    ArrayInit arrInit = (ArrayInit) decl.val;
    createArray(out, arrayType, arrInit.dims);
    // multi-dim array
    if (arrInit.vals.size() >= 2) {
      // TODO
    }
    // one-dim array
    else if (arrInit.vals.size() == 1) {
      for (int i = 0; i < arrInit.vals.get(0).size(); i++) {
        out.write(DUP);
        ctx.pushGen.push(out, INT, i);
        ctx.exprGen.eval(out, variables, type, arrInit.vals.get(0).get(i));
        out.write(storeOpCode);
      }
    }
    ctx.assignGen.assignArray(out, variables, type, decl.arrDim, decl.name);
    return out;
  }

  private void createArray(DynamicByteArray out, byte arrayType, List<Integer> dims) {
    if (dims.size() == 1) {
      ctx.pushGen.push(out, INT, dims.get(0));
      out.write(NEWARRAY);
      out.write(arrayType);
    } else {
      // TODO:
    }
  }
}
