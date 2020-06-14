package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.ArrayTypes.BYTE_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.CHAR_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.DOUBLE_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.FLOAT_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.INT_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.LONG_ARRAY;
import static io.github.martinschneider.orzo.codegen.ArrayTypes.SHORT_ARRAY;
import static io.github.martinschneider.orzo.codegen.OpCodes.BALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.BASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.CALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.CASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.DALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.DASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.FALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.FASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.IALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.IASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.LALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.LASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.SALOAD;
import static io.github.martinschneider.orzo.codegen.OpCodes.SASTORE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.STRING;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;

public class TypeUtils {
  public static String descr(String type) {
    // TODO: general handling of reference types and arrays
    if (type.contains(STRING)) {
      type = type.replaceAll(STRING, "Ljava/lang/String;");
      return type;
    } else if (type.contains(BYTE)) {
      type = type.replaceAll(BYTE, "B");
    } else if (type.contains(CHAR)) {
      type = type.replaceAll(CHAR, "C");
    } else if (type.contains(DOUBLE)) {
      type = type.replaceAll(DOUBLE, "D");
    } else if (type.contains(FLOAT)) {
      type = type.replaceAll(FLOAT, "F");
    } else if (type.contains(INT)) {
      type = type.replaceAll(INT, "I");
    } else if (type.contains(LONG)) {
      type = type.replaceAll(LONG, "J");
    } else if (type.contains(SHORT)) {
      type = type.replaceAll(SHORT, "S");
    } else if (type.contains(VOID)) {
      type = type.replaceAll(VOID, "V");
    } else if (type.contains(BOOLEAN)) {
      type = type.replaceAll(BOOLEAN, "Z");
    }
    return type;
  }

  public static byte getLoadOpCode(String type) {
    switch (type) {
      case INT:
        return IALOAD;
      case BYTE:
        return BALOAD;
      case SHORT:
        return SALOAD;
      case LONG:
        return LALOAD;
      case DOUBLE:
        return DALOAD;
      case FLOAT:
        return FALOAD;
      case CHAR:
        return CALOAD;
    }
    return 0;
  }

  public static byte getStoreOpCode(String type) {
    switch (type) {
      case INT:
        return IASTORE;
      case BYTE:
        return BASTORE;
      case SHORT:
        return SASTORE;
      case LONG:
        return LASTORE;
      case DOUBLE:
        return DASTORE;
      case FLOAT:
        return FASTORE;
      case CHAR:
        return CASTORE;
    }
    return 0;
  }

  public static byte getArrayType(String type) {
    switch (type) {
      case INT:
        return INT_ARRAY;
      case BYTE:
        return BYTE_ARRAY;
      case SHORT:
        return SHORT_ARRAY;
      case LONG:
        return LONG_ARRAY;
      case DOUBLE:
        return DOUBLE_ARRAY;
      case FLOAT:
        return FLOAT_ARRAY;
      case CHAR:
        return CHAR_ARRAY;
    }
    return 0;
  }
}
