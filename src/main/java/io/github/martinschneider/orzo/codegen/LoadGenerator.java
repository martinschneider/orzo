package io.github.martinschneider.orzo.codegen;

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
import static io.github.martinschneider.orzo.codegen.OpCodes.I2B;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2C;
import static io.github.martinschneider.orzo.codegen.OpCodes.I2S;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.LLOAD_3;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;

public class LoadGenerator {
  public static HasOutput load(DynamicByteArray out, String type, byte idx) {
    switch (type) {
      case LONG:
        return loadLong(out, idx);
      case FLOAT:
        return loadFloat(out, idx);
      case DOUBLE:
        return loadDouble(out, idx);
      case INT:
        return loadInteger(out, idx);
      case SHORT:
        return loadInteger(out, idx);
      case BYTE:
        return loadInteger(out, idx);
      case CHAR:
        return loadInteger(out, idx);
    }
    return out;
  }

  public static HasOutput loadDouble(DynamicByteArray out, byte idx) {
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

  public static HasOutput loadFloat(DynamicByteArray out, byte idx) {
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

  public static HasOutput loadInteger(DynamicByteArray out, byte idx) {
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

  public static HasOutput loadLong(DynamicByteArray out, byte idx) {
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

  public static HasOutput loadReference(DynamicByteArray out, byte idx) {
    if (idx == 0) {
      out.write(ALOAD_0);
    } else if (idx == 1) {
      out.write(ALOAD_1);
    } else if (idx == 2) {
      out.write(ALOAD_2);
    } else if (idx == 3) {
      out.write(ALOAD_3);
    } else {
      out.write(ALOAD);
      out.write(idx);
    }
    return out;
  }

  public static HasOutput loadValue(DynamicByteArray out, String type, byte idx) {
    switch (type) {
      case LONG:
        return loadLong(out, idx);
      case DOUBLE:
        return loadDouble(out, idx);
      case FLOAT:
        return loadFloat(out, idx);
      case REF:
        return loadReference(out, idx);
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
      default:
        return loadInteger(out, idx);
    }
  }
}
