package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadDouble;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadInteger;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadLong;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadValue;
import static io.github.martinschneider.orzo.codegen.OpCodes.DADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.DMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.DREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.FADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.FMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.FREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2B;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2S;
import static io.github.martinschneider.orzo.codegen.OpCodes.IADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.IAND;
import static io.github.martinschneider.orzo.codegen.OpCodes.IDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.IINC;
import static io.github.martinschneider.orzo.codegen.OpCodes.IMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.IOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.IREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISHL;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.IUSHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.IXOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LAND;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDIV;
import static io.github.martinschneider.orzo.codegen.OpCodes.LMUL;
import static io.github.martinschneider.orzo.codegen.OpCodes.LOR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LREM;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSHL;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.LUSHR;
import static io.github.martinschneider.orzo.codegen.OpCodes.LXOR;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.RSHIFTU;
import static io.github.martinschneider.orzo.lexer.tokens.Token.op;
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
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.VariableInfo;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.DoubleNum;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntNum;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.ExpressionParser2;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
    Object val = null;
    if (expr == null) {
      return null;
    }
    List<Token> tokens = new ExpressionParser2(ctx, getMethodNames(ctx.clazz)).postfix(expr.tokens);
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (token instanceof Identifier) {
        Identifier id = (Identifier) token;
        String varType = variables.get(id).type;
        // look ahead for ++ or -- operators because in that case we do not push the val to the
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
          } else if (type.equals(INT) || type.equals(BYTE) || (type.equals(SHORT))) {
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
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        String methodName = methodCall.name.toString();
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
        for (Expression exp : methodCall.params) {
          eval(out, variables, type, exp);
        }
        ctx.opsGenerator.invokeStatic(
            out, ctx.clazz.name.val.toString(), methodName, method.getTypeDescr());
        type = method.type;
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
                incInteger(out, variables.get(tokens.get(i - 1)).idx, (byte) 1);
                break;
              case DOUBLE:
                incDouble(out, variables.get(tokens.get(i - 1)).idx);
                break;
              case FLOAT:
                incFloat(out, variables.get(tokens.get(i - 1)).idx);
                break;
              case LONG:
                incLong(out, variables.get(tokens.get(i - 1)).idx);
                break;
              case BYTE:
                incByte(out, variables.get(tokens.get(i - 1)).idx, (byte) 1);
              case SHORT:
                incShort(out, variables.get(tokens.get(i - 1)).idx, (byte) 1);
            }
            break;
          case POST_DECREMENT:
            switch (type) {
              case INT:
                incInteger(out, variables.get(tokens.get(i - 1)).idx, (byte) -1);
                break;
              case DOUBLE:
                decDouble(out, variables.get(tokens.get(i - 1)).idx);
                break;
              case FLOAT:
                decFloat(out, variables.get(tokens.get(i - 1)).idx);
                break;
              case LONG:
                decLong(out, variables.get(tokens.get(i - 1)).idx);
                break;
              case BYTE:
                incByte(out, variables.get(tokens.get(i - 1)).idx, (byte) -1);
              case SHORT:
                incShort(out, variables.get(tokens.get(i - 1)).idx, (byte) -1);
            }
          default:
        }
      }
    }
    // TODO: this is not working yet
    // what to return if the val cannot be determined at compile time?
    return new ExpressionResult(type, val);
  }

  private List<Identifier> getMethodNames(Clazz clazz) {
    List<Identifier> ids = new ArrayList<>();
    for (Method method : clazz.body) {
      ids.add(method.name);
    }
    return ids;
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

  // long type cannot be increased directly, load to the stack and add
  private HasOutput incLong(DynamicByteArray out, byte idx) {
    loadLong(out, idx);
    ctx.opsGenerator.pushLong(out, 1);
    out.write(LADD);
    return out;
  }

  // there is not long constant for -1, therefore using +1 and LSUB
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
