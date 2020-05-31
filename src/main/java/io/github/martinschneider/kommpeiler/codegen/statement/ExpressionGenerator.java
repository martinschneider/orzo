package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DDIV;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DMUL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DREM;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DSUB;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FDIV;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FMUL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FREM;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FSUB;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IAND;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IDIV;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IMUL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IOR;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IREM;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISHL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISHR;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISUB;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IUSHR;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IXOR;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LAND;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LDIV;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LMUL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LOR;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LREM;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSHL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSHR;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSUB;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LUSHR;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LXOR;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.LSHIFT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.RSHIFT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.op;
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
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.ExpressionParser2;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.MethodCall;
import io.github.martinschneider.kommpeiler.scanner.tokens.DoubleNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operator;
import io.github.martinschneider.kommpeiler.scanner.tokens.Operators;
import io.github.martinschneider.kommpeiler.scanner.tokens.Str;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class ExpressionGenerator {
  public CGContext ctx;
  public static final String LOGGER_NAME = "expression code generator";

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
        new ExpressionParser2(ctx, getMethodNames(ctx.clazz)).postfix(expr.getInfix());
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
          VariableInfo varInfo = variables.get(id);
          byte varIdx = varInfo.getIdx();
          if (id.getSelector() != null) {
            // array
            ctx.opsGenerator.loadValueFromArray(
                out, variables, id.getSelector().getExpression(), varInfo.getArrayType(), varIdx);
          } else {
            ctx.opsGenerator.loadValue(out, varType, varIdx);
          }
        }
        if (!type.equals(varType)) {
          ctx.opsGenerator.convert(out, varType, type);
        }
      } else if (token instanceof IntNum) {
        BigInteger bigInt = (BigInteger) ((IntNum) token).getValue();
        Long intValue = bigInt.longValue();
        // look ahead for <<, >> or >>> operators which require the second argument to be an integer
        if (i + 1 < tokens.size()
            && ((tokens.get(i + 1).eq(op(LSHIFT))
                || (tokens.get(i + 1).eq(op(RSHIFT)))
                || (tokens.get(i + 1).eq(op(RSHIFTU)))))) {
          ctx.opsGenerator.pushInteger(out, intValue.intValue());
        } else if (!type.equals(INT) || intValue != 0 || pushIfZero) {
          if (type.equals(LONG)) {
            ctx.opsGenerator.pushLong(out, intValue.longValue());
          } else if (type.equals(DOUBLE)) {
            ctx.opsGenerator.pushDouble(out, intValue.doubleValue());
          } else if (type.equals(FLOAT)) {
            ctx.opsGenerator.pushFloat(out, intValue.floatValue());
          } else if (type.equals(INT) || type.equals(BYTE) || (type.equals(SHORT))) {
            ctx.opsGenerator.pushInteger(out, intValue.intValue());
          }
        }
        value = bigInt;
      } else if (token instanceof DoubleNum) {
        BigDecimal bigDec = (BigDecimal) ((DoubleNum) token).getValue();
        if (type.equals(DOUBLE)) {
          ctx.opsGenerator.pushDouble(out, bigDec.doubleValue());
        } else if (type.equals(FLOAT)) {
          ctx.opsGenerator.pushFloat(out, bigDec.floatValue());
        }
        value = bigDec;
      } else if (token instanceof Str) {
        ctx.opsGenerator.ldc(out, CONSTANT_STRING, ((Str) token).strValue());
        type = STRING;
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        String methodName = methodCall.getName().toString();
        Method method = ctx.methodMap.get(methodName);
        if (method == null) {
          ctx.errors.addError(
              LOGGER_NAME,
              "missing method declaration \""
                  + methodName
                  + "\", known methods: "
                  + ctx.methodMap.keySet());
          return null;
        }
        for (Expression exp : methodCall.getParameters()) {
          eval(out, variables, type, exp);
        }
        ctx.opsGenerator.invokeStatic(
            out, ctx.clazz.getName().getValue().toString(), methodName, method.getTypeDescr());
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
              case DOUBLE:
                out.write(DADD);
                break;
              case FLOAT:
                out.write(FADD);
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
              case DOUBLE:
                out.write(DSUB);
                break;
              case FLOAT:
                out.write(FSUB);
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
              case DOUBLE:
                out.write(DMUL);
                break;
              case FLOAT:
                out.write(FMUL);
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
              case DOUBLE:
                out.write(DDIV);
                break;
              case FLOAT:
                out.write(FDIV);
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
              case DOUBLE:
                out.write(DREM);
                break;
              case FLOAT:
                out.write(FREM);
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
          case LSHIFT:
            switch (type) {
              case INT:
                out.write(ISHL);
                break;
              case LONG:
                out.write(LSHL);
                break;
            }
            break;
          case RSHIFT:
            switch (type) {
              case INT:
                out.write(ISHR);
                break;
              case LONG:
                out.write(LSHR);
                break;
            }
            break;
          case RSHIFTU:
            switch (type) {
              case INT:
                out.write(IUSHR);
                break;
              case LONG:
                out.write(LUSHR);
                break;
            }
            break;
          case BITWISE_AND:
            switch (type) {
              case INT:
                out.write(IAND);
                break;
              case LONG:
                out.write(LAND);
                break;
            }
            break;
          case BITWISE_OR:
            switch (type) {
              case INT:
                out.write(IOR);
                break;
              case LONG:
                out.write(LOR);
                break;
            }
            break;
          case BITWISE_XOR:
            switch (type) {
              case INT:
                out.write(IXOR);
                break;
              case LONG:
                out.write(LXOR);
                break;
            }
            break;
          case POST_INCREMENT:
            switch (type) {
              case INT:
                ctx.opsGenerator.incInteger(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
                break;
              case DOUBLE:
                ctx.opsGenerator.incDouble(out, variables.get(tokens.get(i - 1)).getIdx());
                break;
              case FLOAT:
                ctx.opsGenerator.incFloat(out, variables.get(tokens.get(i - 1)).getIdx());
                break;
              case LONG:
                ctx.opsGenerator.incLong(out, variables.get(tokens.get(i - 1)).getIdx());
                break;
              case BYTE:
                ctx.opsGenerator.incByte(out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
              case SHORT:
                ctx.opsGenerator.incShort(out, variables.get(tokens.get(i - 1)).getIdx(), (byte) 1);
            }
            break;
          case POST_DECREMENT:
            switch (type) {
              case INT:
                ctx.opsGenerator.incInteger(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) -1);
                break;
              case DOUBLE:
                ctx.opsGenerator.decDouble(out, variables.get(tokens.get(i - 1)).getIdx());
                break;
              case FLOAT:
                ctx.opsGenerator.decFloat(out, variables.get(tokens.get(i - 1)).getIdx());
                break;
              case LONG:
                ctx.opsGenerator.decLong(out, variables.get(tokens.get(i - 1)).getIdx());
                break;
              case BYTE:
                ctx.opsGenerator.incByte(out, variables.get(tokens.get(i - 1)).getIdx(), (byte) -1);
              case SHORT:
                ctx.opsGenerator.incShort(
                    out, variables.get(tokens.get(i - 1)).getIdx(), (byte) -1);
            }
          default:
        }
      }
    }
    // TODO: this is not working yet
    // what to return if the value cannot be determined at compile time?
    return new ExpressionResult(type, value);
  }

  private List<Identifier> getMethodNames(Clazz clazz) {
    return clazz.getBody().stream().map(Method::getName).collect(Collectors.toList());
  }
}
