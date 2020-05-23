package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IDIV;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IMUL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IREM;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISUB;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LDIV;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LMUL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LREM;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSUB;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BYTE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.SHORT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.STRING;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionResult;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.ExpressionParser;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operators;
import io.github.martinschneider.kommpeiler.scanner.tokens.Str;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class ExpressionGenerator {
  public CGContext context;

  public ExpressionResult eval(
      DynamicByteArray out, VariableMap variables, String type, Expression expr) {
    return eval(out, variables, type, expr, true);
  }

  public ExpressionResult eval(
      DynamicByteArray out,
      VariableMap variables,
      String type,
      Expression expr,
      boolean pushIfZero) {
    // TODO: support String concatenation
    // TODO: support different types
    // TODO: error handling, e.g. only "+" operator is valid for String concatenation, "%" is not
    // valid for doubles etc.
    if (type == null) {
      type = new NumExprTypeDecider().getType(variables, expr);
    }
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
        String varType = variables.get(id).getType();
        // look ahead for ++ or -- operators because in that case we do not push the value to the
        // stack
        if (i + 1 == tokens.size()
            || (!tokens.get(i + 1).eq(op(POST_DECREMENT))
                && !tokens.get(i + 1).eq(op(POST_INCREMENT)))) {
          byte varIdx = variables.get(id).getIdx();
          context.opsGenerator.loadValue(out, varType, varIdx);
        }
        type = varType;
      } else if (token instanceof IntNum) {
        BigInteger bigInt = (BigInteger) ((IntNum) token).getValue();
        Long intValue = bigInt.longValue();
        if (intValue != 0 || pushIfZero) {
          if (type.equals(LONG)) {
            context.opsGenerator.pushLong(out, intValue.longValue());
          } else if (type.equals(INT) || type.equals(BYTE) || (type.equals(SHORT))) {
            context.opsGenerator.pushInteger(out, intValue.intValue());
          }
        }
        value = bigInt;
      } else if (token instanceof Str) {
        context.opsGenerator.ldc(out, CONSTANT_STRING, ((Str) token).strValue());
        type = STRING;
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        String methodName =
            methodCall.getNames().get(methodCall.getNames().size() - 1).getValue().toString();
        Method method = context.methodMap.get(methodName);
        for (Expression exp : methodCall.getParameters()) {
          eval(out, variables, type, exp);
        }
        context.opsGenerator.invokeStatic(
            out, context.clazz.getName().getValue().toString(), methodName, method.getTypeDescr());
        type = method.getType();
      } else if (token instanceof Operator) {
        Operators op = ((Operator) token).opValue();
        // TODO: map types and op codes more elegantly
        switch (op) {
          case PLUS:
            switch (type) {
              case INT:
                out.write(IADD);
                break;
              case BYTE:
                out.write(IADD);
                break;
              case SHORT:
                out.write(IADD);
                break;
              case LONG:
                out.write(LADD);
            }
            break;
          case MINUS:
            switch (type) {
              case INT:
                out.write(ISUB);
                break;
              case BYTE:
                out.write(ISUB);
                break;
              case SHORT:
                out.write(ISUB);
                break;
              case LONG:
                out.write(LSUB);
            }
            break;
          case TIMES:
            switch (type) {
              case INT:
                out.write(IMUL);
                break;
              case BYTE:
                out.write(IMUL);
                break;
              case SHORT:
                out.write(IMUL);
                break;
              case LONG:
                out.write(LMUL);
            }
            break;
          case DIV:
            switch (type) {
              case INT:
                out.write(IDIV);
                break;
              case BYTE:
                out.write(IDIV);
                break;
              case SHORT:
                out.write(IDIV);
                break;
              case LONG:
                out.write(LDIV);
            }
            break;
          case MOD:
            switch (type) {
              case INT:
                out.write(IREM);
                break;
              case BYTE:
                out.write(IREM);
                break;
              case SHORT:
                out.write(IREM);
                break;
              case LONG:
                out.write(LREM);
            }
            break;
          case POST_INCREMENT:
            switch (type) {
              case INT:
                context.opsGenerator.incInteger(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
                break;
              case LONG:
                context.opsGenerator.incLong(out, variables.get(tokens.get(i - 1)).getIdx());
                break;
              case BYTE:
                context.opsGenerator.incByte(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
              case SHORT:
                context.opsGenerator.incShort(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
            }
            break;
          case POST_DECREMENT:
            switch (type) {
              case INT:
                context.opsGenerator.incInteger(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) -1);
                break;
              case LONG:
                context.opsGenerator.decLong(out, variables.get(tokens.get(i - 1)).getIdx());
                break;
              case BYTE:
                context.opsGenerator.incByte(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) -1);
              case SHORT:
                context.opsGenerator.incShort(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) -1);
            }
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
