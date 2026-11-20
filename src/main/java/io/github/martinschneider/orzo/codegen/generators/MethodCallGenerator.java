package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.POP;
import static io.github.martinschneider.orzo.codegen.OpCodes.POP2;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;
import static io.github.martinschneider.orzo.util.FactoryHelper.defaultConstr;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.GenericTypes;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.codegen.identifier.VariableInfo;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodCallGenerator implements StatementGenerator<MethodCall> {
  private static final String LOGGER_NAME = "method call code generator";
  public CGContext ctx;
  // Generic return type of the most recently generated call, e.g. "java.util.List<a.b.Foo>"
  // (null if unknown/not generic). Consumed by generateChained() for the next call in a chain.
  public String lastGenericType;

  public MethodCallGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public String generate(
      DynamicByteArray out, GlobalIdentifierMap classIdMap, MethodCall methodCall) {
    List<String> types = new ArrayList<>();
    for (Expression exp : methodCall.params) {
      types.add(new NumExprTypeDecider(ctx).getType(classIdMap, exp));
    }
    String methodName = methodCall.name.toString();
    Method method = findMatchingMethod(methodName, types);
    // For dotted names (e.g. identifier.toString), try to resolve via the receiver variable's type
    String receiverName = null;
    String[] receiverChainParts = null;
    if (method == null && methodName.contains(".")) {
      int dot = methodName.lastIndexOf('.');
      receiverName = methodName.substring(0, dot);
      String simpleMethod = methodName.substring(dot + 1);
      io.github.martinschneider.orzo.codegen.identifier.VariableInfo receiverVar =
          classIdMap.variables.localMap.get(receiverName);
      if (receiverVar == null) {
        receiverVar = classIdMap.variables.fieldMap.get(receiverName);
      }
      if (receiverVar != null && receiverVar.type != null) {
        // Try simple class name prefix
        int lastDotInType = receiverVar.type.lastIndexOf('.');
        String simpleType =
            (lastDotInType >= 0) ? receiverVar.type.substring(lastDotInType + 1) : receiverVar.type;
        method = findMatchingMethod(simpleType + "." + simpleMethod, types);
        if (method == null) {
          method = findMatchingMethod(receiverVar.type + "." + simpleMethod, types);
        }
        // TODO: the toString/hashCode/getClass/equals fallbacks below are hard-coded and fragile.
        // All Object methods should be resolved generically via reflection on java.lang.Object
        // so that new Object methods don't need individual special cases here.
        // Fall back to java/lang/Object for toString(), hashCode() etc.
        if (method == null) {
          if (types.isEmpty()) {
            if ("toString".equals(simpleMethod)) {
              method = new Method("java/lang/Object", "toString", STRING, new ArrayList<>());
            } else if ("hashCode".equals(simpleMethod)) {
              method = new Method("java/lang/Object", "hashCode", INT, new ArrayList<>());
            } else if ("getClass".equals(simpleMethod)) {
              method =
                  new Method(
                      "java/lang/Object", "getClass", "Ljava/lang/Class;", new ArrayList<>());
            }
          } else if (types.size() == 1 && "equals".equals(simpleMethod)) {
            method =
                new Method("java/lang/Object", "equals", BOOLEAN, List.of("Ljava/lang/Object;"));
          }
        }
        // Reflection fallback for external Java stdlib methods
        if (method == null) {
          method = findMethodViaReflectionTyped(receiverVar.type, simpleMethod, types);
          if (method == null) {
            method = findMethodViaReflection(receiverVar.type, simpleMethod, types.size());
          }
        }
      }
      // Handle super.method() calls - invokespecial on parent class
      if (method == null && "super".equals(receiverName)) {
        String superClass =
            (ctx.clazz != null
                    && ctx.clazz.baseClass != null
                    && !"java.lang.Object".equals(ctx.clazz.baseClass))
                ? ctx.clazz.baseClass.replace('.', '/')
                : "java/lang/Object";
        if (types.isEmpty() && "toString".equals(simpleMethod)) {
          method = new Method(superClass, "toString", STRING, new ArrayList<>());
        } else if (types.isEmpty() && "hashCode".equals(simpleMethod)) {
          method = new Method(superClass, "hashCode", INT, new ArrayList<>());
        } else if (types.size() == 1 && "equals".equals(simpleMethod)) {
          method = new Method(superClass, "equals", BOOLEAN, List.of("Ljava/lang/Object;"));
        }
      }
      // Reflection fallback for class-name-prefixed calls (e.g. ParserContext.build(errors))
      if (method == null && receiverVar == null && !"super".equals(receiverName)) {
        String resolvedClassName = resolveClassName(receiverName);
        method = findMethodViaReflectionTyped(resolvedClassName, simpleMethod, types);
        if (method == null) {
          method = findMethodViaReflection(resolvedClassName, simpleMethod, types.size());
        }
      }
      // Handle a bare statically-imported field used as a receiver (e.g. "import static
      // Type.BASIC_TYPES;" then "BASIC_TYPES.contains(x)") — receiverName has no dot here since
      // the source never names the declaring class, unlike the ClassName.FIELD.method() case
      // below.
      if (method == null && receiverVar == null && ctx.staticFieldMap != null) {
        io.github.martinschneider.orzo.codegen.FieldProcessor.StaticField staticField =
            ctx.staticFieldMap.get(receiverName);
        if (staticField != null) {
          method = findMethodViaReflectionTyped(staticField.fieldType, simpleMethod, types);
          if (method == null) {
            method = findMethodViaReflection(staticField.fieldType, simpleMethod, types.size());
          }
          if (method != null) {
            String jvmClass = staticField.className.replace('.', '/');
            String fieldDescr = TypeUtils.descr(staticField.fieldType);
            ctx.constPool.addClass(jvmClass);
            ctx.constPool.addFieldRef(jvmClass, staticField.fieldName, fieldDescr);
            out.write(io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC);
            out.write(
                ctx.constPool.indexOf(
                    io.github.martinschneider.orzo.codegen.constants.ConstantTypes
                        .CONSTANT_FIELDREF,
                    jvmClass,
                    staticField.fieldName,
                    fieldDescr));
            ctx.opStack.push(staticField.fieldType);
            lastGenericType = staticField.genericType;
            return generateChained(
                out,
                classIdMap,
                new MethodCall(simpleMethod, methodCall.params),
                staticField.fieldType);
          }
        }
      }
      // Resolve receiver as a dotted field chain (e.g., "id.val" → load id, getfield val)
      if (method == null
          && receiverVar == null
          && receiverName != null
          && receiverName.contains(".")) {
        String[] parts = receiverName.split("\\.");
        io.github.martinschneider.orzo.codegen.identifier.VariableInfo chainFirstVar =
            classIdMap.variables.localMap.get(parts[0]);
        if (chainFirstVar == null) {
          chainFirstVar = classIdMap.variables.fieldMap.get(parts[0]);
        }
        if (chainFirstVar == null && "this".equals(parts[0]) && ctx.clazz != null) {
          chainFirstVar =
              new VariableInfo(
                  "this", ctx.clazz.fqn('.'), Collections.emptyList(), false, (short) 0, null);
        }
        if (chainFirstVar != null) {
          String chainType = chainFirstVar.type;
          boolean chainOk = true;
          for (int pi = 1; pi < parts.length && chainOk; pi++) {
            java.util.Map<
                    String, io.github.martinschneider.orzo.codegen.FieldProcessor.InstanceField>
                chainFieldMap =
                    new io.github.martinschneider.orzo.codegen.FieldProcessor()
                        .getInstanceFieldMap(chainType, ctx.allClazzes);
            io.github.martinschneider.orzo.codegen.FieldProcessor.InstanceField cf =
                chainFieldMap.get(parts[pi]);
            if (cf == null) {
              chainOk = false;
            } else {
              chainType = cf.fieldType;
            }
          }
          if (chainOk) {
            receiverChainParts = parts;
            method = findMethodViaReflectionTyped(chainType, simpleMethod, types);
            if (method == null) {
              method = findMethodViaReflection(chainType, simpleMethod, types.size());
            }
          }
        }
        // Handle ClassName.STATIC_FIELD.method() via reflection (e.g.
        // ConstantPool.INT_CONSTANT_TYPES.contains(type))
        if (method == null && chainFirstVar == null && parts.length == 2) {
          String resolvedClass = resolveClassName(parts[0]);
          try {
            Class<?> reflClass = Class.forName(resolvedClass);
            java.lang.reflect.Field reflField = reflClass.getField(parts[1]);
            String fieldType = reflField.getType().getName();
            method = findMethodViaReflectionTyped(fieldType, simpleMethod, types);
            if (method == null) {
              method = findMethodViaReflection(fieldType, simpleMethod, types.size());
            }
            if (method != null) {
              String jvmClass = resolvedClass.replace('.', '/');
              String fieldDescr = TypeUtils.descr(fieldType);
              ctx.constPool.addClass(jvmClass);
              ctx.constPool.addFieldRef(jvmClass, parts[1], fieldDescr);
              out.write(io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC);
              out.write(
                  ctx.constPool.indexOf(
                      io.github.martinschneider.orzo.codegen.constants.ConstantTypes
                          .CONSTANT_FIELDREF,
                      jvmClass,
                      parts[1],
                      fieldDescr));
              ctx.opStack.push(fieldType);
              return generateChained(
                  out, classIdMap, new MethodCall(simpleMethod, methodCall.params), fieldType);
            }
          } catch (ClassNotFoundException | NoSuchFieldException e) {
            // fall through to existing error handling
          }
        }
      }
    }
    // Handle no-receiver Object methods called on implicit 'this'
    if (method == null && !methodName.contains(".")) {
      if ("getClass".equals(methodName) && types.isEmpty()) {
        method = new Method("java/lang/Object", "getClass", "Ljava/lang/Class;", new ArrayList<>());
      }
    }
    if (method == null) {
      ctx.errors.addError(
          LOGGER_NAME,
          (ctx.clazz != null ? ctx.clazz.sourceFile + " " : "")
              + " missing method declaration \""
              + methodName
              + types
              + "\"",
          new RuntimeException().getStackTrace());
      return "";
    }
    boolean isStatic = method.accFlags != null && method.accFlags.contains(AccessFlag.ACC_STATIC);
    String receiverGeneric = null;
    if (!isStatic) {
      if (receiverName != null) {
        // Load the explicit receiver variable
        io.github.martinschneider.orzo.codegen.identifier.VariableInfo receiverVar =
            classIdMap.variables.localMap.get(receiverName);
        if (receiverVar == null) {
          receiverVar = classIdMap.variables.fieldMap.get(receiverName);
        }
        if (receiverVar != null) {
          ctx.loadGen.load(out, receiverVar);
          receiverGeneric = receiverVar.genericType;
        } else if (receiverChainParts != null) {
          io.github.martinschneider.orzo.codegen.identifier.VariableInfo chainVar =
              classIdMap.variables.localMap.get(receiverChainParts[0]);
          if (chainVar == null) {
            chainVar = classIdMap.variables.fieldMap.get(receiverChainParts[0]);
          }
          if (chainVar == null && "this".equals(receiverChainParts[0]) && ctx.clazz != null) {
            chainVar =
                new VariableInfo(
                    "this", ctx.clazz.fqn('.'), Collections.emptyList(), false, (short) 0, null);
          }
          ctx.loadGen.load(out, chainVar);
          String chainType = chainVar.type;
          for (int pi = 1; pi < receiverChainParts.length; pi++) {
            java.util.Map<
                    String, io.github.martinschneider.orzo.codegen.FieldProcessor.InstanceField>
                chainFieldMap =
                    new io.github.martinschneider.orzo.codegen.FieldProcessor()
                        .getInstanceFieldMap(chainType, ctx.allClazzes);
            io.github.martinschneider.orzo.codegen.FieldProcessor.InstanceField cf =
                chainFieldMap.get(receiverChainParts[pi]);
            String cn = cf.className.replace('.', '/');
            String ftd = TypeUtils.descr(cf.fieldType);
            ctx.constPool.addClass(cn);
            ctx.constPool.addFieldRef(cn, cf.fieldName, ftd);
            ctx.loadGen.getField(
                out,
                ctx.constPool.indexOf(
                    io.github.martinschneider.orzo.codegen.constants.ConstantTypes
                        .CONSTANT_FIELDREF,
                    cn,
                    cf.fieldName,
                    ftd));
            ctx.opStack.pop();
            ctx.opStack.push(cf.fieldType);
            chainType = cf.fieldType;
            receiverGeneric = cf.genericType;
          }
        } else {
          ctx.loadGen.loadReference(out, (short) 0);
        }
      } else {
        ctx.loadGen.loadReference(out, (short) 0); // push 'this' for invokevirtual
      }
    }
    pushArgs(out, methodCall.params, types, method);
    if (isStatic) {
      ctx.invokeGen.invokeStatic(out, method);
    } else if ("super".equals(receiverName)) {
      ctx.invokeGen.invokeSpecial(out, method);
    } else if (isInterfaceClass(method.fqClassName)) {
      ctx.invokeGen.invokeInterface(out, method);
    } else {
      ctx.invokeGen.invokeVirtual(out, method);
    }
    return applyGenericReturnType(out, method, receiverGeneric);
  }

  // If the receiver's generic type is known, replace an erased return type (e.g. Object for
  // List<Foo>.get()) with the real one, emitting the CHECKCAST javac would emit.
  private String applyGenericReturnType(
      DynamicByteArray out, Method method, String receiverGeneric) {
    lastGenericType = null;
    String type = method.type;
    if (receiverGeneric == null
        || type == null
        || TypeUtils.isPrimitive(type)
        || type.startsWith("[")
        || VOID.equals(type)) {
      return type;
    }
    List<String> paramTypes = new ArrayList<>();
    for (Argument arg : method.args) {
      paramTypes.add(arg.type);
    }
    String generic =
        GenericTypes.returnType(receiverGeneric, method.name.val.toString(), paramTypes);
    if (generic == null) {
      return type;
    }
    if (generic.indexOf('<') >= 0) {
      lastGenericType = generic;
    }
    String erased = GenericTypes.erasure(generic);
    if ("java.lang.String".equals(erased)) {
      erased = STRING;
    }
    if (erased.equals(type) || "java.lang.Object".equals(erased)) {
      return type;
    }
    ctx.basicGen.convert1(out, type, erased);
    return erased;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, MethodCall methodCall) {
    if ("super".equals(methodCall.name.toString())) {
      if (methodCall.params != null && !methodCall.params.isEmpty()) {
        callSuperConstrWithArgs(out, methodCall);
      } else {
        callSuperConstr(out);
      }
    } else if ("this".equals(methodCall.name.toString())) {
      callThisConstrWithArgs(out, methodCall);
    } else if ("System.out.println".equals(methodCall.name.toString())) {
      // TODO: System.out.println is hard-coded. Generalise to arbitrary getstatic + invokevirtual
      // chains driven by field type reflection so that other PrintStream methods (print, printf,
      // System.err.println, etc.) also work without special-casing.
      for (Expression param : methodCall.params) {
        ctx.invokeGen.getStatic(out, "java/lang/System", "out", "Ljava/io/PrintStream;");
        ExpressionResult result = ctx.exprGen.eval(out, null, param);
        if (result != null) {
          print(out, result.type);
        } else {
          ctx.errors.addError(
              LOGGER_NAME,
              String.format("error evaluating expression %s", param),
              new RuntimeException().getStackTrace());
          return null;
        }
      }
    } else {
      String retType = generate(out, ctx.classIdMap, methodCall);
      // Chained calls as a standalone statement, e.g. "sb.append(x).append(y);" — each
      // subsequent call is invoked on the previous call's return value, left on the stack.
      io.github.martinschneider.orzo.lexer.tokens.Identifier chainNext = methodCall.next;
      while (chainNext instanceof MethodCall) {
        retType = generateChained(out, ctx.classIdMap, (MethodCall) chainNext, retType);
        chainNext = chainNext.next;
      }
      if (retType != null && !retType.isEmpty() && !VOID.equals(retType)) {
        if (LONG.equals(retType) || DOUBLE.equals(retType)) {
          out.write(POP2);
        } else {
          out.write(POP);
        }
        ctx.opStack.pop();
      }
    }
    return out;
  }

  public HasOutput callSuperConstr(HasOutput out) {
    ctx.loadGen.loadReference(out, (short) 0);
    ctx.opStack.push(SHORT);
    String superClass =
        (ctx.clazz.baseClass != null && !ctx.clazz.baseClass.equals("java.lang.Object"))
            ? ctx.clazz.baseClass.replace('.', '/')
            : "java/lang/Object";
    ctx.invokeGen.invokeSpecial(out, defaultConstr(superClass));
    return out;
  }

  public HasOutput callSuperConstrWithArgs(DynamicByteArray out, MethodCall methodCall) {
    ctx.loadGen.loadReference(out, (short) 0);
    ctx.opStack.push(SHORT);
    String superClass =
        (ctx.clazz.baseClass != null && !ctx.clazz.baseClass.equals("java.lang.Object"))
            ? ctx.clazz.baseClass.replace('.', '/')
            : "java/lang/Object";
    // Phase 1: collect argument types without emitting bytecode
    List<String> exprTypes = new ArrayList<>();
    for (Expression param : methodCall.params) {
      exprTypes.add(new NumExprTypeDecider(ctx).getType(ctx.classIdMap, param));
    }
    // Find constructor using type information
    Method superConstr = ctx.exprGen.findMatchingConstructor(superClass, exprTypes);
    if (superConstr == null) {
      superConstr = findConstructorViaReflection(superClass, exprTypes.size());
    }
    if (superConstr == null) {
      List<Argument> constrArgs = new ArrayList<>();
      for (int i = 0; i < exprTypes.size(); i++) {
        constrArgs.add(
            new Argument(
                exprTypes.get(i), io.github.martinschneider.orzo.lexer.tokens.Token.id("p" + i)));
      }
      superConstr =
          new Method(
              superClass.replace('/', '.'),
              List.of(AccessFlag.ACC_PUBLIC),
              "void",
              io.github.martinschneider.orzo.lexer.tokens.Token.id("<init>"),
              constrArgs,
              null);
    }
    // Phase 2: eval each argument and box if needed
    pushArgs(out, methodCall.params, exprTypes, superConstr);
    ctx.invokeGen.invokeSpecial(out, superConstr);
    return out;
  }

  public HasOutput callThisConstrWithArgs(DynamicByteArray out, MethodCall methodCall) {
    ctx.loadGen.loadReference(out, (short) 0);
    ctx.opStack.push(SHORT);
    String thisClass = ctx.clazz.fqn('/');
    List<String> exprTypes = new ArrayList<>();
    for (Expression param : methodCall.params) {
      exprTypes.add(new NumExprTypeDecider(ctx).getType(ctx.classIdMap, param));
    }
    Method thisConstr = ctx.exprGen.findMatchingConstructor(thisClass, exprTypes);
    if (thisConstr == null) {
      thisConstr = findConstructorViaReflection(thisClass, exprTypes.size());
    }
    if (thisConstr == null) {
      List<Argument> constrArgs = new ArrayList<>();
      for (int i = 0; i < exprTypes.size(); i++) {
        constrArgs.add(
            new Argument(
                exprTypes.get(i), io.github.martinschneider.orzo.lexer.tokens.Token.id("p" + i)));
      }
      thisConstr =
          new Method(
              thisClass.replace('/', '.'),
              List.of(AccessFlag.ACC_PUBLIC),
              "void",
              io.github.martinschneider.orzo.lexer.tokens.Token.id("<init>"),
              constrArgs,
              null);
    }
    pushArgs(out, methodCall.params, exprTypes, thisConstr);
    ctx.invokeGen.invokeSpecial(out, thisConstr);
    return out;
  }

  Method findConstructorViaReflection(String jvmClassName, int argCount) {
    try {
      Class<?> clazz = Class.forName(jvmClassName.replace('/', '.'));
      for (java.lang.reflect.Constructor<?> c : clazz.getConstructors()) {
        if (Modifier.isPublic(c.getModifiers()) && c.getParameterCount() == argCount) {
          List<Argument> args = new ArrayList<>();
          for (java.lang.reflect.Parameter p : c.getParameters()) {
            args.add(
                new Argument(
                    p.getType().getName(),
                    io.github.martinschneider.orzo.lexer.tokens.Token.id(p.getName())));
          }
          markVarargs(args, c.isVarArgs());
          return new Method(
              jvmClassName.replace('/', '.'),
              List.of(AccessFlag.ACC_PUBLIC),
              "void",
              io.github.martinschneider.orzo.lexer.tokens.Token.id("<init>"),
              args,
              null);
        }
      }
    } catch (ClassNotFoundException e) {
      // not on classpath, fall through
    }
    return null;
  }

  Method findConstructorViaReflectionTyped(String jvmClassName, List<String> argTypes) {
    try {
      Class<?> clazz = Class.forName(jvmClassName.replace('/', '.'));
      for (int pass = 0; pass < 2; pass++) {
        for (java.lang.reflect.Constructor<?> c : clazz.getConstructors()) {
          if (!Modifier.isPublic(c.getModifiers())) {
            continue;
          }
          // second pass: a varargs constructor called with its trailing arguments unpacked
          boolean compatible =
              (pass == 0)
                  ? c.getParameterCount() == argTypes.size()
                  : isVarargsCompatible(c.isVarArgs(), c.getParameterTypes(), argTypes);
          for (int i = 0; compatible && pass == 0 && i < argTypes.size(); i++) {
            compatible = isArgTypeCompatible(argTypes.get(i), c.getParameters()[i].getType());
          }
          if (!compatible) continue;
          List<Argument> args = new ArrayList<>();
          for (java.lang.reflect.Parameter p : c.getParameters()) {
            args.add(
                new Argument(
                    p.getType().getName(),
                    io.github.martinschneider.orzo.lexer.tokens.Token.id(p.getName())));
          }
          markVarargs(args, c.isVarArgs());
          return new Method(
              jvmClassName.replace('/', '.'),
              List.of(AccessFlag.ACC_PUBLIC),
              "void",
              io.github.martinschneider.orzo.lexer.tokens.Token.id("<init>"),
              args,
              null);
        }
      }
    } catch (ClassNotFoundException e) {
      // not on classpath, fall through
    }
    return null;
  }

  private static void markVarargs(List<Argument> args, boolean isVarArgs) {
    if (isVarArgs && !args.isEmpty()) {
      args.get(args.size() - 1).varargs = true;
    }
  }

  // Whether the given argument types match a varargs signature with the trailing arguments
  // unpacked, e.g. (String, int, int) for format(String, Object...)
  private boolean isVarargsCompatible(
      boolean isVarArgs, Class<?>[] paramTypes, List<String> argTypes) {
    int fixed = paramTypes.length - 1;
    if (!isVarArgs || argTypes.size() < fixed) {
      return false;
    }
    for (int i = 0; i < fixed; i++) {
      if (!isArgTypeCompatible(argTypes.get(i), paramTypes[i])) {
        return false;
      }
    }
    Class<?> elemType = paramTypes[fixed].getComponentType();
    for (int i = fixed; i < argTypes.size(); i++) {
      if (!isArgTypeCompatible(argTypes.get(i), elemType)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Evaluates the arguments of a call to the given method, converting (e.g. boxing) each one to the
   * parameter type. For a varargs method called with its trailing arguments unpacked, those are
   * packed into a new array.
   */
  public void pushArgs(
      DynamicByteArray out, List<Expression> params, List<String> types, Method method) {
    int paramCount = method.args.size();
    boolean pack =
        paramCount > 0
            && method.args.get(paramCount - 1).varargs
            && !(types.size() == paramCount && types.get(paramCount - 1).startsWith("["));
    int direct = pack ? paramCount - 1 : types.size();
    for (int i = 0; i < direct; i++) {
      pushArg(out, params.get(i), types.get(i), method.args.get(i).type);
    }
    if (pack) {
      String elemType =
          TypeUtils.jvmDescriptorToTypeName(method.args.get(paramCount - 1).type.substring(1));
      ctx.pushGen.push(out, INT, types.size() - direct);
      ctx.basicGen.newArray(out, elemType);
      for (int i = direct; i < types.size(); i++) {
        out.write(io.github.martinschneider.orzo.codegen.OpCodes.DUP);
        ctx.opStack.push(REF);
        ctx.pushGen.push(out, INT, i - direct);
        pushArg(out, params.get(i), types.get(i), elemType);
        out.write(TypeUtils.getStoreOpCode(elemType));
        ctx.opStack.pop(3);
      }
    }
  }

  private void pushArg(DynamicByteArray out, Expression param, String srcType, String tgtType) {
    // When autoboxing (primitive → reference): use source type for eval so that
    // PushGenerator emits correct bytecode for numeric literals
    String evalType =
        TypeUtils.isPrimitive(srcType) && !TypeUtils.isPrimitive(tgtType) ? srcType : tgtType;
    ExpressionResult exprResult = ctx.exprGen.eval(out, evalType, param);
    ctx.basicGen.convert1(out, exprResult.type, tgtType);
  }

  /**
   * generate code to call the appropriate println method for the specified type this will print the
   * top element on the stack
   */
  private DynamicByteArray print(DynamicByteArray out, String type) {
    if (type.equals(STRING)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of("Ljava/lang/String;")));
    } else if (type.equals(INT) || type.equals(BYTE) || type.equals(SHORT)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(INT)));
    } else if (type.equals(LONG)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(LONG)));
    } else if (type.equals(DOUBLE)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(DOUBLE)));
    } else if (type.equals(FLOAT)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(FLOAT)));
    } else if (type.equals(CHAR)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(CHAR)));
    } else if (type.equals(BOOLEAN)) {
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of(BOOLEAN)));
    } else {
      // Handle object types (including enums) by calling println(Object)
      ctx.invokeGen.invokeVirtual(
          out, new Method("java/io/PrintStream", "println", VOID, List.of("Ljava/lang/Object;")));
    }
    return out;
  }

  public boolean isInterfaceClass(String className) {
    if (className == null) {
      return false;
    }
    try {
      return Class.forName(className.replace('/', '.')).isInterface();
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public Method findMethodViaReflection(String receiverType, String methodName, int argCount) {
    if (receiverType == null) {
      return null;
    }
    try {
      String javaClassName = receiverType.replace('/', '.');
      if (javaClassName.startsWith("L") && javaClassName.endsWith(";")) {
        javaClassName = javaClassName.substring(1, javaClassName.length() - 1);
      }
      javaClassName = resolveClassName(javaClassName);
      Class<?> clazz = Class.forName(javaClassName);
      java.lang.reflect.Method bridgeCandidate = null;
      for (java.lang.reflect.Method m : getMethodsIncludingNonPublic(clazz)) {
        if (m.getName().equals(methodName) && m.getParameterCount() == argCount) {
          if (m.isBridge()) {
            if (bridgeCandidate == null) bridgeCandidate = m;
            continue;
          }
          List<Argument> args = new ArrayList<>();
          for (java.lang.reflect.Parameter p : m.getParameters()) {
            args.add(
                new Argument(
                    p.getType().getName(),
                    io.github.martinschneider.orzo.lexer.tokens.Token.id(p.getName())));
          }
          markVarargs(args, m.isVarArgs());
          String returnType = m.getReturnType().getName();
          // Normalize common return types
          if ("java.lang.String".equals(returnType)) {
            returnType = STRING;
          }
          List<AccessFlag> accFlags = new ArrayList<>();
          if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
            accFlags.add(AccessFlag.ACC_STATIC);
          }
          return new Method(javaClassName, accFlags, returnType, Token.id(methodName), args, null);
        }
      }
      // Fall back to bridge methods (e.g. StringBuilder.length() is a bridge on Java 21)
      if (bridgeCandidate != null) {
        List<Argument> args = new ArrayList<>();
        for (java.lang.reflect.Parameter p : bridgeCandidate.getParameters()) {
          args.add(
              new Argument(
                  p.getType().getName(),
                  io.github.martinschneider.orzo.lexer.tokens.Token.id(p.getName())));
        }
        markVarargs(args, bridgeCandidate.isVarArgs());
        String returnType = bridgeCandidate.getReturnType().getName();
        if ("java.lang.String".equals(returnType)) {
          returnType = STRING;
        }
        List<AccessFlag> accFlags = new ArrayList<>();
        if (java.lang.reflect.Modifier.isStatic(bridgeCandidate.getModifiers())) {
          accFlags.add(AccessFlag.ACC_STATIC);
        }
        return new Method(javaClassName, accFlags, returnType, Token.id(methodName), args, null);
      }
    } catch (ClassNotFoundException e) {
      // not on classpath, fall through
    }
    return null;
  }

  public String generateChained(
      DynamicByteArray out,
      GlobalIdentifierMap classIdMap,
      MethodCall methodCall,
      String receiverType) {
    String receiverGeneric =
        (lastGenericType != null && GenericTypes.erasure(lastGenericType).equals(receiverType))
            ? lastGenericType
            : null;
    lastGenericType = null;
    List<String> types = new ArrayList<>();
    for (Expression exp : methodCall.params) {
      types.add(new NumExprTypeDecider(ctx).getType(classIdMap, exp));
    }
    String methodName = methodCall.name.toString();
    Method method = null;
    if (receiverType != null) {
      // Prioritize receiver-type-specific lookups to avoid matching same-named methods on 'this'
      int lastDot = receiverType.lastIndexOf('.');
      String simpleType = (lastDot >= 0) ? receiverType.substring(lastDot + 1) : receiverType;
      method = findMatchingMethod(simpleType + "." + methodName, types);
      if (method == null) {
        method = findMatchingMethod(receiverType + "." + methodName, types);
      }
      if (method == null) {
        method = findMethodViaReflectionTyped(receiverType, methodName, types);
      }
      // FQN resolution fallback for simple class names (e.g. "ClassParser" → package-qualified)
      if (method == null) {
        String resolvedType = resolveClassName(receiverType);
        method = findMethodViaReflectionTyped(resolvedType, methodName, types);
      }
    }
    // No bare-name ("implicit this") fallback here: generateChained is only ever called for a
    // genuine chain continuation (the receiver is always whatever the previous call in the chain
    // returned, left on the stack), never an implicit-this call — so if the receiver-type-based
    // lookups above all failed (e.g. a generic method's return erased to Object, so receiverType
    // is a useless "java.lang.Object"), falling back to a same-named method on the *enclosing*
    // class is never correct. It previously did exactly that silently, invoking the wrong method
    // with mismatched semantics instead of raising a compile error (root-caused via
    // "reg.get(x.getClass()).generate(x)" picking an unrelated this.generate(...) and
    // VerifyError-ing at runtime instead of failing to compile).
    // Handle dotted method name as field chain + method (e.g. "val.toString" on Token)
    if (method == null && methodName.contains(".")) {
      int lastDotInName = methodName.lastIndexOf('.');
      String fieldPart = methodName.substring(0, lastDotInName);
      String methodPart = methodName.substring(lastDotInName + 1);
      String chainType = receiverType;
      boolean chainOk = chainType != null;
      for (String fp : fieldPart.split("\\.")) {
        if (!chainOk) break;
        java.util.Map<String, io.github.martinschneider.orzo.codegen.FieldProcessor.InstanceField>
            fm =
                new io.github.martinschneider.orzo.codegen.FieldProcessor()
                    .getInstanceFieldMap(chainType, ctx.allClazzes);
        io.github.martinschneider.orzo.codegen.FieldProcessor.InstanceField fi = fm.get(fp);
        if (fi == null) {
          chainOk = false;
        } else {
          String jvmClass = fi.className.replace('.', '/');
          String descr = TypeUtils.descr(fi.fieldType);
          ctx.constPool.addClass(jvmClass);
          ctx.constPool.addFieldRef(jvmClass, fi.fieldName, descr);
          ctx.opStack.pop();
          ctx.loadGen.getField(
              out,
              ctx.constPool.indexOf(
                  io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF,
                  jvmClass,
                  fi.fieldName,
                  descr));
          ctx.opStack.push(fi.fieldType);
          chainType = fi.fieldType;
        }
      }
      if (chainOk) {
        method = findMethodViaReflectionTyped(chainType, methodPart, types);
        if (method == null) {
          method = findMethodViaReflection(chainType, methodPart, types.size());
        }
        if (method == null && types.isEmpty() && "toString".equals(methodPart)) {
          method = new Method("java/lang/Object", "toString", STRING, new ArrayList<>());
        }
      }
    }
    if (method == null) {
      ctx.errors.addError(
          LOGGER_NAME,
          (ctx.clazz != null ? ctx.clazz.sourceFile + " " : "")
              + "missing chained method \""
              + methodName
              + types
              + "\" on "
              + receiverType,
          new RuntimeException().getStackTrace());
      return "";
    }
    // Receiver is already on the operand stack from the previous method call
    pushArgs(out, methodCall.params, types, method);
    boolean isStatic = method.accFlags != null && method.accFlags.contains(AccessFlag.ACC_STATIC);
    if (isStatic) {
      ctx.invokeGen.invokeStatic(out, method);
    } else if (isInterfaceClass(method.fqClassName)) {
      ctx.invokeGen.invokeInterface(out, method);
    } else {
      ctx.invokeGen.invokeVirtual(out, method);
    }
    return applyGenericReturnType(out, method, receiverGeneric);
  }

  private Method findMethodViaReflectionTyped(
      String receiverType, String methodName, List<String> argTypes) {
    if (receiverType == null) {
      return null;
    }
    try {
      String javaClassName = receiverType.replace('/', '.');
      if (javaClassName.startsWith("L") && javaClassName.endsWith(";")) {
        javaClassName = javaClassName.substring(1, javaClassName.length() - 1);
      }
      javaClassName = resolveClassName(javaClassName);
      Class<?> clazz = Class.forName(javaClassName);
      java.lang.reflect.Method bridgeCandidate = null;
      // Two passes: Class.getMethods() order is unspecified, so with only a widening-compatible
      // check (e.g. char is also compatible with an int parameter, for "StringBuffer.append(C)"
      // vs "append(I)") the FIRST compatible overload found could be the wrong one whenever an
      // exact match also exists. Prefer an exact-type match on every parameter first; only fall
      // back to widening-compatible matching if no exact match exists for any overload.
      // Third pass: a varargs method called with its trailing arguments unpacked
      for (int pass = 0; pass < 3; pass++) {
        boolean exactOnly = pass == 0;
        for (java.lang.reflect.Method m : getMethodsIncludingNonPublic(clazz)) {
          if (!m.getName().equals(methodName)) {
            continue;
          }
          boolean compatible =
              (pass < 2)
                  ? m.getParameterCount() == argTypes.size()
                  : isVarargsCompatible(m.isVarArgs(), m.getParameterTypes(), argTypes);
          for (int i = 0; compatible && pass < 2 && i < argTypes.size(); i++) {
            compatible =
                exactOnly
                    ? isExactTypeMatch(argTypes.get(i), m.getParameters()[i].getType())
                    : isArgTypeCompatible(argTypes.get(i), m.getParameters()[i].getType());
          }
          if (!compatible) {
            continue;
          }
          if (m.isBridge()) {
            if (bridgeCandidate == null) bridgeCandidate = m;
            continue;
          }
          List<Argument> args = new ArrayList<>();
          for (java.lang.reflect.Parameter p : m.getParameters()) {
            args.add(
                new Argument(
                    p.getType().getName(),
                    io.github.martinschneider.orzo.lexer.tokens.Token.id(p.getName())));
          }
          markVarargs(args, m.isVarArgs());
          String returnType = m.getReturnType().getName();
          if ("java.lang.String".equals(returnType)) {
            returnType = STRING;
          }
          List<AccessFlag> accFlags = new ArrayList<>();
          if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
            accFlags.add(AccessFlag.ACC_STATIC);
          }
          return new Method(
              javaClassName,
              accFlags,
              returnType,
              io.github.martinschneider.orzo.lexer.tokens.Token.id(methodName),
              args,
              null);
        }
      }
      // Fall back to bridge methods (e.g. StringBuilder.length() is a bridge on Java 21)
      if (bridgeCandidate != null) {
        List<Argument> args = new ArrayList<>();
        for (java.lang.reflect.Parameter p : bridgeCandidate.getParameters()) {
          args.add(
              new Argument(
                  p.getType().getName(),
                  io.github.martinschneider.orzo.lexer.tokens.Token.id(p.getName())));
        }
        markVarargs(args, bridgeCandidate.isVarArgs());
        String returnType = bridgeCandidate.getReturnType().getName();
        if ("java.lang.String".equals(returnType)) {
          returnType = STRING;
        }
        List<AccessFlag> accFlags = new ArrayList<>();
        if (java.lang.reflect.Modifier.isStatic(bridgeCandidate.getModifiers())) {
          accFlags.add(AccessFlag.ACC_STATIC);
        }
        return new Method(
            javaClassName,
            accFlags,
            returnType,
            io.github.martinschneider.orzo.lexer.tokens.Token.id(methodName),
            args,
            null);
      }
    } catch (ClassNotFoundException e) {
      // not on classpath
    }
    return null;
  }

  private java.lang.reflect.Method[] getMethodsIncludingNonPublic(Class<?> clazz) {
    java.util.List<java.lang.reflect.Method> result = new java.util.ArrayList<>();
    for (java.lang.reflect.Method m : clazz.getMethods()) {
      result.add(m);
    }
    Class<?> c = clazz;
    while (c != null) {
      for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
        int mod = m.getModifiers();
        if (!java.lang.reflect.Modifier.isPublic(mod)
            && !java.lang.reflect.Modifier.isPrivate(mod)) {
          result.add(m);
        }
      }
      c = c.getSuperclass();
    }
    return result.toArray(new java.lang.reflect.Method[0]);
  }

  private boolean isArgTypeCompatible(String argType, Class<?> paramClass) {
    if (argType == null) {
      return true;
    }
    // A lambda has no type of its own (see LambdaExpr.PSEUDO_TYPE) - it's compatible with any
    // functional-interface-shaped parameter, determined from the TARGET side instead.
    if (io.github.martinschneider.orzo.lexer.tokens.LambdaExpr.PSEUDO_TYPE.equals(argType)) {
      return paramClass.isInterface();
    }
    // Orzo represents array types as "[" + <element type word>, e.g. "[byte" for byte[]
    // (see NumExprTypeDecider), not a JVM array descriptor. Class.forName can't resolve that
    // directly, so match against the parameter's component type instead of falling through
    // to the "assume compatible" default below (which made every write(...) overload on
    // DynamicByteArray look equally compatible with a byte[] argument).
    if (argType.startsWith("[")) {
      if (!paramClass.isArray()) {
        return paramClass == Object.class;
      }
      String elemType = argType.substring(1);
      Class<?> elemParam = paramClass.getComponentType();
      // Primitive array element types must match exactly (unlike scalar args, arrays aren't
      // covariant/widening in Java: byte[] is not assignable to int[]).
      switch (elemType) {
        case "boolean":
          return elemParam == boolean.class;
        case "byte":
          return elemParam == byte.class;
        case "char":
          return elemParam == char.class;
        case "short":
          return elemParam == short.class;
        case "int":
          return elemParam == int.class;
        case "long":
          return elemParam == long.class;
        case "float":
          return elemParam == float.class;
        case "double":
          return elemParam == double.class;
        default:
          return isArgTypeCompatible(elemType, elemParam);
      }
    }
    String cleanType =
        argType.startsWith("L") && argType.endsWith(";")
            ? argType.substring(1, argType.length() - 1).replace('/', '.')
            : argType.replace('/', '.');
    // Map Orzo primitive/built-in type names to Java classes
    switch (cleanType) {
      case "boolean":
        return paramClass == boolean.class
            || paramClass == Boolean.class
            || paramClass == Object.class;
      case "int":
      case "byte":
      case "short":
        return paramClass == int.class
            || paramClass == long.class
            || paramClass == Integer.class
            || paramClass == Object.class;
      case "char":
        return paramClass == char.class
            || paramClass == int.class
            || paramClass == long.class
            || paramClass == Character.class
            || paramClass == Integer.class
            || paramClass == Object.class;
      case "long":
        return paramClass == long.class || paramClass == Long.class || paramClass == Object.class;
      case "float":
        return paramClass == float.class
            || paramClass == double.class
            || paramClass == Float.class
            || paramClass == Object.class;
      case "double":
        return paramClass == double.class
            || paramClass == Double.class
            || paramClass == Object.class;
      case "String":
        cleanType = "java.lang.String";
        break;
      // Boxed types: compatible with their unboxed primitive and widened primitives
      case "java.lang.Integer":
        return paramClass == int.class
            || paramClass == long.class
            || paramClass == Integer.class
            || paramClass == Object.class;
      case "java.lang.Long":
        return paramClass == long.class || paramClass == Long.class || paramClass == Object.class;
      case "java.lang.Double":
        return paramClass == double.class
            || paramClass == Double.class
            || paramClass == Object.class;
      case "java.lang.Float":
        return paramClass == float.class
            || paramClass == double.class
            || paramClass == Float.class
            || paramClass == Object.class;
      case "java.lang.Byte":
        return paramClass == byte.class
            || paramClass == short.class
            || paramClass == int.class
            || paramClass == long.class
            || paramClass == Byte.class
            || paramClass == Object.class;
      case "java.lang.Short":
        return paramClass == short.class
            || paramClass == int.class
            || paramClass == long.class
            || paramClass == Short.class
            || paramClass == Object.class;
      case "java.lang.Boolean":
        return paramClass == boolean.class
            || paramClass == Boolean.class
            || paramClass == Object.class;
      case "java.lang.Character":
        return paramClass == char.class
            || paramClass == int.class
            || paramClass == long.class
            || paramClass == Character.class
            || paramClass == Object.class;
      default:
        break;
    }
    // Try java.lang.* if simple name
    if (!cleanType.contains(".")) {
      try {
        Class<?> argClass = Class.forName("java.lang." + cleanType);
        return paramClass.isAssignableFrom(argClass);
      } catch (ClassNotFoundException e) {
        // fall through
      }
    }
    try {
      Class<?> argClass = Class.forName(cleanType);
      return paramClass.isAssignableFrom(argClass);
    } catch (ClassNotFoundException e) {
      return true; // unknown type: assume compatible
    }
  }

  // Exact-type check used to prefer a precise overload (e.g. "char" -> append(char)) over a
  // merely widening-compatible one (e.g. append(int)) when Class.getMethods() happens to return
  // the latter first. Primitives must match their exact wrapper-free class; everything else
  // falls back to isArgTypeCompatible's broader (reference-assignability) notion of a match.
  private boolean isExactTypeMatch(String argType, Class<?> paramClass) {
    if (argType == null) {
      return false;
    }
    // A lambda has no type of its own (see LambdaExpr.PSEUDO_TYPE) - it's compatible with any
    // functional-interface-shaped parameter, determined from the TARGET side instead.
    if (io.github.martinschneider.orzo.lexer.tokens.LambdaExpr.PSEUDO_TYPE.equals(argType)) {
      return paramClass.isInterface();
    }
    switch (argType) {
      case "boolean":
        return paramClass == boolean.class;
      case "byte":
        return paramClass == byte.class;
      case "char":
        return paramClass == char.class;
      case "short":
        return paramClass == short.class;
      case "int":
        return paramClass == int.class;
      case "long":
        return paramClass == long.class;
      case "float":
        return paramClass == float.class;
      case "double":
        return paramClass == double.class;
      default:
        return isArgTypeCompatible(argType, paramClass);
    }
  }

  private String resolveClassName(String simpleName) {
    if (simpleName == null || simpleName.contains(".")) {
      return simpleName;
    }
    if (ctx.clazz != null && ctx.clazz.imports != null) {
      for (Import imp : ctx.clazz.imports) {
        if (!imp.isStatic && imp.id.endsWith("." + simpleName)) {
          return imp.id;
        }
      }
    }
    // Check java.lang auto-import before falling back to the current package
    try {
      Class.forName("java.lang." + simpleName);
      return "java.lang." + simpleName;
    } catch (ClassNotFoundException e) {
      // not in java.lang, try package prefix
    }
    if (ctx.clazz != null && ctx.clazz.packageName != null && !ctx.clazz.packageName.isEmpty()) {
      return ctx.clazz.packageName + "." + simpleName;
    }
    return simpleName;
  }

  public Method findMatchingMethod(String methodName, List<String> types) {
    List<List<String>> typesList = new ArrayList<>();
    for (int i = 0; i < types.size(); i++) {
      typesList.add(TypeUtils.assignableTo(types.get(i)));
    }
    Method method = null;
    for (List<String> assignTypes : TypeUtils.combinations(typesList)) {
      String methodKey = methodName + TypeUtils.typesDescr(assignTypes);
      method = ctx.methodMap.get(methodKey);
      if (method != null) {
        break;
      }
    }
    if (method == null) {
      method = findVarargsMethod(methodName, types);
    }
    return method;
  }

  /**
   * Finds a varargs method in the method map (e.g. a method of a class compiled in the same run)
   * that matches a call with its trailing arguments unpacked, e.g. sum(1, 2, 3) for sum(int...).
   */
  public Method findVarargsMethod(String methodName, List<String> types) {
    for (java.util.Map.Entry<String, Method> entry : ctx.methodMap.entrySet()) {
      Method candidate = entry.getValue();
      int paramCount = candidate.args.size();
      if (!entry.getKey().startsWith(methodName + "(")
          || paramCount == 0
          || !candidate.args.get(paramCount - 1).varargs
          || types.size() < paramCount - 1) {
        continue;
      }
      String elemType = candidate.args.get(paramCount - 1).type.substring(1);
      boolean compatible = true;
      for (int i = 0; compatible && i < types.size(); i++) {
        String paramType = (i < paramCount - 1) ? candidate.args.get(i).type : elemType;
        compatible = isAssignable(types.get(i), paramType);
      }
      if (compatible) {
        return candidate;
      }
    }
    return null;
  }

  private boolean isAssignable(String argType, String paramType) {
    String paramDescr = TypeUtils.descr(TypeUtils.jvmDescriptorToTypeName(paramType));
    for (String assignable : TypeUtils.assignableTo(argType)) {
      if (TypeUtils.descr(assignable).equals(paramDescr)) {
        return true;
      }
    }
    return false;
  }
}
