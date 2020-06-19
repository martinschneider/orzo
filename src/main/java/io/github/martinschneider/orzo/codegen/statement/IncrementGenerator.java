package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadDouble;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadFloat;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadInteger;
import static io.github.martinschneider.orzo.codegen.LoadGenerator.loadLong;
import static io.github.martinschneider.orzo.codegen.OpCodes.DADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP;
import static io.github.martinschneider.orzo.codegen.OpCodes.DUP2;
import static io.github.martinschneider.orzo.codegen.OpCodes.FADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSUB;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2B;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2C;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2S;
import static io.github.martinschneider.orzo.codegen.OpCodes.IADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.IINC;
import static io.github.martinschneider.orzo.codegen.OpCodes.LADD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSUB;
import static io.github.martinschneider.orzo.codegen.StoreGenerator.storeDouble;
import static io.github.martinschneider.orzo.codegen.StoreGenerator.storeFloat;
import static io.github.martinschneider.orzo.codegen.StoreGenerator.storeInteger;
import static io.github.martinschneider.orzo.codegen.StoreGenerator.storeLong;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_DECREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Operators.POST_INCREMENT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Operator;
import io.github.martinschneider.orzo.lexer.tokens.Operators;
import io.github.martinschneider.orzo.lexer.tokens.Token;
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
    if (incr.expr.tokens.size() != 2) {
      ctx.errors.addError(LOGGER_NAME, "invalid unary increment expression");
      return out;
    }
    Token token1 = incr.expr.tokens.get(0);
    Token token2 = incr.expr.tokens.get(1);
    Identifier id;
    Operators op;
    boolean pre = false;
    if (token1 instanceof Identifier) {
      id = (Identifier) token1;
      op = ((Operator) token2).opValue();
    } else {
      id = (Identifier) token2;
      op = ((Operator) token1).opValue();
      pre = true;
    }
    String type = variables.get(id).type;
    byte idx = variables.get(id).idx;
    if (op.equals(POST_INCREMENT)) {
      switch (type) {
        case INT:
          incInteger(out, idx, (byte) 1, pre, true);
          break;
        case DOUBLE:
          incDouble(out, idx, pre, true);
          break;
        case FLOAT:
          incFloat(out, idx, pre, true);
          break;
        case LONG:
          incLong(out, idx, pre, true);
          break;
        case BYTE:
          incByte(out, idx, (byte) 1, pre, true);
          break;
        case SHORT:
          incShort(out, idx, (byte) 1, pre, true);
          break;
        case CHAR:
          incChar(out, idx, (byte) 1, pre, true);
          break;
      }
    } else if (op.equals(POST_DECREMENT)) {
      switch (type) {
        case INT:
          incInteger(out, idx, (byte) -1, pre, true);
          break;
        case DOUBLE:
          decDouble(out, idx, pre, true);
          break;
        case FLOAT:
          decFloat(out, idx, pre, true);
          break;
        case LONG:
          decLong(out, idx, pre, true);
          break;
        case BYTE:
          incByte(out, idx, (byte) -1, pre, true);
          break;
        case SHORT:
          incShort(out, idx, (byte) -1, pre, true);
          break;
        case CHAR:
          incChar(out, idx, (byte) -1, pre, true);
          break;
      }
    }
    return out;
  }

  public HasOutput incInteger(
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

  // byte cannot be increased directly, load to the stack and convert
  public HasOutput incByte(
      DynamicByteArray out, byte idx, byte val, boolean pre, boolean evalOnly) {
    if (pre) {
      loadInteger(out, idx);
      ctx.opsGenerator.pushInteger(out, val);
      out.write(IADD);
      if (!evalOnly) {
        out.write(DUP);
      }
      storeInteger(out, idx);
      if (!evalOnly) {
        out.write(I2B);
      }
    } else {
      loadInteger(out, idx);
      if (!evalOnly) {
        out.write(DUP);
      }
      ctx.opsGenerator.pushInteger(out, val);
      out.write(IADD);
      storeInteger(out, idx);
      if (!evalOnly) {
        out.write(I2B);
      }
    }
    return out;
  }

  // short cannot be increased directly, load to the stack and convert
  public HasOutput incShort(
      DynamicByteArray out, byte idx, byte val, boolean pre, boolean evalOnly) {
    if (pre) {
      loadInteger(out, idx);
      ctx.opsGenerator.pushInteger(out, val);
      out.write(IADD);
      if (!evalOnly) {
        out.write(DUP);
      }
      storeInteger(out, idx);
      if (!evalOnly) {
        out.write(I2S);
      }
    } else {
      loadInteger(out, idx);
      if (!evalOnly) {
        out.write(DUP);
      }
      ctx.opsGenerator.pushInteger(out, val);
      out.write(IADD);
      storeInteger(out, idx);
      if (!evalOnly) {
        out.write(I2S);
      }
    }
    return out;
  }

  // char cannot be increased directly, load to the stack and convert
  public HasOutput incChar(
      DynamicByteArray out, byte idx, byte val, boolean pre, boolean evalOnly) {
    if (pre) {
      loadInteger(out, idx);
      ctx.opsGenerator.pushInteger(out, val);
      out.write(IADD);
      if (!evalOnly) {
        out.write(DUP);
      }
      storeInteger(out, idx);
      if (!evalOnly) {
        out.write(I2C);
      }
    } else {
      loadInteger(out, idx);
      if (!evalOnly) {
        out.write(DUP);
      }
      ctx.opsGenerator.pushInteger(out, val);
      out.write(IADD);
      storeInteger(out, idx);
      if (!evalOnly) {
        out.write(I2C);
      }
    }
    return out;
  }

  // long cannot be increased directly, load to the stack and add
  public HasOutput incLong(DynamicByteArray out, byte idx, boolean pre, boolean evalOnly) {
    if (pre) {
      loadLong(out, idx);
      ctx.opsGenerator.pushLong(out, 1);
      out.write(LADD);
      if (!evalOnly) {
        out.write(DUP2);
      }
      storeLong(out, idx);
    } else {
      loadLong(out, idx);
      if (!evalOnly) {
        out.write(DUP2);
      }
      ctx.opsGenerator.pushLong(out, 1);
      out.write(LADD);
      storeLong(out, idx);
    }
    return out;
  }

  // there is no long constant for -1, therefore using +1 and LSUB
  // to avoid explicitly storing -1 in the constant pool
  public HasOutput decLong(DynamicByteArray out, byte idx, boolean pre, boolean evalOnly) {
    if (pre) {
      loadLong(out, idx);
      ctx.opsGenerator.pushLong(out, 1);
      out.write(LSUB);
      if (!evalOnly) {
        out.write(DUP2);
      }
      storeLong(out, idx);
    } else {
      loadLong(out, idx);
      if (!evalOnly) {
        out.write(DUP2);
      }
      ctx.opsGenerator.pushLong(out, 1);
      out.write(LSUB);
      storeLong(out, idx);
    }
    return out;
  }

  public HasOutput incDouble(DynamicByteArray out, byte idx, boolean pre, boolean evalOnly) {
    if (pre) {
      loadDouble(out, idx);
      ctx.opsGenerator.pushDouble(out, 1);
      out.write(DADD);
      if (!evalOnly) {
        out.write(DUP2);
      }
      storeDouble(out, idx);
    } else {
      loadDouble(out, idx);
      if (!evalOnly) {
        out.write(DUP2);
      }
      ctx.opsGenerator.pushDouble(out, 1);
      out.write(DADD);
      storeDouble(out, idx);
    }
    return out;
  }

  public HasOutput incFloat(DynamicByteArray out, byte idx, boolean pre, boolean evalOnly) {
    if (pre) {
      loadFloat(out, idx);
      ctx.opsGenerator.pushFloat(out, 1);
      out.write(FADD);
      if (!evalOnly) {
        out.write(DUP);
      }
      storeFloat(out, idx);
    } else {
      loadFloat(out, idx);
      if (!evalOnly) {
        out.write(DUP);
      }
      ctx.opsGenerator.pushFloat(out, 1);
      out.write(FADD);
      storeFloat(out, idx);
    }
    return out;
  }

  public HasOutput decDouble(DynamicByteArray out, byte idx, boolean pre, boolean evalOnly) {
    if (pre) {
      loadDouble(out, idx);
      ctx.opsGenerator.pushDouble(out, 1);
      out.write(DSUB);
      if (!evalOnly) {
        out.write(DUP2);
      }
      storeDouble(out, idx);
    } else {
      loadDouble(out, idx);
      if (!evalOnly) {
        out.write(DUP2);
      }
      ctx.opsGenerator.pushDouble(out, 1);
      out.write(DSUB);
      storeDouble(out, idx);
    }
    return out;
  }

  public HasOutput decFloat(DynamicByteArray out, byte idx, boolean pre, boolean evalOnly) {
    if (pre) {
      loadFloat(out, idx);
      ctx.opsGenerator.pushFloat(out, 1);
      out.write(FSUB);
      if (!evalOnly) {
        out.write(DUP);
      }
      storeFloat(out, idx);
    } else {
      loadFloat(out, idx);
      if (!evalOnly) {
        out.write(DUP);
      }
      ctx.opsGenerator.pushFloat(out, 1);
      out.write(FSUB);
      storeFloat(out, idx);
    }
    return out;
  }
}
