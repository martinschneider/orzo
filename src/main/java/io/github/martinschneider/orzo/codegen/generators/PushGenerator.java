package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.orzo.codegen.OpCodes.DCONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.DCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_4;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_5;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_M1;
import static io.github.martinschneider.orzo.codegen.OpCodes.LCONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.LCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.SIPUSH;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_DOUBLE;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FLOAT;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
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

public class PushGenerator {
  public CGContext ctx;

  public PushGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public HasOutput push(DynamicByteArray out, String type, int number) {
    return push(out, type, (double) number);
  }

  public HasOutput push(DynamicByteArray out, String type, double number) {
    ctx.opStack.push(type);
    switch (type) {
      case LONG:
        return pushLong(out, (long) number);
      case FLOAT:
        return pushFloat(out, (float) number);
      case DOUBLE:
        return pushDouble(out, number);
      case INT:
        return pushInteger(out, (int) number);
      case BYTE:
        return pushInteger(out, (int) number);
      case SHORT:
        return pushInteger(out, (int) number);
      case CHAR:
        return pushInteger(out, (int) number);
    }
    return out;
  }

  public HasOutput pushBool(DynamicByteArray out, boolean value) {
    ctx.opStack.push(BOOLEAN);
    return bipush(out, (value) ? 1 : 0);
  }

  private HasOutput pushLong(DynamicByteArray out, long number) {
    if (number == 0) {
      out.write(LCONST_0);
    } else if (number == 1) {
      out.write(LCONST_1);
    } else {
      ctx.loadGen.ldc2_w(out, CONSTANT_LONG, number);
    }
    return out;
  }

  private HasOutput pushFloat(DynamicByteArray out, float number) {
    if (number == 0) {
      out.write(FCONST_0);
    } else if (number == 1) {
      out.write(FCONST_1);
    } else if (number == 2) {
      out.write(FCONST_2);
    } else {
      ctx.constPool.addFloat(number);
      ctx.loadGen.ldc(out, CONSTANT_FLOAT, number);
    }
    return out;
  }

  private HasOutput pushDouble(DynamicByteArray out, double number) {
    if (number == 0.0) {
      out.write(DCONST_0);
    } else if (number == 1.0) {
      out.write(DCONST_1);
    } else {
      ctx.loadGen.ldc2_w(out, CONSTANT_DOUBLE, number);
    }
    return out;
  }

  // push int, byte, short or bool
  private HasOutput pushInteger(DynamicByteArray out, int number) {
    if (number == -1) {
      out.write(ICONST_M1);
    } else if (number == 0) {
      out.write(ICONST_0);
    } else if (number == 1) {
      out.write(ICONST_1);
    } else if (number == 2) {
      out.write(ICONST_2);
    } else if (number == 3) {
      out.write(ICONST_3);
    } else if (number == 4) {
      out.write(ICONST_4);
    } else if (number == 5) {
      out.write(ICONST_5);
    } else if (number >= Byte.MIN_VALUE && number <= Byte.MAX_VALUE) {
      bipush(out, number);
    } else if (number >= Short.MIN_VALUE && number <= Short.MAX_VALUE) {
      sipush(out, number);
    } else {
      ctx.loadGen.ldc(out, CONSTANT_INTEGER, number);
    }
    return out;
  }

  private HasOutput sipush(DynamicByteArray out, int number) {
    out.write(SIPUSH);
    out.write((short) number);
    return out;
  }

  private HasOutput bipush(DynamicByteArray out, int number) {
    out.write(BIPUSH);
    out.write((byte) number);
    ctx.opStack.push(BYTE);
    return out;
  }
}
