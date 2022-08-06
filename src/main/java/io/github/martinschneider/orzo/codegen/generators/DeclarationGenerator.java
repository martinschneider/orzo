package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
import static io.github.martinschneider.orzo.codegen.OpCodes.NEWARRAY;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getArrayType;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getStoreOpCode;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.util.List;

public class DeclarationGenerator implements StatementGenerator<ParallelDeclaration> {
  private static final int ZERO = 0;
  private static final String LOG_NAME = "generate declaration code";

  public DeclarationGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  private CGContext ctx;

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, ParallelDeclaration pDecl) {
    for (Declaration decl : pDecl.declarations) {
      if (decl.arrDim > 0) {
        return generateArray(out, ctx.classIdMap, method, decl);
      }
      if (decl.val != null) {
        VariableInfo varInfo = ctx.classIdMap.variables.get(decl.name);
        if (decl.isField && !varInfo.accFlags.contains(AccessFlag.ACC_STATIC)) {
          ctx.loadGen.loadReference(out, ctx.classIdMap.variables.get(decl.name).objectRef);
        }
        ctx.exprGen.eval(out, decl.type, decl.val);
      } else {
        ctx.pushGen.push(out, decl.type, ZERO);
      }
      ctx.assignGen.assign(out, ctx.classIdMap.variables, decl.type, decl.name);
    }
    return out;
  }

  public HasOutput generateArray(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, Method method, Declaration decl) {
    // TODO: handle method calls in array declaration
    if (decl.val == null || !(decl.val instanceof ArrayInit)) {
      ctx.errors.addError(
          LOG_NAME,
          "invalid array initialiser " + decl.val,
          new RuntimeException().getStackTrace());
      return out;
    }
    String type = decl.type;
    byte arrayType = getArrayType(type);
    byte storeOpCode = getStoreOpCode(type);
    ArrayInit arrInit = (ArrayInit) decl.val;
    createArray(out, classIdMap, arrayType, arrInit.dims);
    // multi-dim array
    if (arrInit.vals.size() >= 2) {
      // TODO
    }
    // one-dim array
    else if (arrInit.vals.size() == 1) {
      for (int i = 0; i < arrInit.vals.get(0).size(); i++) {
        out.write(DUP);
        ctx.pushGen.push(out, INT, i);
        ctx.exprGen.eval(out, type, arrInit.vals.get(0).get(i));
        out.write(storeOpCode);
      }
    }
    ctx.assignGen.assignArray(out, classIdMap, type, decl.arrDim, decl.name);
    return out;
  }

  private void createArray(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, byte arrayType, List<Expression> dims) {
    if (dims.size() == 1) {
      ctx.exprGen.eval(out, INT, dims.get(0));
      out.write(NEWARRAY);
      out.write(arrayType);
    } else {
      // TODO:
    }
  }
}
