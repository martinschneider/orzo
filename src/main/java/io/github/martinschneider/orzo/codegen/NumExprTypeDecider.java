package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;

import io.github.martinschneider.orzo.lexer.tokens.BoolLiteral;
import io.github.martinschneider.orzo.lexer.tokens.FPLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.IntLiteral;
import io.github.martinschneider.orzo.lexer.tokens.Str;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.MethodCall;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NumExprTypeDecider {
  private CGContext ctx;

  public NumExprTypeDecider(CGContext ctx) {
    this.ctx = ctx;
  }

  public String getType(VariableMap variables, Expression expr) {
    Set<String> types = new HashSet<>();
    for (Token token : expr.tokens) {
      if (token instanceof IntLiteral) {
        long intValue = ((BigInteger) token.val).longValue();
        if (intValue > Integer.MAX_VALUE) {
          types.add(LONG);
        } else {
          types.add(INT);
        }
      } else if (token instanceof FPLiteral) {
        types.add(DOUBLE);
      } else if (token instanceof BoolLiteral) {
        types.add(BOOLEAN);
      } else if (token instanceof Str) {
        types.add(STRING);
      } else if (token instanceof Identifier) {
        Identifier id = (Identifier) token;
        VariableInfo var = variables.get(token);
        if (var != null) {
          if (id.arrSel != null) {
            types.add(var.arrType);
          } else if (var.arrType != null) {
            types.add("[" + var.arrType);
          } else {
            types.add(var.type);
          }
        }
      } else if (token instanceof MethodCall) {
        MethodCall methodCall = (MethodCall) token;
        List<String> argTypes = new ArrayList<>();
        for (Expression exp : methodCall.params) {
          argTypes.add(new NumExprTypeDecider(ctx).getType(variables, exp));
        }
        Method method =
            ctx.methodCallGenerator.findMatchingMethod(methodCall.name.toString(), argTypes);
        if (method != null) {
          types.add(method.type);
        }
      }
    }
    return getSmallestType(types);
  }

  private String getSmallestType(Set<String> types) {
    String arrayType = getArrayType(types);
    if (arrayType != null) {
      return arrayType;
    }
    if (types.contains(CHAR)) {
      return CHAR;
    } else if (types.contains(BOOLEAN)) {
      return BOOLEAN;
    } else if (types.contains(DOUBLE)) {
      return DOUBLE;
    } else if (types.contains(FLOAT)) {
      return FLOAT;
    } else if (types.contains(LONG)) {
      return LONG;
    } else if (types.contains(INT)) {
      return INT;
    } else if (types.contains(SHORT)) {
      return SHORT;
    } else if (types.contains(BYTE)) {
      return BYTE;
    } else if (types.contains(STRING)) {
      return STRING;
    }
    return INT;
  }

  private String getArrayType(Set<String> types) {
    for (String type : types) {
      if (type.contains("[")) {
        return type;
      }
    }
    return null;
  }
}
