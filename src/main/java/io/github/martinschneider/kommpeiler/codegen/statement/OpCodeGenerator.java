package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DCONST_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DCONST_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DLOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DLOAD_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DLOAD_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DLOAD_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DLOAD_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DRETURN;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DSTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DSTORE_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DSTORE_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DSTORE_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DSTORE_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DSUB;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FCONST_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FCONST_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FCONST_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FLOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FLOAD_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FLOAD_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FLOAD_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FLOAD_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FRETURN;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FSTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FSTORE_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FSTORE_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FSTORE_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FSTORE_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FSUB;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GETSTATIC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.I2B;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.I2D;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.I2L;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.I2S;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_4;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_5;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_M1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IINC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ILOAD_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.INVOKESTATIC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.INVOKEVIRTUAL;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IRETURN;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ISTORE_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LADD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LCONST_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LCONST_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LDC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LDC2_W;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LLOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LLOAD_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LLOAD_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LLOAD_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LLOAD_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LRETURN;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSTORE_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSTORE_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSTORE_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSTORE_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LSUB;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.RETURN;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.SIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_DOUBLE;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_FLOAT;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_LONG;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BYTE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.DOUBLE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.FLOAT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.SHORT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.VOID;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

public class OpCodeGenerator {
  public CGContext context;

  // push long
  public HasOutput pushLong(DynamicByteArray out, long number) {
    if (number == 0) {
      out.write(LCONST_0);
    } else if (number == 1) {
      out.write(LCONST_1);
    } else {
      ldc2_w(out, CONSTANT_LONG, number);
    }
    return out;
  }

  // push float
  public HasOutput pushFloat(DynamicByteArray out, float number) {
    if (number == 0) {
      out.write(FCONST_0);
    } else if (number == 1) {
      out.write(FCONST_1);
    } else if (number == 2) {
      out.write(FCONST_2);
    } else {
      ldc(out, CONSTANT_FLOAT, number);
    }
    return out;
  }

  // push double
  public HasOutput pushDouble(DynamicByteArray out, double number) {
    if (number == 0.0) {
      out.write(DCONST_0);
    } else if (number == 1.0) {
      out.write(DCONST_1);
    } else {
      ldc2_w(out, CONSTANT_DOUBLE, number);
    }
    return out;
  }

