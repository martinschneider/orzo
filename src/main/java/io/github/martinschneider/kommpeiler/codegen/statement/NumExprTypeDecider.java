package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.DOUBLE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import io.github.martinschneider.kommpeiler.scanner.tokens.IntNum;
import io.github.martinschneider.kommpeiler.scanner.tokens.Token;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public class NumExprTypeDecider {
  public CGContext context;

  public String getType(VariableMap variables, Expression expr) {
    Set<String> types = new HashSet<>();
    for (Token token : expr.getInfix()) {
      if (token instanceof IntNum) {
        long intValue = ((BigInteger) token.getValue()).longValue();
        if (intValue > Integer.MAX_VALUE) {
          types.add(LONG);
        } else {
          types.add(INT);
        }
      } else if (token instanceof Identifier) {
        VariableInfo var = variables.get(token);
        if (var != null) {
          types.add(var.getType());
        }
      }
    }
    return getSmallestType(types);
  }

  private String getSmallestType(Set<String> types) {
    if (types.contains(DOUBLE)) {
      return DOUBLE;
    } else if (types.contains(LONG)) {
      return LONG;
    } else {
      return INT;
    }
  }
}
