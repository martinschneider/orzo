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
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;
import static io.github.martinschneider.orzo.util.FactoryHelper.defaultConstr;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.identifier.GlobalIdentifierMap;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Argument;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Import;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class MethodCallGenerator implements StatementGenerator<MethodCall> {
  private static final String LOGGER_NAME = "method call code generator";
  public CGContext ctx;

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
        method = findMethodViaReflection(resolvedClassName, simpleMethod, types.size());
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
        } else {
          ctx.loadGen.loadReference(out, (short) 0);
        }
      } else {
        ctx.loadGen.loadReference(out, (short) 0); // push 'this' for invokevirtual
      }
    }
    for (int i = 0; i < types.size(); i++) {
      String srcType = types.get(i);
      String tgtType = method.args.get(i).type;
      // When autoboxing (primitive → reference): use source type for eval so that
      // PushGenerator emits correct bytecode for numeric literals
      String evalType =
          TypeUtils.isPrimitive(srcType) && !TypeUtils.isPrimitive(tgtType) ? srcType : tgtType;
      ExpressionResult exprResult = ctx.exprGen.eval(out, evalType, methodCall.params.get(i));
      ctx.basicGen.convert1(out, exprResult.type, tgtType);
    }
    if (isStatic) {
      ctx.invokeGen.invokeStatic(out, method);
    } else if ("super".equals(receiverName)) {
      ctx.invokeGen.invokeSpecial(out, method);
    } else if (isInterfaceClass(method.fqClassName)) {
      ctx.invokeGen.invokeInterface(out, method);
    } else {
      ctx.invokeGen.invokeVirtual(out, method);
    }
    return method.type;
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
    for (int i = 0; i < methodCall.params.size(); i++) {
      String srcType = exprTypes.get(i);
      String tgtType = superConstr.args.get(i).type;
      String evalType =
          TypeUtils.isPrimitive(srcType) && !TypeUtils.isPrimitive(tgtType) ? srcType : tgtType;
      ExpressionResult exprResult = ctx.exprGen.eval(out, evalType, methodCall.params.get(i));
      ctx.basicGen.convert1(out, exprResult.type, tgtType);
    }
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
    for (int i = 0; i < methodCall.params.size(); i++) {
      String srcType = exprTypes.get(i);
      String tgtType = thisConstr.args.get(i).type;
      String evalType =
          TypeUtils.isPrimitive(srcType) && !TypeUtils.isPrimitive(tgtType) ? srcType : tgtType;
      ExpressionResult exprResult = ctx.exprGen.eval(out, evalType, methodCall.params.get(i));
      ctx.basicGen.convert1(out, exprResult.type, tgtType);
    }
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

  public boolean isPrimitiveType(String type) {
    return TypeUtils.isPrimitive(type);
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
      Class<?> clazz = Class.forName(javaClassName);
      for (java.lang.reflect.Method m : clazz.getMethods()) {
        if (m.isBridge()) continue;
        if (m.getName().equals(methodName) && m.getParameterCount() == argCount) {
          List<Argument> args = new ArrayList<>();
          for (java.lang.reflect.Parameter p : m.getParameters()) {
            args.add(
                new Argument(
                    p.getType().getName(),
                    io.github.martinschneider.orzo.lexer.tokens.Token.id(p.getName())));
          }
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
    // Bare-name fallback (for method calls where type info is unavailable)
    if (method == null) {
      method = findMatchingMethod(methodName, types);
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
    for (int i = 0; i < types.size(); i++) {
      String srcType = types.get(i);
      String tgtType = method.args.get(i).type;
      String evalType =
          TypeUtils.isPrimitive(srcType) && !TypeUtils.isPrimitive(tgtType) ? srcType : tgtType;
      ExpressionResult exprResult = ctx.exprGen.eval(out, evalType, methodCall.params.get(i));
      ctx.basicGen.convert1(out, exprResult.type, tgtType);
    }
    boolean isStatic = method.accFlags != null && method.accFlags.contains(AccessFlag.ACC_STATIC);
    if (isStatic) {
      ctx.invokeGen.invokeStatic(out, method);
    } else if (isInterfaceClass(method.fqClassName)) {
      ctx.invokeGen.invokeInterface(out, method);
    } else {
      ctx.invokeGen.invokeVirtual(out, method);
    }
    return method.type;
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
      Class<?> clazz = Class.forName(javaClassName);
      for (java.lang.reflect.Method m : clazz.getMethods()) {
        if (m.isBridge()) continue;
        if (!m.getName().equals(methodName) || m.getParameterCount() != argTypes.size()) {
          continue;
        }
        boolean compatible = true;
        for (int i = 0; i < argTypes.size(); i++) {
          if (!isArgTypeCompatible(argTypes.get(i), m.getParameters()[i].getType())) {
            compatible = false;
            break;
          }
        }
        if (!compatible) {
          continue;
        }
        List<Argument> args = new ArrayList<>();
        for (java.lang.reflect.Parameter p : m.getParameters()) {
          args.add(
              new Argument(
                  p.getType().getName(),
                  io.github.martinschneider.orzo.lexer.tokens.Token.id(p.getName())));
        }
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
    } catch (ClassNotFoundException e) {
      // not on classpath
    }
    return null;
  }

  private boolean isArgTypeCompatible(String argType, Class<?> paramClass) {
    if (argType == null) {
      return true;
    }
    String cleanType =
        argType.startsWith("L") && argType.endsWith(";")
            ? argType.substring(1, argType.length() - 1).replace('/', '.')
            : argType;
    // Map Orzo primitive/built-in type names to Java classes
    switch (cleanType) {
      case "int":
      case "byte":
      case "short":
      case "char":
      case "boolean":
        return paramClass == int.class
            || paramClass == long.class
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
    return method;
  }
}
