package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ACONST_NULL;
import static io.github.martinschneider.orzo.codegen.OpCodes.ANEWARRAY;
import static io.github.martinschneider.orzo.codegen.OpCodes.CHECKCAST;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
import static io.github.martinschneider.orzo.codegen.OpCodes.NEWARRAY;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getArrayType;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getStoreOpCode;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.TypeUtils;
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
        VariableInfo fieldVarInfo =
            decl.isField ? ctx.classIdMap.variables.fieldMap.get(decl.name.id().toString()) : null;
        VariableInfo varInfo =
            fieldVarInfo != null ? fieldVarInfo : ctx.classIdMap.variables.get(decl.name);
        if (decl.isField && varInfo != null && !varInfo.accFlags.contains(AccessFlag.ACC_STATIC)) {
          ctx.loadGen.loadReference(out, (short) 0);
        }
        ctx.exprGen.eval(out, decl.type, decl.val);
        String actualType = ctx.opStack.type();
        // Primitive<->primitive mismatches are already handled by eval()'s own widening (it was
        // given decl.type as its target-type hint). The two remaining cases needing an explicit
        // conversion: autobox (e.g. "Integer x = 5;" — actualType primitive, decl.type a boxed
        // reference type), or a CHECKCAST for a generic/erased method returning a wider reference
        // type than the variable's declared type (both sides reference types but unequal).
        boolean needsAutobox =
            actualType != null
                && TypeUtils.isPrimitive(actualType)
                && !TypeUtils.isPrimitive(decl.type);
        boolean needsRefCast =
            actualType != null
                && !actualType.equals(decl.type)
                && !TypeUtils.isPrimitive(actualType)
                && !TypeUtils.isPrimitive(decl.type);
        if (needsAutobox || needsRefCast) {
          ctx.basicGen.convert1(out, actualType, decl.type);
        }
      } else {
        if (TypeUtils.isPrimitive(decl.type)) {
          ctx.pushGen.push(out, decl.type, ZERO);
        } else {
          out.write(ACONST_NULL);
          ctx.opStack.push(decl.type);
        }
      }
      if (decl.isField) {
        VariableInfo fieldVarInfo =
            ctx.classIdMap.variables.fieldMap.get(decl.name.id().toString());
        if (fieldVarInfo != null) {
          ctx.storeGen.store(out, fieldVarInfo);
        }
      } else {
        ctx.assignGen.assign(out, ctx.classIdMap.variables, decl.type, decl.name);
        ctx.classIdMap.variables.localMap.get(decl.name.val.toString()).genericType =
            decl.genericType;
      }
    }
    return out;
  }

  public HasOutput generateArray(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, Method method, Declaration decl) {
    if (decl.val == null) {
      out.write(ACONST_NULL);
      ctx.assignGen.assignArray(out, classIdMap, decl.type, decl.arrDim, decl.name);
      return out;
    }
    if (!(decl.val instanceof ArrayInit)) {
      ctx.exprGen.eval(out, decl.type, decl.val);
      String actualType = ctx.opStack.type();
      if (actualType != null && !actualType.startsWith("[") && !REF.equals(actualType)) {
        // e.g. an erased generic return (Map<K, V[]>.get() returns Object): cast to the array
        String arrayClass = TypeUtils.descr(decl.type, decl.arrDim);
        ctx.constPool.addClass(arrayClass);
        out.write(CHECKCAST);
        out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, arrayClass));
        ctx.opStack.pop();
        ctx.opStack.push(REF);
      }
      ctx.assignGen.assignArray(out, classIdMap, decl.type, decl.arrDim, decl.name);
      return out;
    }
    String type = decl.type;
    byte storeOpCode = getStoreOpCode(type);
    ArrayInit arrInit = (ArrayInit) decl.val;
    if (decl.isField) {
      VariableInfo varInfo = ctx.classIdMap.variables.get(decl.name);
      if (varInfo != null && !varInfo.accFlags.contains(AccessFlag.ACC_STATIC)) {
        ctx.loadGen.loadReference(out, varInfo.objectRef);
      }
    }
    createArray(out, classIdMap, type, arrInit.dims);
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
      DynamicByteArray out, GlobalIdentifierMap classIdMap, String type, List<Expression> dims) {
    if (dims.size() == 1) {
      ctx.exprGen.eval(out, INT, dims.get(0));
      if (TypeUtils.isPrimitive(type)) {
        out.write(NEWARRAY);
        out.write(getArrayType(type));
      } else {
        String jvmClass = jvmClassName(type);
        ctx.constPool.addClass(jvmClass);
        out.write(ANEWARRAY);
        out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, jvmClass));
      }
      // NEWARRAY/ANEWARRAY consumes the dimension int and produces an array reference;
      // opStack still had the dimension's int on top, so swap it for the resulting ref type.
      ctx.opStack.pop();
      ctx.opStack.push(REF);
    } else {
      // TODO:
    }
  }

  private String jvmClassName(String type) {
    String descr = TypeUtils.descr(type);
    return (descr.startsWith("L") && descr.endsWith(";"))
        ? descr.substring(1, descr.length() - 1)
        : descr;
  }
}
