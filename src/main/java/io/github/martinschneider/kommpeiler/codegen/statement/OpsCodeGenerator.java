package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DRETURN;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.GETSTATIC;
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
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LDC;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.RETURN;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.SIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.kommpeiler.parser.productions.BasicType.INT;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Type;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class OpsCodeGenerator {
  private ExpressionGenerator exprGenerator;
  private ConstantPool constPool;

  public OpsCodeGenerator(ConstantPool constPool) {
    this.constPool = constPool;
  }

  public void setExpressionCodeGenerator(ExpressionGenerator exprGenerator) {
    this.exprGenerator = exprGenerator;
  }

  public HasOutput pushInteger(DynamicByteArray out, ConstantPool constPool, int number) {
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
    } else if (number >= -128 && number < 128) {
      bipush(out, number);
    } else if (number >= -32768 && number < 32768) {
      sipush(out, number);
    } else {
      ldc(out, CONSTANT_INTEGER, number);
    }
    return out;
  }

  public HasOutput ldc(DynamicByteArray out, byte type, Object key) {
    out.write(LDC);
    out.write((byte) constPool.indexOf(type, key));
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

  public HasOutput sipush(DynamicByteArray out, int number) {
    out.write(SIPUSH);
    out.write((short) number);
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
      DynamicByteArray out,
      Clazz clazz,
      Map<Identifier, Integer> variables,
      ConstantPool constPool,
      String type,
      Expression retValue) {
    exprGenerator.eval(out, variables, retValue);
    switch (type) {
      case "INT":
        out.write(IRETURN);
        return out;
      case "DOUBLE":
        out.write(DRETURN);
        return out;
      case "VOID":
        out.write(RETURN);
        return out;
    }
    return out;
  }

  public HasOutput assignValue(
      DynamicByteArray out, Map<Identifier, Integer> variables, Type type, Identifier name) {
    if (type.getValue().equals(INT.name())) {
      return assignInteger(variables, out, name);
    }
    return out;
  }

  public HasOutput assignInteger(
      Map<Identifier, Integer> variables, HasOutput out, Identifier var) {
    variables.computeIfAbsent(var, x -> variables.size());
    int index = variables.get(var);
    return storeInteger(out, index);
  }

  public HasOutput incInteger(DynamicByteArray out, byte idx, byte value) {
    out.write(IINC);
    out.write(idx);
    out.write(value);
    loadInteger(out, idx);
    return out;
  }

  public HasOutput getStatic(
      DynamicByteArray out, ConstantPool constPool, String clazz, String field, String type) {
    out.write(GETSTATIC);
    out.write(constPool.indexOf(CONSTANT_FIELDREF, clazz, field, type));
    return out;
  }

  public HasOutput invokeVirtual(
      DynamicByteArray out, ConstantPool constPool, String clazz, String field, String type) {
    out.write(INVOKEVIRTUAL);
    out.write(constPool.indexOf(CONSTANT_METHODREF, clazz, field, type));
    return out;
  }

  public HasOutput invokeStatic(
      DynamicByteArray out, ConstantPool constPool, String clazz, String field, String type) {
    out.write(INVOKESTATIC);
    out.write(constPool.indexOf(CONSTANT_METHODREF, clazz, field, type));
    return out;
  }
}
