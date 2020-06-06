package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
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
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import io.github.martinschneider.orzo.parser.productions.Statement;

public class MethodCallGenerator implements StatementGenerator {
  private static final String LOGGER_NAME = "method call code generator";
  private CGContext ctx;

  public MethodCallGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    MethodCall methodCall = (MethodCall) stmt;
    if ("System.out.println".equals(methodCall.name.toString())) {
      for (Expression param : methodCall.params) {
        ctx.opsGenerator.getStatic(out, "java/lang/System", "out", "Ljava/io/PrintStream;");
        ExpressionResult result = ctx.exprGenerator.eval(out, variables, null, param);
        print(out, result.type);
      }
    } else {
      String methodName = methodCall.name.toString();
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
      for (Expression expr : methodCall.params) {
        ctx.exprGenerator.eval(out, variables, null, expr);
      }
      ctx.opsGenerator.invokeStatic(
          out, ctx.clazz.name.val.toString(), methodName, methodToCall.getTypeDescr());
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
