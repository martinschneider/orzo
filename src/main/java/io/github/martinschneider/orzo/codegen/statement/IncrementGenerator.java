package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.LoadGenerator.load;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadInteger;
import static io.github.martinschneider.orzo.codegen.OpCodes.IINC;
import static io.github.martinschneider.orzo.codegen.StoreGenerator.store;
import static io.github.martinschneider.orzo.codegen.statement.OperatorMaps.arithmeticOps;
import static io.github.martinschneider.orzo.codegen.statement.OperatorMaps.dupOps;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.MINUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PLUS;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.PRE_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.parser.productions.Increment;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;

public class IncrementGenerator implements StatementGenerator {
  private CGContext ctx;
  private static final String LOGGER_NAME = "increment code generator";

  public IncrementGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    Increment incr = (Increment) stmt;
    if (incr.expr.tokens.size() != 2
        || !(incr.expr.tokens.get(0) instanceof Identifier)
        || !(incr.expr.tokens.get(1) instanceof Operator)) {
      ctx.errors.addError(LOGGER_NAME, "invalid unary increment expression");
      return out;
    }
    Identifier id = (Identifier) incr.expr.tokens.get(0);
    Operators op = ((Operator) incr.expr.tokens.get(1)).opValue();
    String type = variables.get(id).type;
    byte idx = variables.get(id).idx;
    inc(out, type, idx, op, true);
    return out;
  }

  // if evalOnly==true the variable value will be changed but not put/left on the stack
  public HasOutput inc(
      DynamicByteArray out, String type, byte idx, Operators incrOp, boolean evalOnly) {
    boolean pre = false;
    Operators op = PLUS;
    if (incrOp.equals(PRE_DECREMENT) || incrOp.equals(PRE_INCREMENT)) {
      pre = true;
    }
    if (incrOp.equals(PRE_DECREMENT) || incrOp.equals(POST_DECREMENT)) {
      op = MINUS;
    }
    // special handling for integer because there is IINC
    if (type.equals(INT)) {
      return incInt(out, idx, op.equals(PLUS) ? (byte) 1 : (byte) -1, pre, evalOnly);
    }
    if (pre) {
      load(out, type, idx);
      ctx.opsGenerator.push(out, type, 1);
      out.write(arithmeticOps.get(op).get(type));
      if (!evalOnly) {
        out.write(dupOps.get(type));
      }
      store(out, type, idx);
    } else {
      load(out, type, idx);
      if (!evalOnly) {
        out.write(dupOps.get(type));
      }
      ctx.opsGenerator.push(out, type, 1);
      out.write(arithmeticOps.get(op).get(type));
      store(out, type, idx);
    }
    return out;
  }

  private HasOutput incInt(
      DynamicByteArray out, byte idx, byte val, boolean pre, boolean evalOnly) {
    if (!evalOnly && !pre) {
      loadInteger(out, idx);
    }
    out.write(IINC);
    out.write(idx);
    out.write(val);
    if (!evalOnly && pre) {
      loadInteger(out, idx);
    }
    return out;
  }
}
