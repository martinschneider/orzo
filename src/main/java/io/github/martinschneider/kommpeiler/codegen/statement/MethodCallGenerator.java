package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.ExpressionResult;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.OpsCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.parser.productions.Type;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class MethodCallGenerator implements StatementGenerator {
  private ExpressionCodeGenerator expressionCodeGenerator;
  private OpsCodeGenerator opsCodeGenerator;
  private ConstantPool constantPool;
  private Map<String, Method> methodMap;

  public MethodCallGenerator(
      ExpressionCodeGenerator expressionCodeGenerator,
      OpsCodeGenerator opsCodeGenerator,
      ConstantPool constantPool,
      Map<String, Method> methodMap) {
    this.expressionCodeGenerator = expressionCodeGenerator;
    this.opsCodeGenerator = opsCodeGenerator;
    this.constantPool = constantPool;
    this.methodMap = methodMap;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement statement,
      Method method,
      Clazz clazz) {
    MethodCall methodCall = (MethodCall) statement;
    if ("System.out.println".equals(methodCall.getQualifiedName())) {
      for (Expression param : methodCall.getParameters()) {
        opsCodeGenerator.getStatic(
            out, constantPool, "java/lang/System", "out", "Ljava/io/PrintStream;");
        ExpressionResult result = expressionCodeGenerator.evaluateExpression(out, variables, param);
        print(out, result.getType());
      }
    } else {
      String methodName =
          methodCall.getNames().get(methodCall.getNames().size() - 1).getValue().toString();
      for (Expression exp : methodCall.getParameters()) {
        expressionCodeGenerator.evaluateExpression(out, variables, exp);
      }
      opsCodeGenerator.invokeStatic(
          out,
          constantPool,
          clazz.getName().getValue().toString(),
          methodName,
          methodMap.get(methodName).getTypeDescr());
    }
    return out;
  }

  /**
   * generate code to call the appropriate println method for the specified type this will print the
   * top element on the stack
   */
  private DynamicByteArray print(DynamicByteArray out, Type type) {
    if (type.getValue().equals("java.lang.String".toUpperCase())) {
      opsCodeGenerator.invokeVirtual(
          out, constantPool, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    } else if (type.getValue().equals("INT")) {
      opsCodeGenerator.invokeVirtual(out, constantPool, "java/io/PrintStream", "println", "(I)V");
    } else {
      // TODO: call toString() first
    }
    return out;
  }
}