  // push int, byte or short
  public HasOutput pushInteger(DynamicByteArray out, int number) {
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
      ldc(out, CONSTANT_INTEGER, number);
    }
    return out;
  }

  public HasOutput ldc2_w(DynamicByteArray out, byte type, Object key) {
    out.write(LDC2_W);
    byte idx = (byte) context.constPool.indexOf(type, key);
    if (idx == -1) {
      if (type == CONSTANT_LONG) {
        context.constPool.addLong((Long) key);
      } else if (type == CONSTANT_DOUBLE) {
        context.constPool.addDouble((Double) key);
      }
      idx = (byte) context.constPool.indexOf(type, key);
    }
    out.write((short) (idx - 1));
    return out;
  }

  public HasOutput ldc(DynamicByteArray out, byte type, Object key) {
    byte idx = (byte) context.constPool.indexOf(type, key);
    out.write(LDC);
    out.write(idx);
    return out;
  }

  public HasOutput loadValue(DynamicByteArray out, String type, byte idx) {
    switch (type) {
      case LONG:
        return loadLong(out, idx);
      case DOUBLE:
        return loadDouble(out, idx);
      case FLOAT:
        return loadFloat(out, idx);
      default:
        return loadInteger(out, idx);
    }
  }

  private HasOutput loadDouble(DynamicByteArray out, byte idx) {
    if (idx == 0) {
      out.write(DLOAD_0);
    } else if (idx == 1) {
      out.write(DLOAD_1);
    } else if (idx == 2) {
      out.write(DLOAD_2);
    } else if (idx == 3) {
      out.write(DLOAD_3);
    } else {
      out.write(DLOAD);
      out.write(idx);
    }
    return out;
  }

  private HasOutput loadFloat(DynamicByteArray out, byte idx) {
    if (idx == 0) {
      out.write(FLOAD_0);
    } else if (idx == 1) {
      out.write(FLOAD_1);
    } else if (idx == 2) {
      out.write(FLOAD_2);
    } else if (idx == 3) {
      out.write(FLOAD_3);
    } else {
      out.write(FLOAD);
      out.write(idx);
    }
    return out;
  }

  public HasOutput loadInteger(DynamicByteArray out, byte idx) {
    if (idx == 0) {
      out.write(ILOAD_0);
    } else if (idx == 1) {
      out.write(ILOAD_1);
    } else if (idx == 2) {
      out.write(ILOAD_2);
    } else if (idx == 3) {
      out.write(ILOAD_3);
    } else {
      out.write(ILOAD);
      out.write(idx);
    }
    return out;
  }

  public HasOutput loadLong(DynamicByteArray out, byte idx) {
    if (idx == 0) {
      out.write(LLOAD_0);
    } else if (idx == 1) {
      out.write(LLOAD_1);
    } else if (idx == 2) {
      out.write(LLOAD_2);
    } else if (idx == 3) {
      out.write(LLOAD_3);
    } else {
      out.write(LLOAD);
      out.write(idx);
    }
    return out;
  }

  public HasOutput sipush(DynamicByteArray out, int number) {
    out.write(SIPUSH);
    out.write((short) number);
    return out;
  }

  public HasOutput storeLong(HasOutput out, int idx) {
    if (idx == 0) {
      out.write(LSTORE_0);
    } else if (idx == 1) {
      out.write(LSTORE_1);
    } else if (idx == 2) {
      out.write(LSTORE_2);
    } else if (idx == 3) {
      out.write(LSTORE_3);
    } else {
      out.write(LSTORE);
      out.write((byte) idx);
    }
    return out;
  }

  private HasOutput storeDouble(DynamicByteArray out, byte idx) {
    if (idx == 0) {
      out.write(DSTORE_0);
    } else if (idx == 1) {
      out.write(DSTORE_1);
    } else if (idx == 2) {
      out.write(DSTORE_2);
    } else if (idx == 3) {
      out.write(DSTORE_3);
    } else {
      out.write(DSTORE);
      out.write(idx);
    }
    return out;
  }

  private HasOutput storeFloat(DynamicByteArray out, byte idx) {
    if (idx == 0) {
      out.write(FSTORE_0);
    } else if (idx == 1) {
      out.write(FSTORE_1);
    } else if (idx == 2) {
      out.write(FSTORE_2);
    } else if (idx == 3) {
      out.write(FSTORE_3);
    } else {
      out.write(FSTORE);
      out.write(idx);
    }
    return out;
  }

  public HasOutput storeInteger(HasOutput out, int idx) {
    if (idx == 0) {
      out.write(ISTORE_0);
    } else if (idx == 1) {
      out.write(ISTORE_1);
    } else if (idx == 2) {
      out.write(ISTORE_2);
    } else if (idx == 3) {
      out.write(ISTORE_3);
    } else {
      out.write(ISTORE);
      out.write((byte) idx);
    }
    return out;
  }

  public HasOutput bipush(DynamicByteArray out, int number) {
    out.write(BIPUSH);
    out.write((byte) number);
    return out;
  }

  public HasOutput ret(
      DynamicByteArray out, VariableMap variables, String type, Expression retValue) {
    context.exprGenerator.eval(out, variables, type, retValue);
    switch (type) {
      case INT:
        out.write(IRETURN);
        return out;
      case LONG:
        out.write(LRETURN);
        return out;
      case DOUBLE:
        out.write(DRETURN);
        return out;
      case FLOAT:
        out.write(FRETURN);
        return out;
      case VOID:
        out.write(RETURN);
        return out;
    }
    return out;
  }

  public HasOutput assignValue(
      DynamicByteArray out, VariableMap variables, String type, Identifier id) {
    if (!variables.getVariables().containsKey(id)) {
      variables.put(id, new VariableInfo(id.getValue().toString(), type, (byte) variables.size()));
    }
    byte idx = variables.get(id).getIdx();
    switch (type) {
      case BYTE:
        return storeInteger(out, idx);
      case SHORT:
        return storeInteger(out, idx);
      case LONG:
        return storeLong(out, idx);
      case INT:
        return storeInteger(out, idx);
      case DOUBLE:
        return storeDouble(out, idx);
      case FLOAT:
        return storeFloat(out, idx);
    }
    return out;
  }

  public HasOutput incInteger(DynamicByteArray out, byte idx, byte value) {
    out.write(IINC);
    out.write(idx);
    out.write(value);
    loadInteger(out, idx);
    return out;
  }

  // byte cannot be increased directly, load to the stack and convert
  public HasOutput incByte(DynamicByteArray out, byte idx, byte value) {
    loadInteger(out, idx);
    pushInteger(out, value);
    out.write(IADD);
    out.write(I2B);
    return out;
  }

  // short cannot be increased directly, load to the stack and convert
  public HasOutput incShort(DynamicByteArray out, byte idx, byte value) {
    loadInteger(out, idx);
    pushInteger(out, value);
    out.write(IADD);
    out.write(I2S);
    return out;
  }

  // long type cannot be increased directly, load to the stack and add
  public HasOutput incLong(DynamicByteArray out, byte idx) {
    loadLong(out, idx);
    pushLong(out, 1);
    out.write(LADD);
    return out;
  }

  // there is not long constant for -1, therefore using +1 and LSUB
  // to avoid explicitly storing -1 in the constant pool
  public HasOutput decLong(DynamicByteArray out, byte idx) {
    loadLong(out, idx);
    pushLong(out, 1);
    out.write(LSUB);
    return out;
  }

  public void incDouble(DynamicByteArray out, byte idx) {
    loadDouble(out, idx);
    pushDouble(out, 1);
    out.write(DADD);
  }

  public void incFloat(DynamicByteArray out, byte idx) {
    loadDouble(out, idx);
    pushDouble(out, 1);
    out.write(FADD);
  }

  public void decDouble(DynamicByteArray out, byte idx) {
    loadDouble(out, idx);
    pushDouble(out, 1);
    out.write(DSUB);
  }

  public void decFloat(DynamicByteArray out, byte idx) {
    loadDouble(out, idx);
    pushDouble(out, 1);
    out.write(FSUB);
  }

  public HasOutput getStatic(DynamicByteArray out, String clazz, String field, String type) {
    out.write(GETSTATIC);
    out.write(context.constPool.indexOf(CONSTANT_FIELDREF, clazz, field, type));
    return out;
  }

  public HasOutput invokeVirtual(DynamicByteArray out, String clazz, String field, String type) {
    out.write(INVOKEVIRTUAL);
    out.write(context.constPool.indexOf(CONSTANT_METHODREF, clazz, field, type));
    return out;
  }

  public HasOutput invokeStatic(DynamicByteArray out, String clazz, String field, String type) {
    out.write(INVOKESTATIC);
    out.write(context.constPool.indexOf(CONSTANT_METHODREF, clazz, field, type));
    return out;
  }

  public void convert(DynamicByteArray out, String from, String to) {
    if (from.equals(INT) && to.equals(DOUBLE)) {
      out.write(I2D);
    } else if (from.equals(INT) && to.equals(LONG)) {
      out.write(I2L);
    }
    // TODO: others
  }
}
