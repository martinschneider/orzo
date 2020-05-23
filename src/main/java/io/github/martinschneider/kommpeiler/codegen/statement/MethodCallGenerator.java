package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BYTE;
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
  private CGContext context;

  public MethodCallGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    MethodCall methodCall = (MethodCall) stmt;
    if ("System.out.println".equals(methodCall.getQualifiedName())) {
      for (Expression param : methodCall.getParameters()) {
        context.opsGenerator.getStatic(out, "java/lang/System", "out", "Ljava/io/PrintStream;");
        ExpressionResult result = context.exprGenerator.eval(out, variables, null, param);
        print(out, result.getType());
      }
    } else {
      String methodName =
          methodCall.getNames().get(methodCall.getNames().size() - 1).getValue().toString();
      for (Expression expr : methodCall.getParameters()) {
        context.exprGenerator.eval(out, variables, null, expr);
      }
      context.opsGenerator.invokeStatic(
          out,
          context.clazz.getName().getValue().toString(),
          methodName,
          context.methodMap.get(methodName).getTypeDescr());
    }
    return out;
  }

  /**
   * generate code to call the appropriate println method for the specified type this will print the
   * top element on the stack
   */
  private DynamicByteArray print(DynamicByteArray out, String type) {
    if (type.equals(STRING)) {
      context.opsGenerator.invokeVirtual(
          out, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    } else if (type.equals(INT) || type.equals(BYTE) || type.equals(SHORT)) {
      context.opsGenerator.invokeVirtual(out, "java/io/PrintStream", "println", "(I)V");
    } else if (type.equals(LONG)) {
      context.opsGenerator.invokeVirtual(out, "java/io/PrintStream", "println", "(J)V");
    } else {
      // TODO: call toString() first
    }
    return out;
  }
}
