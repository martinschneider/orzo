package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.ExpressionResult;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.ArrayList;
import java.util.List;

public class MethodCallGenerator implements StatementGenerator {
  private static final String LOGGER_NAME = "method call code generator";
  public CGContext ctx;

  public MethodCallGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public String generate(DynamicByteArray out, VariableMap variables, MethodCall methodCall) {
    List<String> types = new ArrayList<>();
    for (Expression exp : methodCall.params) {
      types.add(new NumExprTypeDecider(ctx).getType(variables, exp));
    }
    String methodName = methodCall.name.toString();
    Method method = findMatchingMethod(methodName, types);
    if (method == null) {
      ctx.errors.addError(
          LOGGER_NAME,
          "missing method declaration \""
              + methodName
              + "\", known methods: "
              + ctx.methodMap.keySet());
      return "";
    }
    String clazzName = ctx.clazz.fqn('/');
    if (methodName.contains(".")) {
      String[] tmp = methodName.split("\\.");
      clazzName = method.fqClassName.replaceAll("\\.", "/");
      methodName = tmp[1];
      ctx.constPool.addClass(clazzName);
      ctx.constPool.addMethodRef(clazzName, methodName, TypeUtils.methodDescr(method));
    } else if (!ctx.clazz.fqn().equals(method.fqClassName)) { // static import
      clazzName = method.fqClassName.replaceAll("\\.", "/");
      ctx.constPool.addClass(clazzName);
      ctx.constPool.addMethodRef(clazzName, methodName, TypeUtils.methodDescr(method));
    }
    for (int i = 0; i < types.size(); i++) {
      ctx.exprGenerator.eval(out, variables, method.args.get(i).type, methodCall.params.get(i));
      ctx.opsGenerator.convert(out, types.get(i), method.args.get(i).type);
    }
    ctx.opsGenerator.invokeStatic(out, clazzName, methodName, TypeUtils.methodDescr(method));
    return method.type;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    MethodCall methodCall = (MethodCall) stmt;
    if ("System.out.println".equals(methodCall.name.toString())) {
      for (Expression param : methodCall.params) {
        ctx.opsGenerator.getStatic(out, "java/lang/System", "out", "Ljava/io/PrintStream;");
        ExpressionResult result = ctx.exprGenerator.eval(out, variables, null, param);
        if (result != null) {
          print(out, result.type);
        } else {
          ctx.errors.addError(LOGGER_NAME, "error evaluating expression " + param);
          return null;
        }
      }
    } else {
      generate(out, variables, methodCall);
    }
    return out;
  }

  /**
   * generate code to call the appropriate println method for the specified type this will print the
   * top element on the stack
   */
  private DynamicByteArray print(DynamicByteArray out, String type) {
    if (type.equals(STRING)) {
      ctx.opsGenerator.invokeVirtual(
          out, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    } else if (type.equals(INT) || type.equals(BYTE) || type.equals(SHORT)) {
      ctx.opsGenerator.invokeVirtual(out, "java/io/PrintStream", "println", "(I)V");
    } else if (type.equals(LONG)) {
      ctx.opsGenerator.invokeVirtual(out, "java/io/PrintStream", "println", "(J)V");
    } else if (type.equals(DOUBLE)) {
      ctx.opsGenerator.invokeVirtual(out, "java/io/PrintStream", "println", "(D)V");
    } else if (type.equals(FLOAT)) {
      ctx.opsGenerator.invokeVirtual(out, "java/io/PrintStream", "println", "(F)V");
    } else if (type.equals(CHAR)) {
      ctx.opsGenerator.invokeVirtual(out, "java/io/PrintStream", "println", "(C)V");
    } else if (type.equals(BOOLEAN)) {
      ctx.opsGenerator.invokeVirtual(out, "java/io/PrintStream", "println", "(Z)V");
    } else {
      // TODO: call toString() first
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
