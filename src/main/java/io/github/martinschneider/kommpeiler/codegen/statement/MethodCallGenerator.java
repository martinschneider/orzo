package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionResult;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
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
  private ExpressionGenerator exprGenerator;
  private OpsCodeGenerator opsGenerator;
  private ConstantPool constPool;
  private Map<String, Method> methodMap;

  public MethodCallGenerator(
      ExpressionGenerator exprGenerator,
      OpsCodeGenerator opsGenerator,
      ConstantPool constPool,
      Map<String, Method> methodMap) {
    this.exprGenerator = exprGenerator;
    this.opsGenerator = opsGenerator;
    this.constPool = constPool;
    this.methodMap = methodMap;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement stmt,
      Method method,
      Clazz clazz) {
    MethodCall methodCall = (MethodCall) stmt;
    if ("System.out.println".equals(methodCall.getQualifiedName())) {
      for (Expression param : methodCall.getParameters()) {
        opsGenerator.getStatic(out, constPool, "java/lang/System", "out", "Ljava/io/PrintStream;");
        ExpressionResult result = exprGenerator.eval(out, variables, param);
        print(out, result.getType());
      }
    } else {
      String methodName =
          methodCall.getNames().get(methodCall.getNames().size() - 1).getValue().toString();
      for (Expression expr : methodCall.getParameters()) {
        exprGenerator.eval(out, variables, expr);
      }
      opsGenerator.invokeStatic(
          out,
          constPool,
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
      opsGenerator.invokeVirtual(
          out, constPool, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    } else if (type.getValue().equals("INT")) {
      opsGenerator.invokeVirtual(out, constPool, "java/io/PrintStream", "println", "(I)V");
    } else {
      // TODO: call toString() first
    }
    return out;
  }
}
