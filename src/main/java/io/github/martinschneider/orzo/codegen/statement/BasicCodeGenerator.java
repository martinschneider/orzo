package io.github.martinschneider.orzo.codegen.statement;

import static io.github.martinschneider.orzo.codegen.OpCodes.DCONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.DCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.FCONST_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.GETSTATIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2D;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2L;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_4;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_5;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_M1;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKESTATIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKEVIRTUAL;
import static io.github.martinschneider.orzo.codegen.OpCodes.LCONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.LCONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDC;
import static io.github.martinschneider.orzo.codegen.OpCodes.LDC2_W;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_DOUBLE;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FLOAT;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_LONG;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.orzo.codegen.statement.LoadGenerator.loadReference;
import static io.github.martinschneider.orzo.codegen.statement.PushGenerator.bipush;
import static io.github.martinschneider.orzo.codegen.statement.PushGenerator.sipush;
import static io.github.martinschneider.orzo.codegen.statement.StoreGenerator.storeInArray;
import static io.github.martinschneider.orzo.codegen.statement.StoreGenerator.storeReference;
import static io.github.martinschneider.orzo.codegen.statement.StoreGenerator.storeValue;
import static io.github.martinschneider.orzo.codegen.statement.TypeUtils.getLoadOpCode;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableInfo;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.List;

public class BasicCodeGenerator {
  public CGContext ctx;

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
    byte idx = (byte) ctx.constPool.indexOf(type, key);
    if (idx == -1) {
      if (type == CONSTANT_LONG) {
        ctx.constPool.addLong((Long) key);
      } else if (type == CONSTANT_DOUBLE) {
        ctx.constPool.addDouble((Double) key);
      }
      idx = (byte) ctx.constPool.indexOf(type, key);
    }
    out.write((short) (idx - 1));
    return out;
  }

  public HasOutput ldc(DynamicByteArray out, byte type, Object key) {
    byte idx = (byte) ctx.constPool.indexOf(type, key);
    out.write(LDC);
    out.write(idx);
    return out;
  }

  public HasOutput loadValueFromArray(
      DynamicByteArray out,
      VariableMap variables,
      List<Expression> indices,
      String type,
      byte idx) {
    loadReference(out, idx);
    for (Expression arrIdx : indices) {
      ctx.exprGenerator.eval(out, variables, INT, arrIdx);
    }
    out.write(getLoadOpCode(type.replaceAll("\\[", "")));
    return out;
  }

  public HasOutput assign(DynamicByteArray out, VariableMap variables, String type, Identifier id) {
    if (!variables.containsKey(id)) {
      variables.put(id, new VariableInfo(id.val.toString(), type, (byte) variables.size));
    }
    return storeValue(out, type, variables.get(id).idx);
  }

  public HasOutput assignInArray(
      DynamicByteArray out, VariableMap variables, Identifier id, Expression val) {
    if (!variables.containsKey(id)) {
      variables.put(id, new VariableInfo(id.val.toString(), REF, (byte) variables.size));
    }
    VariableInfo varInfo = variables.get(id);
    byte idx = varInfo.idx;
    String type = varInfo.arrType;
    loadReference(out, idx);
    for (Expression arrIdx : id.arrSel.exprs) {
      ctx.exprGenerator.eval(out, variables, INT, arrIdx);
    }
    ctx.exprGenerator.eval(out, variables, type, val);
    storeInArray(out, type);
    return out;
  }

  public HasOutput assignArray(
      DynamicByteArray out, VariableMap variables, String type, int arrDim, Identifier id) {
    if (!variables.containsKey(id)) {
      variables.put(id, new VariableInfo(id.val.toString(), REF, type, (byte) variables.size));
    }
    byte idx = variables.get(id).idx;
    return storeReference(out, idx);
  }

  public HasOutput getStatic(DynamicByteArray out, String clazz, String field, String type) {
    out.write(GETSTATIC);
    out.write(ctx.constPool.indexOf(CONSTANT_FIELDREF, clazz, field, type));
    return out;
  }

  public HasOutput invokeVirtual(DynamicByteArray out, String clazz, String field, String type) {
    out.write(INVOKEVIRTUAL);
    out.write(ctx.constPool.indexOf(CONSTANT_METHODREF, clazz, field, type));
    return out;
  }

  public HasOutput invokeStatic(DynamicByteArray out, String clazz, String field, String type) {
    out.write(INVOKESTATIC);
    out.write(ctx.constPool.indexOf(CONSTANT_METHODREF, clazz, field, type));
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
