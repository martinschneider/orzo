package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ALOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.DLOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.FLOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2B;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2C;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2S;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDC;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDC2_W;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_3;
import static io.github.martinschneider.orzo.codegen.TypeUtils.getLoadOpCode;
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
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableInfo;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.List;

public class LoadGenerator {
  public CGContext ctx;

  public LoadGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  private HasOutput load(DynamicByteArray out, String type, short idx) {
    ctx.opStack.push(type);
    switch (type) {
      case LONG:
        return loadLong(out, idx);
      case DOUBLE:
        return loadDouble(out, idx);
      case FLOAT:
        return loadFloat(out, idx);
      case REF:
        return loadReference(out, idx);
      case BOOLEAN:
        return loadInteger(out, idx);
      case SHORT:
        loadInteger(out, idx);
        out.write(I2S);
        return out;
      case BYTE:
        loadInteger(out, idx);
        out.write(I2B);
        return out;
      case CHAR:
        loadInteger(out, idx);
        out.write(I2C);
        return out;
      case INT:
        return loadInteger(out, idx);
    }
    return out;
  }

  public HasOutput ldc(DynamicByteArray out, byte type, Object key) {
    ctx.opStack.push(mapConstantPoolType(type));
    byte idx = (byte) ctx.constPool.indexOf(type, key);
    out.write(LDC);
    out.write(idx);
    return out;
  }

  public HasOutput ldc2_w(DynamicByteArray out, byte type, Object key) {
    ctx.opStack.push(mapConstantPoolType(type));
    out.write(LDC2_W);
    short idx = ctx.constPool.indexOf(type, key, true);
    if (idx == -1) {
      if (type == CONSTANT_LONG) {
        ctx.opStack.push(LONG);
        ctx.constPool.addLong((Long) key);
      } else if (type == CONSTANT_DOUBLE) {
        ctx.opStack.push(DOUBLE);
        ctx.constPool.addDouble((Double) key);
      }
      idx = ctx.constPool.indexOf(type, key);
    }
    out.write((short) (idx - 1));
    return out;
  }

  public HasOutput loadValueFromArray(
      DynamicByteArray out, VariableMap variables, List<Expression> indices, VariableInfo varInfo) {
    load(out, varInfo);
    loadValueFromArrayOnStack(out, variables, indices, varInfo.arrType);
    return out;
  }

  public HasOutput loadValueFromArrayOnStack(
      DynamicByteArray out, VariableMap variables, List<Expression> indices, String type) {
    for (Expression arrIdx : indices) {
      ctx.exprGen.eval(out, variables, INT, arrIdx);
    }
    out.write(getLoadOpCode(type.replaceAll("\\[", "")));
    return out;
  }

  public HasOutput getStatic(DynamicByteArray out, short idx) {
    out.write(GETSTATIC);
    out.write(idx);
    return out;
  }

  private HasOutput loadDouble(DynamicByteArray out, short idx) {
    if (idx == 0) {
      out.write(DLOAD_0);
    } else if (idx == 1) {
      out.write(DLOAD_1);
    } else if (idx == 2) {
      out.write(DLOAD_2);
    } else if (idx == 3) {
      out.write(DLOAD_3);
    } else {
      ctx.basicGen.wide(out, idx, DLOAD);
    }
    return out;
  }

  private HasOutput loadFloat(DynamicByteArray out, short idx) {
    if (idx == 0) {
      out.write(FLOAD_0);
    } else if (idx == 1) {
      out.write(FLOAD_1);
    } else if (idx == 2) {
      out.write(FLOAD_2);
    } else if (idx == 3) {
      out.write(FLOAD_3);
    } else {
      ctx.basicGen.wide(out, idx, FLOAD);
    }
    return out;
  }

  private HasOutput loadInteger(DynamicByteArray out, short idx) {
    if (idx == 0) {
      out.write(ILOAD_0);
    } else if (idx == 1) {
      out.write(ILOAD_1);
    } else if (idx == 2) {
      out.write(ILOAD_2);
    } else if (idx == 3) {
      out.write(ILOAD_3);
    } else {
      ctx.basicGen.wide(out, idx, ILOAD);
    }
    return out;
  }

  private HasOutput loadLong(DynamicByteArray out, short idx) {
    if (idx == 0) {
      out.write(LLOAD_0);
    } else if (idx == 1) {
      out.write(LLOAD_1);
    } else if (idx == 2) {
      out.write(LLOAD_2);
    } else if (idx == 3) {
      out.write(LLOAD_3);
    } else {
      ctx.basicGen.wide(out, idx, LLOAD);
    }
    return out;
  }

  private HasOutput loadReference(DynamicByteArray out, short idx) {
    if (idx == 0) {
      out.write(ALOAD_0);
    } else if (idx == 1) {
      out.write(ALOAD_1);
    } else if (idx == 2) {
      out.write(ALOAD_2);
    } else if (idx == 3) {
      out.write(ALOAD_3);
    } else {
      ctx.basicGen.wide(out, idx, ALOAD);
    }
    return out;
  }

  private String mapConstantPoolType(byte type) {
    switch (type) {
      case CONSTANT_INTEGER:
        return INT;
      case CONSTANT_FLOAT:
        return FLOAT;
      case CONSTANT_LONG:
        return LONG;
      case CONSTANT_DOUBLE:
        return DOUBLE;
      default:
        return REF;
    }
  }

  public void load(DynamicByteArray out, VariableInfo varInfo) {
    if (varInfo.isField) {
      getStatic(out, varInfo.idx);
    } else {
      load(out, varInfo.type, varInfo.idx);
    }
  }
}
