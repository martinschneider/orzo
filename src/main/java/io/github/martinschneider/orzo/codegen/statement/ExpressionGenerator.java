package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadDouble;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadInteger;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadLong;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadValue;
import static io.github.martinschneider.orzo.codegen.OpCodes.DADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.FADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2B;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2C;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2S;
import static io.github.martinschneider.orzo.codegen.OpCodes.IADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.IINC;
import static io.github.martinschneider.orzo.codegen.OpCodes.LADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSUB;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
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
import io.github.martinschneider.orzo.codegen.VariableInfo;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.DoubleNum;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntNum;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class ExpressionGenerator {
  public CGContext ctx;
  private static final String LOGGER_NAME = "expression code generator";

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
      type = new NumExprTypeDecider(ctx).getType(variables, expr);
    }
    Object val = null;
    if (expr == null) {
      return null;
    }
    List<Token> tokens = expr.tokens;
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (token instanceof Identifier) {
        Identifier id = (Identifier) token;
        String varType = variables.get(id).type;
        // look ahead for ++ or -- operators because in that case we do not push the value to the
        // stack
        if (i + 1 == tokens.size()
            || (!tokens.get(i + 1).eq(op(POST_DECREMENT))
                && !tokens.get(i + 1).eq(op(POST_INCREMENT)))) {
          VariableInfo varInfo = variables.get(id);
          byte varIdx = varInfo.idx;
          if (id.arrSel != null) {
            // array
            ctx.opsGenerator.loadValueFromArray(
                out, variables, id.arrSel.exprs, varInfo.arrType, varIdx);
            type = varInfo.arrType;
          } else {
            loadValue(out, varType, varIdx);
          }
        }
        if (!type.equals(varType)) {
          ctx.opsGenerator.convert(out, varType, type);
        }
      } else if (token instanceof IntNum) {
        BigInteger bigInt = (BigInteger) ((IntNum) token).val;
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
          } else if (type.equals(INT)
              || type.equals(BYTE)
              || (type.equals(SHORT))
              || type.equals(CHAR)) {
            ctx.opsGenerator.pushInteger(out, intValue.intValue());
          }
        }
        val = bigInt;
      } else if (token instanceof DoubleNum) {
        BigDecimal bigDec = (BigDecimal) ((DoubleNum) token).val;
        if (type.equals(DOUBLE)) {
          ctx.opsGenerator.pushDouble(out, bigDec.doubleValue());
        } else if (type.equals(FLOAT)) {
          ctx.opsGenerator.pushFloat(out, bigDec.floatValue());
        }
        val = bigDec;
      } else if (token instanceof Str) {
        ctx.opsGenerator.ldc(out, CONSTANT_STRING, ((Str) token).strValue());
        type = STRING;
      } else if (token instanceof Chr) {
        char chr = (char) ((Chr) token).val;
        ctx.opsGenerator.pushInteger(out, chr);
        type = CHAR;
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        String methodName = methodCall.name.toString();
        Method method = ctx.methodMap.get(methodName);
        String clazzName = ctx.clazz.name.val.toString();
        if (methodName.contains(".")) {
          String[] tmp = methodName.split("\\.");
          clazzName = method.fqClassName.replaceAll("\\.", "/");
          methodName = tmp[1];
          ctx.constPool.addClass(clazzName);
          ctx.constPool.addMethodRef(clazzName, methodName, method.getTypeDescr());
        }
        if (method == null) {
          ctx.errors.addError(
              LOGGER_NAME,
              "missing method declaration \""
                  + methodName
                  + "\", known methods: "
                  + ctx.methodMap.keySet());
          return null;
        }
        for (Expression exp : methodCall.params) {
          eval(out, variables, type, exp);
        }
        ctx.opsGenerator.invokeStatic(out, clazzName, methodName, method.getTypeDescr());
        type = method.type;
      } else if (token instanceof Operator) {
        Operators op = ((Operator) token).opValue();
        if (op.equals(POST_INCREMENT)) {
          byte idx = variables.get(tokens.get(i - 1)).idx;
          switch (type) {
            case INT:
              incInteger(out, idx, (byte) 1);
              break;
            case DOUBLE:
              incDouble(out, idx);
              break;
            case FLOAT:
              incFloat(out, idx);
              break;
            case LONG:
              incLong(out, idx);
              break;
            case BYTE:
              incByte(out, idx, (byte) 1);
              break;
            case SHORT:
              incShort(out, idx, (byte) 1);
              break;
            case CHAR:
              incChar(out, idx, (byte) 1);
              break;
          }
        } else if (op.equals(POST_DECREMENT)) {
          byte idx = variables.get(tokens.get(i - 1)).idx;
          switch (type) {
            case INT:
              incInteger(out, idx, (byte) -1);
              break;
            case DOUBLE:
              decDouble(out, idx);
              break;
            case FLOAT:
              decFloat(out, idx);
              break;
            case LONG:
              decLong(out, idx);
              break;
            case BYTE:
              incByte(out, idx, (byte) -1);
              break;
            case SHORT:
              incShort(out, idx, (byte) -1);
              break;
            case CHAR:
              incChar(out, idx, (byte) -1);
              break;
          }
        } else {
          Byte opCode = ArithmeticOperators.map.getOrDefault(op, Collections.emptyMap()).get(type);
          if (opCode != null) {
            out.write(opCode);
          }
        }
      }
    }
    // TODO: this is not working yet
    // what to return if the val cannot be determined at compile time?
    return new ExpressionResult(type, val);
  }

  private HasOutput incInteger(DynamicByteArray out, byte idx, byte val) {
    out.write(IINC);
    out.write(idx);
    out.write(val);
    loadInteger(out, idx);
    return out;
  }

  // byte cannot be increased directly, load to the stack and convert
  private HasOutput incByte(DynamicByteArray out, byte idx, byte val) {
    loadInteger(out, idx);
    ctx.opsGenerator.pushInteger(out, val);
    out.write(IADD);
    out.write(I2B);
    return out;
  }

  // short cannot be increased directly, load to the stack and convert
  private HasOutput incShort(DynamicByteArray out, byte idx, byte val) {
    loadInteger(out, idx);
    ctx.opsGenerator.pushInteger(out, val);
    out.write(IADD);
    out.write(I2S);
    return out;
  }

  // short cannot be increased directly, load to the stack and convert
  private HasOutput incChar(DynamicByteArray out, byte idx, byte val) {
    loadInteger(out, idx);
    ctx.opsGenerator.pushInteger(out, val);
    out.write(IADD);
    out.write(I2C);
    return out;
  }

  // long type cannot be increased directly, load to the stack and add
  private HasOutput incLong(DynamicByteArray out, byte idx) {
    loadLong(out, idx);
    ctx.opsGenerator.pushLong(out, 1);
    out.write(LADD);
    return out;
  }

  // there is no long constant for -1, therefore using +1 and LSUB
  // to avoid explicitly storing -1 in the constant pool
  private HasOutput decLong(DynamicByteArray out, byte idx) {
    loadLong(out, idx);
    ctx.opsGenerator.pushLong(out, 1);
    out.write(LSUB);
    return out;
  }

  private void incDouble(DynamicByteArray out, byte idx) {
    loadDouble(out, idx);
    ctx.opsGenerator.pushDouble(out, 1);
    out.write(DADD);
  }

  private void incFloat(DynamicByteArray out, byte idx) {
    loadDouble(out, idx);
    ctx.opsGenerator.pushDouble(out, 1);
    out.write(FADD);
  }

  private void decDouble(DynamicByteArray out, byte idx) {
    loadDouble(out, idx);
    ctx.opsGenerator.pushDouble(out, 1);
    out.write(DSUB);
  }

  private void decFloat(DynamicByteArray out, byte idx) {
    loadDouble(out, idx);
    ctx.opsGenerator.pushDouble(out, 1);
    out.write(FSUB);
  }
}
