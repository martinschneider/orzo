package io.github.martinschneider.orzo.codegen.generators;

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
import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
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
              + methodCall.loc.toString()
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
      ExpressionResult exprResult =
          ctx.exprGen.eval(out, method.args.get(i).type, methodCall.params.get(i));
      ctx.basicGen.convert1(out, exprResult.type, method.args.get(i).type);
    }
    if (isStatic) {
      ctx.invokeGen.invokeStatic(out, method);
    } else {
      ctx.invokeGen.invokeVirtual(out, method);
    }
    return method.type;
  }

  @Override
  public HasOutput generate(DynamicByteArray out, Method method, MethodCall methodCall) {
    if ("super".equals(methodCall.name.toString())) {
      callSuperConstr(out);
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
      generate(out, ctx.classIdMap, methodCall);
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
