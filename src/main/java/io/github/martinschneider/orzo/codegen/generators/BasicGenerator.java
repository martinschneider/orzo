package io.github.martinschneider.orzo.codegen.generators;

import static io.github.martinschneider.orzo.codegen.OpCodes.ARRAYLENGTH;
import static io.github.martinschneider.orzo.codegen.OpCodes.CHECKCAST;
import static io.github.martinschneider.orzo.codegen.OpCodes.INVOKESTATIC;
import static io.github.martinschneider.orzo.codegen.OpCodes.WIDE;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.CAST_OPS;
import static io.github.martinschneider.orzo.codegen.generators.OperatorMaps.CAST_OPS_1;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.TypeUtils;
import java.util.Collections;

public class BasicGenerator {
  private CGContext ctx;

  private static final String LOG_NAME = "basic generator";

  public BasicGenerator(CGContext ctx) {
    this.ctx = ctx;
  }

  public void convert(DynamicByteArray out, String from, String to) {
    addCastingErrors(from, to);
    // TODO: array casts
    if (from != null && to != null) {
      byte[] castBytes =
          CAST_OPS.getOrDefault(from, Collections.emptyMap()).getOrDefault(to, new byte[0]);
      out.write(castBytes);
      if (castBytes.length == 0 && !TypeUtils.isPrimitive(to) && !to.equals(from)) {
        // Reference type cast: emit checkcast instruction
        out.write(CHECKCAST);
        out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, to.replace('.', '/')));
      }
    }
    ctx.opStack.pop();
    ctx.opStack.push(to);
  }

  public void convert1(DynamicByteArray out, String from, String to) {
    // TODO: array casts
    if (from != null && to != null) {
      if (TypeUtils.isPrimitive(from) && !from.equals("void") && STRING.equals(to)) {
        primitiveToString(out, from);
      } else if (TypeUtils.isPrimitive(from)
          && !from.equals("void")
          && !TypeUtils.isPrimitive(to)
          && !to.startsWith("[")) {
        autobox(out, from);
      } else {
        addCastingErrors(from, to);
        byte[] castOps =
            CAST_OPS_1.getOrDefault(from, Collections.emptyMap()).getOrDefault(to, new byte[0]);
        out.write(castOps);
        // Narrowing reference conversion (e.g. erased generic Object → String): emit CHECKCAST
        if (castOps.length == 0
            && !TypeUtils.isPrimitive(from)
            && !TypeUtils.isPrimitive(to)
            && !from.equals(to)
            && !"void".equals(to)
            && isNarrowingReferenceConversion(from, to)) {
          String jvmTo = resolveJvmClassName(to);
          ctx.constPool.addClass(jvmTo);
          out.write(CHECKCAST);
          out.write((short) ctx.constPool.indexOf(CONSTANT_CLASS, jvmTo));
        }
      }
    }
    ctx.opStack.pop();
    ctx.opStack.push(to);
  }

  private void primitiveToString(DynamicByteArray out, String primitiveType) {
    String descriptor;
    switch (primitiveType) {
      case "boolean":
        descriptor = "(Z)Ljava/lang/String;";
        break;
      case "char":
        descriptor = "(C)Ljava/lang/String;";
        break;
      case "long":
        descriptor = "(J)Ljava/lang/String;";
        break;
      case "float":
        descriptor = "(F)Ljava/lang/String;";
        break;
      case "double":
        descriptor = "(D)Ljava/lang/String;";
        break;
      default: // byte, short, int all widen to int on the JVM stack
        descriptor = "(I)Ljava/lang/String;";
        break;
    }
    ctx.constPool.addClass("java/lang/String");
    out.write(INVOKESTATIC);
    out.write(ctx.constPool.indexOf(CONSTANT_METHODREF, "java/lang/String", "valueOf", descriptor));
  }

  private void autobox(DynamicByteArray out, String primitiveType) {
    String wrapperClass;
    String descriptor;
    switch (primitiveType) {
      case "boolean":
        wrapperClass = "java/lang/Boolean";
        descriptor = "(Z)Ljava/lang/Boolean;";
        break;
      case "int":
        wrapperClass = "java/lang/Integer";
        descriptor = "(I)Ljava/lang/Integer;";
        break;
      case "long":
        wrapperClass = "java/lang/Long";
        descriptor = "(J)Ljava/lang/Long;";
        break;
      case "double":
        wrapperClass = "java/lang/Double";
        descriptor = "(D)Ljava/lang/Double;";
        break;
      case "float":
        wrapperClass = "java/lang/Float";
        descriptor = "(F)Ljava/lang/Float;";
        break;
      case "char":
        wrapperClass = "java/lang/Character";
        descriptor = "(C)Ljava/lang/Character;";
        break;
      case "byte":
        wrapperClass = "java/lang/Byte";
        descriptor = "(B)Ljava/lang/Byte;";
        break;
      case "short":
        wrapperClass = "java/lang/Short";
        descriptor = "(S)Ljava/lang/Short;";
        break;
      default:
        return;
    }
    ctx.constPool.addClass(wrapperClass);
    out.write(INVOKESTATIC);
    out.write(ctx.constPool.indexOf(CONSTANT_METHODREF, wrapperClass, "valueOf", descriptor));
  }

  private void addCastingErrors(String from, String to) {
    // I have considered allowing these casts by mapping 0 to false and everything else to true.
    // However, I decided to stick with standard Java behaviour and raise an error instead.
    if (from.equals(BOOLEAN) && !to.equals(BOOLEAN)) {
      ctx.errors.addError(
          LOG_NAME,
          String.format("cannot cast boolean type to %s", to),
          new RuntimeException().getStackTrace());
    } else if (to.equals(BOOLEAN) && !from.equals(BOOLEAN)) {
      ctx.errors.addError(
          LOG_NAME,
          String.format("cannot cast %s type to boolean", from),
          new RuntimeException().getStackTrace());
    }
  }

  public void wide(HasOutput out, short idx, byte opCode) {
    if (idx > Byte.MAX_VALUE) {
      out.write(WIDE);
      out.write(opCode);
      out.write(idx);
    } else {
      out.write(opCode);
      out.write((byte) idx);
    }
  }

  public void wideInc(HasOutput out, short idx, short inc, byte opCode) {
    if (idx > Byte.MAX_VALUE && inc > Byte.MAX_VALUE) {
      out.write(WIDE);
      out.write(opCode);
      out.write(idx);
      out.write(inc);
    } else {
      out.write(opCode);
      out.write((byte) idx);
      out.write((byte) inc);
    }
  }

  public void arrayLength(HasOutput out) {
    out.write(ARRAYLENGTH);
  }

  private boolean isNarrowingReferenceConversion(String from, String to) {
    try {
      Class<?> fromClass = resolveClass(from);
      Class<?> toClass = resolveClass(to);
      return fromClass.isAssignableFrom(toClass) && !toClass.isAssignableFrom(fromClass);
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private Class<?> resolveClass(String typeName) throws ClassNotFoundException {
    try {
      return Class.forName(typeName);
    } catch (ClassNotFoundException e) {
      return Class.forName("java.lang." + typeName);
    }
  }

  private String resolveJvmClassName(String typeName) {
    try {
      return resolveClass(typeName).getName().replace('.', '/');
    } catch (ClassNotFoundException e) {
      return typeName.replace('.', '/');
    }
  }
}
