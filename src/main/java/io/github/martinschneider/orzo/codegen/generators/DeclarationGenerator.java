package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ACONST_NULL;
import static io.github.martinschneider.orzo.codegen.OpCodes.CHECKCAST;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.LambdaExpr;
import io.github.martinschneider.orzo.lexer.tokens.Type;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.ArrayInit;
import io.github.martinschneider.orzo.parser.productions.ConstructorCall;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;

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
      if (Type.VAR.equals(decl.type) && !inferType(decl, pDecl.declarations.size())) {
        continue;
      }
      if (!decl.isField && !TypeUtils.typeExists(decl.type, ctx.allClazzes, ctx.clazz)) {
        ctx.errors.addError(
            "declaration generator",
            "cannot find class " + simpleName(decl.type),
            new RuntimeException().getStackTrace());
        continue;
      }
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
        String valueType = new NumExprTypeDecider(ctx).getType(ctx.classIdMap, decl.val);
        if (TypeUtils.isIncompatible(valueType, decl.type)) {
          ctx.errors.addError(
              "declaration generator",
              String.format(
                  "incompatible types: %s cannot be converted to %s",
                  TypeUtils.displayName(valueType), TypeUtils.displayName(decl.type)),
              new RuntimeException().getStackTrace());
          continue;
        }
        if (decl.val.tokens.size() == 1 && decl.val.tokens.get(0) instanceof LambdaExpr) {
          // (its parameters get their types from the type arguments, e.g. Function<String, R>)
          ctx.exprGen.lambdaTargetGeneric = decl.genericType;
        }
        ctx.exprGen.eval(out, decl.type, decl.val);
        String actualType = ctx.opStack.type();
        if (decl.arrDim == 0) {
          ctx.basicGen.checkAssignable(valueType, actualType, decl.type);
        }
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
        // unboxing, e.g. "int x = deque.pop();" on a Deque<Integer>
        boolean needsUnbox =
            actualType != null
                && !TypeUtils.isPrimitive(actualType)
                && TypeUtils.isPrimitive(decl.type);
        if (needsAutobox || needsRefCast || needsUnbox) {
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
      if (actualType != null && !actualType.startsWith("[")) {
        // e.g. an erased generic return (Map<K, V[]>.get() returns Object): cast to the array
        // (harmless for null or a value that already is such an array)
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
    ArrayInit arrInit = (ArrayInit) decl.val;
    if (decl.isField) {
      VariableInfo varInfo = ctx.classIdMap.variables.get(decl.name);
      if (varInfo != null && !varInfo.accFlags.contains(AccessFlag.ACC_STATIC)) {
        ctx.loadGen.loadOwner(out, varInfo);
      }
    }
    ctx.exprGen.generateArray(out, classIdMap, arrInit);
    ctx.assignGen.assignArray(out, classIdMap, type, decl.arrDim, decl.name);
    return out;
  }

  // The type of "var x = value;": the value's (an array's element type and dimensions)
  private boolean inferType(Declaration decl, int declarators) {
    if (declarators > 1) {
      ctx.errors.addError(
          "declaration generator",
          "'var' is not allowed in a compound declaration",
          new RuntimeException().getStackTrace());
      return false;
    }
    NumExprTypeDecider decider = new NumExprTypeDecider(ctx);
    String type =
        (decl.val == null || decl.val instanceof ArrayInit && ((ArrayInit) decl.val).type == null)
            ? null
            : decider.getType(ctx.classIdMap, decl.val);
    if (decl.val != null
        && decl.val.tokens.size() == 1
        && decl.val.tokens.get(0) instanceof LambdaExpr) {
      type = null; // (a lambda needs a target type)
    }
    if (type == null || type.equals(NumExprTypeDecider.NULL_TYPE)) {
      ctx.errors.addError(
          "declaration generator",
          "cannot infer type for local variable " + decl.name,
          new RuntimeException().getStackTrace());
      return false;
    }
    int dims = 0;
    while (type.startsWith("[")) {
      type = type.substring(1);
      dims++;
    }
    if (dims > 0 && type.startsWith("L") && type.endsWith(";")) {
      type = type.substring(1, type.length() - 1); // e.g. "[Ljava.lang.reflect.Method;"
    }
    decl.type = type;
    decl.arrDim = dims;
    decl.genericType = (dims == 0) ? genericType(type, decl.val, decider) : null;
    return true;
  }

  // The generic type of a value, e.g. "java.util.ArrayList<String>" for "new ArrayList<String>()",
  // a local variable with a generic type or a call returning one (null if unknown)
  private String genericType(String type, Expression value, NumExprTypeDecider decider) {
    if (value.tokens.size() != 1 || !(value.tokens.get(0) instanceof Identifier)) {
      return null;
    }
    Identifier root = (Identifier) value.tokens.get(0);
    if (root instanceof ConstructorCall && root.next == null) {
      ConstructorCall call = (ConstructorCall) root;
      return (call.typeArgs == null) ? null : type.replace('/', '.') + call.typeArgs;
    } else if (!(root instanceof MethodCall) && root.next == null) {
      VariableInfo var = ctx.classIdMap.variables.get(root);
      return (var != null) ? var.genericType : null;
    }
    return decider.chainGeneric;
  }

  // e.g. "Foo" for "pkg.Foo"
  static String simpleName(String type) {
    return type.substring(type.lastIndexOf('.') + 1);
  }
}
