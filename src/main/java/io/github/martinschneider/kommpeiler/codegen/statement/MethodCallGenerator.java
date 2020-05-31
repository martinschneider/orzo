package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BYTE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.DOUBLE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.FLOAT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.SHORT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.STRING;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionResult;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;

public class MethodCallGenerator implements StatementGenerator {
  private static final String LOGGER_NAME = "method call code generator";
  private CGContext ctx;

  public MethodCallGenerator(CGContext context) {
    this.ctx = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    MethodCall methodCall = (MethodCall) stmt;
    if ("System.out.println".equals(methodCall.getName().toString())) {
      for (Expression param : methodCall.getParameters()) {
        ctx.opsGenerator.getStatic(out, "java/lang/System", "out", "Ljava/io/PrintStream;");
        ExpressionResult result = ctx.exprGenerator.eval(out, variables, null, param);
        print(out, result.getType());
      }
    } else {
      String methodName = methodCall.getName().toString();
      Method methodToCall = ctx.methodMap.get(methodName);
      if (methodToCall == null) {
        ctx.errors.addError(
            LOGGER_NAME,
            "missing method declaration \""
                + methodName
                + "\", known methods: "
                + ctx.methodMap.keySet());
        return null;
      }
      for (Expression expr : methodCall.getParameters()) {
        ctx.exprGenerator.eval(out, variables, null, expr);
      }
      ctx.opsGenerator.invokeStatic(
          out, ctx.clazz.getName().getValue().toString(), methodName, methodToCall.getTypeDescr());
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
    } else {
      // TODO: call toString() first
    }
    return out;
  }
}
