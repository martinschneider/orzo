package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DRETURN;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GETSTATIC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.I2B;
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
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_LONG;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.DOUBLE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.VOID;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

public class OpsCodeGenerator {
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

  public HasOutput ldc2_w(DynamicByteArray out, byte type, Long key) {
    out.write(LDC2_W);
    byte idx = (byte) context.constPool.indexOf(type, key);
    if (idx == -1) {
      context.constPool.addLong(key);
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
      default:
        return loadInteger(out, idx);
    }
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
      case VOID:
        out.write(RETURN);
        return out;
    }
    return out;
  }

  public HasOutput assignValue(
      DynamicByteArray out, VariableMap variables, String type, Identifier name) {
    // TODO: support floating point numbers and other types
    return assignInteger(variables, out, type, name);
  }

  // assign long, int, short, byte or long
  public HasOutput assignInteger(VariableMap variables, HasOutput out, String type, Identifier id) {
    if (!variables.getVariables().containsKey(id)) {
      variables.put(id, new VariableInfo(id.getValue().toString(), type, (byte) variables.size()));
    }
    byte index = variables.get(id).getIdx();
    if (type.equals(LONG)) {
      return storeLong(out, index);
    } else {
      // store byte and short as int
      return storeInteger(out, index);
    }
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
}
