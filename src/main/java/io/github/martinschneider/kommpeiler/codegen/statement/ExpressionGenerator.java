package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IDIV;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IMUL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IREM;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISUB;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.VOID;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionResult;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.ExpressionParser;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.parser.productions.Type;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operators;
import io.github.martinschneider.kommpeiler.scanner.tokens.Str;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpressionGenerator {
  private OpsCodeGenerator opsGenerator;
  private Clazz clazz;
  private ConstantPool constPool;
  private Map<String, Method> methodMap;

  public ExpressionGenerator(
      Clazz clazz,
      ConstantPool constPool,
      Map<String, Method> methodMap,
      OpsCodeGenerator opsGenerator) {
    this.clazz = clazz;
    this.constPool = constPool;
    this.methodMap = methodMap;
    this.opsGenerator = opsGenerator;
  }

  public ExpressionResult eval(
      DynamicByteArray out, Map<Identifier, Integer> variables, Expression expr) {
    return eval(out, variables, expr, true);
  }

  public ExpressionResult eval(
      DynamicByteArray out,
      Map<Identifier, Integer> variables,
      Expression expr,
      boolean pushIfZero) {
    // TODO: support String concatenation
    // TODO: support different types
    // TODO: error handling, e.g. only "+" operator is valid for String concatenation, "%" is not
    // valid for doubles etc.
    Type type = type(VOID);
    Object value = null;
    if (expr == null) {
      return null;
    }
    List<Token> tokens = new ExpressionParser(getMethodNames(clazz)).postfix(expr.getInfix());
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (token instanceof Identifier) {
        Identifier id = (Identifier) token;
        // look ahead for ++ or -- operators because in that case we do not push the value to the
        // stack
        if (i + 1 == tokens.size()
            || (!tokens.get(i + 1).eq(op(POST_DECREMENT))
                && !tokens.get(i + 1).eq(op(POST_INCREMENT)))) {
          opsGenerator.loadInteger(out, variables.get(id).byteValue());
        }
        type = type(INT);
      } else if (token instanceof IntNum) {
        Integer intValue = ((IntNum) token).intValue();
        if (intValue != 0 || pushIfZero) {
          opsGenerator.pushInteger(out, constPool, intValue);
        }
        type = type(INT);
        value = intValue;
      } else if (token instanceof Str) {
        opsGenerator.ldc(out, CONSTANT_STRING, ((Str) token).strValue());
        type = type("java.lang.String");
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        String methodName =
            methodCall.getNames().get(methodCall.getNames().size() - 1).getValue().toString();
        Method method = methodMap.get(methodName);
        for (Expression exp : methodCall.getParameters()) {
          eval(out, variables, exp);
        }
        opsGenerator.invokeStatic(
            out,
            constPool,
            clazz.getName().getValue().toString(),
            methodName,
            method.getTypeDescr());
        type = method.getType();
      } else if (token instanceof Operator) {
        Operators op = ((Operator) token).opValue();
        switch (op) {
          case PLUS:
            out.write(IADD);
            break;
          case MINUS:
            out.write(ISUB);
            break;
          case TIMES:
            out.write(IMUL);
            break;
          case DIV:
            out.write(IDIV);
            break;
          case MOD:
            out.write(IREM);
            break;
          case POST_INCREMENT:
            opsGenerator.incInteger(out, variables.get(tokens.get(i - 1)).byteValue(), (byte) 1);
            break;
          case POST_DECREMENT:
            opsGenerator.incInteger(out, variables.get(tokens.get(i - 1)).byteValue(), (byte) -1);
          default:
        }
      }
    }
    return new ExpressionResult(type, value);
  }

  private List<Identifier> getMethodNames(Clazz clazz) {
    return clazz.getBody().stream().map(Method::getName).collect(Collectors.toList());
  }
}
