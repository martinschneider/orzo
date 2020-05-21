package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IDIV;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IMUL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IREM;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISUB;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.LONG;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.VOID;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.type;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionResult;
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
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
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpressionGenerator {
  public CGContext context;

  public ExpressionResult eval(
      DynamicByteArray out, Map<Identifier, VariableInfo> variables, Expression expr) {
    return eval(out, variables, expr, true);
  }

  public ExpressionResult eval(
      DynamicByteArray out,
      Map<Identifier, VariableInfo> variables,
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
    List<Token> tokens =
        new ExpressionParser(getMethodNames(context.clazz)).postfix(expr.getInfix());
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (token instanceof Identifier) {
        Identifier id = (Identifier) token;
        // look ahead for ++ or -- operators because in that case we do not push the value to the
        // stack
        if (i + 1 == tokens.size()
            || (!tokens.get(i + 1).eq(op(POST_DECREMENT))
                && !tokens.get(i + 1).eq(op(POST_INCREMENT)))) {
          String varType = variables.get(id).getType();
          // TODO: refactor
          if (varType.equals("I")) {
            varType = "INT";
          } else if (varType.equals("L")) {
            varType = "LONG";
          } else if (varType.equals("B")) {
            varType = "BYTE";
          } else if (varType.equals("S")) {
            varType = "SHORT";
          }
          byte varIdx = variables.get(id).getIdx();
          context.opsGenerator.loadValue(out, varType, varIdx);
        }
        type = type(INT);
      } else if (token instanceof IntNum) {
        BigInteger bigInt = (BigInteger) ((IntNum) token).getValue();
        Long intValue = bigInt.longValue();
        if (intValue != 0 || pushIfZero) {
          if (intValue > Integer.MAX_VALUE) {
            type = type(LONG);
            context.opsGenerator.pushLong(out, intValue.longValue());
          } else {
            type = type(INT);
            context.opsGenerator.pushInteger(out, intValue.intValue());
          }
        }
        value = bigInt;
      } else if (token instanceof Str) {
        context.opsGenerator.ldc(out, CONSTANT_STRING, ((Str) token).strValue());
        type = type("java.lang.String");
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        String methodName =
            methodCall.getNames().get(methodCall.getNames().size() - 1).getValue().toString();
        Method method = context.methodMap.get(methodName);
        for (Expression exp : methodCall.getParameters()) {
          eval(out, variables, exp);
        }
        context.opsGenerator.invokeStatic(
            out, context.clazz.getName().getValue().toString(), methodName, method.getTypeDescr());
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
            VariableInfo var = variables.get(tokens.get(i - 1));
            switch (var.getType()) {
              case "INT":
                context.opsGenerator.incInteger(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
                break;
              case "LONG":
                context.opsGenerator.incLong(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
                break;
              case "BYTE":
                context.opsGenerator.incByte(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
              case "SHORT":
                context.opsGenerator.incShort(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
            }
            break;
          case POST_DECREMENT:
            context.opsGenerator.incInteger(
                out, variables.get(tokens.get(i - 1)).getIdx(), (byte) -1);
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
