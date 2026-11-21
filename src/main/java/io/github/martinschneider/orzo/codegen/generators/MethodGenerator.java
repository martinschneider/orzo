package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.*;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.*;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.GenericTypes;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.StackMapTableBuilder;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.parser.productions.Break;
import io.github.martinschneider.orzo.parser.productions.CatchBlock;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Constructor;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.IfBlock;
import io.github.martinschneider.orzo.parser.productions.IfStatement;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import io.github.martinschneider.orzo.parser.productions.ReturnStatement;
import io.github.martinschneider.orzo.parser.productions.Statement;
import io.github.martinschneider.orzo.parser.productions.SwitchCase;
import io.github.martinschneider.orzo.parser.productions.SwitchStatement;
import io.github.martinschneider.orzo.parser.productions.ThrowStatement;
import io.github.martinschneider.orzo.parser.productions.TryStatement;
import io.github.martinschneider.orzo.parser.productions.WhileStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodGenerator {
  private static final String LOG_NAME = "method code generator";

  public CGContext ctx;

  public MethodGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public HasOutput generate(HasOutput out, Method method, Clazz clazz) {
    out.write(method.accessFlags(clazz.isInterface));
    out.write(ctx.constPool.indexOf(CONSTANT_UTF8, method.name.val));
    out.write(
        ctx.constPool.indexOf(
            CONSTANT_UTF8, TypeUtils.methodDescr(method, ctx.allClazzes, ctx.clazz)));
    DynamicByteArray methodOut = new DynamicByteArray();
    ctx.exceptionTable.clear();
    ctx.exceptionHandlerType.clear();
    String signature = GenericTypes.methodSignature(method, ctx.allClazzes, ctx.clazz);
    if (!clazz.isInterface) {
      out.write((short) (signature != null ? 2 : 1)); // attribute size
      writeSignature(out, signature);
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "Code"));
      boolean returned = false;

      // Handle special enum methods
      if (clazz.isEnum && generateEnumMethod(methodOut, method, clazz)) {
        returned = true;
      } else {
        boolean enumPrologue = clazz.isEnum && generateEnumPrologue(methodOut, method, clazz);
        if (method instanceof Constructor
            && !enumPrologue
            && !startsWithCallToSuper(method.body)
            && !startsWithCallToThis(method.body)) {
          ctx.methodCallGen.callSuperConstr(methodOut);
        }
        for (Statement stmt : method.body) {
          generateCode(methodOut, method, stmt);
          if (stmt instanceof ReturnStatement || stmt instanceof ThrowStatement) {
            returned = true;
          } else if (stmt instanceof TryStatement) {
            returned = tryStatementAlwaysReturns((TryStatement) stmt);
          } else if (stmt instanceof IfStatement) {
            returned = ifStatementAlwaysReturns((IfStatement) stmt);
          } else if (stmt instanceof SwitchStatement) {
            returned = switchStatementAlwaysReturns((SwitchStatement) stmt);
          } else if (stmt instanceof WhileStatement) {
            returned = whileStatementAlwaysReturns((WhileStatement) stmt);
          } else {
            returned = false;
          }
        }
      }

      if (!returned) {
        methodOut.write(RETURN);
      }
      byte[] stackMapBytes =
          StackMapTableBuilder.buildFromBytecode(methodOut.getBytes(), method, ctx, ctx.constPool);
      int codeLen = methodOut.size();
      int exTableSize = 8 * ctx.exceptionTable.size();
      int attrSize =
          12 + codeLen + exTableSize + (stackMapBytes != null ? stackMapBytes.length : 0);
      out.write(attrSize); // stack size (2) + local var size (2) + code size (4) +
      // exception table size (2) + attribute count size (2) + optional StackMapTable
      short maxStackSize =
          (short)
              StackMapTableBuilder.computeMaxStack(
                  methodOut.getBytes(),
                  ctx.constPool,
                  ctx.exceptionTable,
                  ctx.exceptionHandlerType);
      out.write(maxStackSize); // max stack size
      out.write((short) (ctx.classIdMap.variables.localSizeMax + 1)); // max local var size
      out.write(codeLen);
      out.write(methodOut.flush());
      out.write((short) ctx.exceptionTable.size()); // exception table count
      for (int[] entry : ctx.exceptionTable) {
        out.write((short) entry[0]); // start_pc
        out.write((short) entry[1]); // end_pc
        out.write((short) entry[2]); // handler_pc
        out.write((short) entry[3]); // catch_type (CP index)
      }
      if (stackMapBytes != null) {
        out.write((short) 1); // 1 attribute: StackMapTable
        out.write(stackMapBytes);
      } else {
        out.write((short) 0); // no attributes
      }
    } else {
      out.write((short) (signature != null ? 1 : 0)); // attribute size
      writeSignature(out, signature);
      out.write(methodOut.flush());
    }
    ctx.opStack.reset();
    return out;
  }

  // https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.9
  private void writeSignature(HasOutput out, String signature) {
    if (signature != null) {
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, "Signature"));
      out.write(2);
      out.write(ctx.constPool.indexOf(CONSTANT_UTF8, signature));
    }
  }

  private static boolean ifStatementAlwaysReturns(IfStatement stmt) {
    if (!stmt.hasElse) {
      return false;
    }
    for (IfBlock block : stmt.ifBlks) {
      if (!endsWithReturn(block.body)) {
        return false;
      }
    }
    return true;
  }

  private static boolean tryStatementAlwaysReturns(TryStatement stmt) {
    if (stmt.finallyBody != null && alwaysReturns(stmt.finallyBody)) {
      return true;
    }
    if (!endsWithReturn(stmt.tryBody)) {
      return false;
    }
    for (CatchBlock cb : stmt.catchBlocks) {
      if (!endsWithReturn(cb.body)) {
        return false;
      }
    }
    return true;
  }

  private static boolean switchStatementAlwaysReturns(SwitchStatement stmt) {
    boolean hasDefault = false;
    for (SwitchCase c : stmt.cases) {
      if (c.labels == null || c.labels.isEmpty()) {
        hasDefault = true;
      }
      if (!endsWithReturn(c.body)) {
        return false;
      }
    }
    return hasDefault;
  }

  // Whether a statement sequence never completes normally (ends with a return/throw, or with an
  // if/else, try, switch or "while (true)" whose every path does), so that no code may follow it.
  public static boolean alwaysReturns(List<Statement> body) {
    if (body == null || body.isEmpty()) {
      return false;
    }
    Statement last = body.get(body.size() - 1);
    if (last instanceof ReturnStatement || last instanceof ThrowStatement) {
      return true;
    } else if (last instanceof IfStatement) {
      return ifStatementAlwaysReturns((IfStatement) last);
    } else if (last instanceof TryStatement) {
      return tryStatementAlwaysReturns((TryStatement) last);
    } else if (last instanceof SwitchStatement) {
      return switchStatementAlwaysReturns((SwitchStatement) last);
    } else if (last instanceof WhileStatement) {
      return whileStatementAlwaysReturns((WhileStatement) last);
    }
    return false;
  }

  private static boolean endsWithReturn(List<Statement> body) {
    return alwaysReturns(body);
  }

  // "while (true) { ... }" with no reachable "break" cannot complete normally (JLS 14.21), so
  // javac requires no trailing return after it. Only the literal-"true" condition case is
  // recognized - any other condition could exit normally and needs a real trailing return.
  private static boolean whileStatementAlwaysReturns(WhileStatement stmt) {
    return isLiteralTrue(stmt.cond) && !containsBreak(stmt.body);
  }

  public static boolean isLiteralTrue(Expression cond) {
    return cond != null
        && cond.tokens.size() == 1
        && cond.tokens.get(0) instanceof BoolLiteral
        && Boolean.TRUE.equals(((BoolLiteral) cond.tokens.get(0)).val);
  }

  // Looks for a "break" that would target THIS loop - does not recurse into nested
  // loops/switches, since a break inside one of those targets that construct instead.
  private static boolean containsBreak(List<Statement> body) {
    if (body == null) {
      return false;
    }
    for (Statement stmt : body) {
      if (stmt instanceof Break) {
        return true;
      } else if (stmt instanceof IfStatement) {
        for (IfBlock block : ((IfStatement) stmt).ifBlks) {
          if (containsBreak(block.body)) {
            return true;
          }
        }
      } else if (stmt instanceof TryStatement) {
        TryStatement tryStmt = (TryStatement) stmt;
        if (containsBreak(tryStmt.tryBody)) {
          return true;
        }
        for (CatchBlock cb : tryStmt.catchBlocks) {
          if (containsBreak(cb.body)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public boolean startsWithCallToSuper(List<Statement> body) {
    if (body.isEmpty() || !(body.get(0) instanceof MethodCall)) {
      return false;
    }
    MethodCall call = (MethodCall) body.get(0);
    return "super".equals(call.name);
  }

  public boolean startsWithCallToThis(List<Statement> body) {
    if (body.isEmpty() || !(body.get(0) instanceof MethodCall)) {
      return false;
    }
    MethodCall call = (MethodCall) body.get(0);
    return "this".equals(call.name);
  }

  private void generateCode(DynamicByteArray out, Method method, Statement stmt) {
    ctx.delegator.generate(out, method, stmt);
  }

  // Generate bytecode for special enum methods
  private boolean generateEnumMethod(DynamicByteArray out, Method method, Clazz clazz) {
    String methodName = method.name.val.toString();

    if ("values".equals(methodName)) {
      generateValuesMethod(out, clazz);
      return true;
    } else if ("valueOf".equals(methodName)) {
      generateValueOfMethod(out, clazz);
      return true;
    } else if ("$values".equals(methodName)) {
      generateDollarValuesMethod(out, clazz);
      return true;
    }
    return false;
  }

  // The synthesized start of an enum's <clinit> (creating the constants) or constructor (calling
  // Enum.<init>(name, ordinal)); the method body is generated after it as usual.
  private boolean generateEnumPrologue(DynamicByteArray out, Method method, Clazz clazz) {
    String methodName = method.name.val.toString();
    if ("<clinit>".equals(methodName)) {
      generateEnumConstants(out, clazz);
      return true;
    } else if ("<init>".equals(methodName)) {
      if (!startsWithCallToThis(method.body)) {
        callEnumSuperConstructor(out);
      }
      return true;
    }
    return false;
  }

  // Generate values() method: return $VALUES.clone()
  private void generateValuesMethod(DynamicByteArray out, Clazz clazz) {
    // Get static field $VALUES: getstatic $VALUES
    out.write(GETSTATIC);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_FIELDREF, clazz.fqn('/'), "$VALUES", "[L" + clazz.fqn('/') + ";"));

    // Call clone() method: invokevirtual clone
    out.write(INVOKEVIRTUAL);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_METHODREF, "java/lang/Object", "clone", "()Ljava/lang/Object;"));

    // Cast to correct array type: checkcast
    out.write(CHECKCAST);
    out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, "[L" + clazz.fqn('/') + ";"));

    // Return: areturn
    out.write(ARETURN);
  }

  // Generate valueOf(String) method: return Enum.valueOf(Class, String)
  private void generateValueOfMethod(DynamicByteArray out, Clazz clazz) {
    // Load class constant: ldc EnumType.class
    short classIndex = ctx.constPool.indexOf(CONSTANT_CLASS, clazz.fqn('/'));
    if (classIndex <= 255) {
      out.write(LDC);
      out.write((byte) classIndex);
    } else {
      out.write(LDC_W);
      out.write(classIndex);
    }

    // Load string parameter: aload_0
    out.write(ALOAD_0);

    // Call Enum.valueOf: invokestatic Enum.valueOf
    out.write(INVOKESTATIC);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_METHODREF,
                "java/lang/Enum",
                "valueOf",
                "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;"));

    // Cast to correct enum type: checkcast
    out.write(CHECKCAST);
    out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, clazz.fqn('/')));

    // Return: areturn
    out.write(ARETURN);
  }

  // Generate $values() helper method: return new array with all enum constants
  private void generateDollarValuesMethod(DynamicByteArray out, Clazz clazz) {
    // Get enum constants from the first field (which should be the enum constants)
    List<Declaration> enumConstants = getEnumConstants(clazz);
    int numConstants = enumConstants.size();

    // Create new array: new EnumType[numConstants]
    if (numConstants <= 127) {
      out.write(BIPUSH);
      out.write((byte) numConstants);
    } else {
      out.write(SIPUSH);
      out.write((short) numConstants);
    }
    out.write(ANEWARRAY);
    out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, clazz.fqn('/')));

    // Populate array with enum constants
    for (int i = 0; i < numConstants; i++) {
      Declaration constant = enumConstants.get(i);
      out.write(DUP); // Duplicate array reference

      // Array index
      if (i <= 127) {
        if (i <= 5) {
          out.write((byte) (ICONST_0 + i)); // ICONST_0 to ICONST_5
        } else {
          out.write(BIPUSH);
          out.write((byte) i);
        }
      } else {
        out.write(SIPUSH);
        out.write((short) i);
      }

      // Get enum constant: getstatic EnumType.CONSTANT_NAME
      out.write(GETSTATIC);
      out.write(
          (short)
              ctx.constPool.indexOf(
                  CONSTANT_FIELDREF,
                  clazz.fqn('/'),
                  constant.name.id().toString(),
                  "L" + clazz.fqn('/') + ";"));

      // Store in array: aastore
      out.write(AASTORE);
    }

    // Return array: areturn
    out.write(ARETURN);
  }

  // Generate <clinit> static initializer: create all enum instances and $VALUES array
  private void generateEnumConstants(DynamicByteArray out, Clazz clazz) {
    List<Declaration> enumConstants = getEnumConstants(clazz);

    // Create each enum constant
    for (int i = 0; i < enumConstants.size(); i++) {
      Declaration constant = enumConstants.get(i);
      Method constructor = findEnumConstructor(clazz, constant.enumArgs);
      if (constructor == null) {
        ctx.errors.addError(
            LOG_NAME,
            String.format(
                "no constructor for enum constant %s%s", constant.name, constant.enumArgs),
            new RuntimeException().getStackTrace());
        continue;
      }

      // Create new enum instance: new EnumType("CONSTANT_NAME", ordinal)
      out.write(NEW);
      out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, clazz.fqn('/')));
      out.write(DUP);

      // Push string name of constant
      short stringIndex = ctx.constPool.indexOf(CONSTANT_STRING, constant.name.id().toString());
      if (stringIndex <= 255) {
        out.write(LDC);
        out.write((byte) stringIndex);
      } else {
        out.write(LDC_W);
        out.write(stringIndex);
      }

      // Push ordinal (index)
      if (i <= 5) {
        out.write((byte) (ICONST_0 + i)); // ICONST_0 to ICONST_5
      } else if (i <= 127) {
        out.write(BIPUSH);
        out.write((byte) i);
      } else {
        out.write(SIPUSH);
        out.write((short) i);
      }

      // the constructor arguments, e.g. RED(1, "r")
      // (the opStack doesn't track the new instance, its copy, name and ordinal)
      for (int j = 0; j < constant.enumArgs.size(); j++) {
        String paramType = constructor.args.get(j + 2).type;
        ExpressionResult arg = ctx.exprGen.eval(out, paramType, constant.enumArgs.get(j));
        ctx.basicGen.convert1(out, arg.type, paramType);
        ctx.opStack.pop();
      }
      out.write(INVOKESPECIAL);
      out.write(
          (short)
              ctx.constPool.indexOf(
                  CONSTANT_METHODREF,
                  clazz.fqn('/'),
                  "<init>",
                  TypeUtils.methodDescr(constructor, ctx.allClazzes, ctx.clazz)));

      // Store in static field: putstatic CONSTANT_NAME
      out.write(PUTSTATIC);
      out.write(
          (short)
              ctx.constPool.indexOf(
                  CONSTANT_FIELDREF,
                  clazz.fqn('/'),
                  constant.name.id().toString(),
                  "L" + clazz.fqn('/') + ";"));
    }

    // Create and store $VALUES array: putstatic $VALUES, $values()
    out.write(INVOKESTATIC);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_METHODREF, clazz.fqn('/'), "$values", "()[L" + clazz.fqn('/') + ";"));
    out.write(PUTSTATIC);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_FIELDREF, clazz.fqn('/'), "$VALUES", "[L" + clazz.fqn('/') + ";"));
  }

  // The constructor for an enum constant's arguments (its parameters include name and ordinal)
  private Method findEnumConstructor(Clazz clazz, List<Expression> args) {
    List<String> argTypes = new ArrayList<>();
    argTypes.add("java.lang.String");
    argTypes.add(INT);
    for (Expression arg : args) {
      argTypes.add(new NumExprTypeDecider(ctx).getType(ctx.classIdMap, arg));
    }
    Method fallback = null;
    for (Method constructor : clazz.getConstructors()) {
      if (constructor.args.size() != argTypes.size()) {
        continue;
      }
      boolean matches = true;
      for (int i = 2; matches && i < argTypes.size(); i++) {
        matches =
            TypeUtils.assignableTo(argTypes.get(i)).contains(constructor.args.get(i).type)
                || argTypes.get(i).equals(constructor.args.get(i).type);
      }
      if (matches) {
        return constructor;
      }
      if (fallback == null) {
        fallback = constructor; // e.g. an argument type that isn't known exactly
      }
    }
    return fallback;
  }

  // "super(name, ordinal)" at the start of an enum constructor (its first two parameters)
  private void callEnumSuperConstructor(DynamicByteArray out) {
    out.write(ALOAD_0);
    out.write(ALOAD_1);
    out.write(ILOAD_2);
    out.write(INVOKESPECIAL);
    out.write(
        (short)
            ctx.constPool.indexOf(
                CONSTANT_METHODREF, "java/lang/Enum", "<init>", "(Ljava/lang/String;I)V"));
  }

  private List<Declaration> getEnumConstants(Clazz clazz) {
    if (clazz.fields != null && !clazz.fields.isEmpty()) {
      // Enum constants are stored in the first ParallelDeclaration
      ParallelDeclaration enumConstantsDecl = clazz.fields.get(0);
      return enumConstantsDecl.declarations;
    }
    return Collections.emptyList();
  }
}
