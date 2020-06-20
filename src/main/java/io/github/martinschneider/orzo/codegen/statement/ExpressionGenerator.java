package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadValue;
import static io.github.martinschneider.orzo.codegen.PushGenerator.pushBool;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.orzo.codegen.statement.OperatorMaps.arithmeticOps;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.LSHIFT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_INCREMENT;
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
import io.github.martinschneider.orzo.codegen.NumExprTypeDecider;
import io.github.martinschneider.orzo.codegen.VariableInfo;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Chr;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class ExpressionGenerator {
  public CGContext ctx;
  private static final String LOGGER_NAME = "expression code generator";
  private static final List<Operators> INCREMENT_DECREMENT_OPS =
      List.of(POST_INCREMENT, POST_DECREMENT, PRE_INCREMENT, PRE_DECREMENT);

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
        String arrType = variables.get(id).arrType;
        // look ahead for ++ or -- operators because in that case we do not push the value to the
        // stack
        if (i + 1 == tokens.size()
            || (!tokens.get(i + 1).eq(op(POST_DECREMENT))
                && !tokens.get(i + 1).eq(op(POST_INCREMENT))
                && !tokens.get(i + 1).eq(op(PRE_INCREMENT))
                && !tokens.get(i + 1).eq(op(PRE_DECREMENT)))) {
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
        if (!type.equals(varType) && arrType == null) {
          ctx.opsGenerator.convert(out, varType, type);
        }
      } else if (token instanceof IntLiteral) {
        BigInteger bigInt = (BigInteger) ((IntLiteral) token).val;
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
      } else if (token instanceof BoolLiteral) {
        Boolean bool = (Boolean) ((BoolLiteral) token).val;
        pushBool(out, bool);
        val = bool;
      } else if (token instanceof FPLiteral) {
        BigDecimal bigDec = (BigDecimal) ((FPLiteral) token).val;
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
        type = ctx.methodCallGenerator.generate(out, variables, (MethodCall) token);
      } else if (token instanceof Operator) {
        Operators op = ((Operator) token).opValue();
        if (List.of(POST_INCREMENT, POST_DECREMENT, PRE_INCREMENT, PRE_DECREMENT).contains(op)) {
          byte idx = variables.get(tokens.get(i - 1)).idx;
          ctx.incrGenerator.inc(out, type, idx, op, false);
        } else {
          Byte opCode = arithmeticOps.getOrDefault(op, Collections.emptyMap()).get(type);
          if (opCode != null) {
            out.write(opCode);
          }
        }
      }
    }
    return new ExpressionResult(type, val);
  }
}
