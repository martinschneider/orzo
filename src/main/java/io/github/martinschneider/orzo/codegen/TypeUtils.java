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
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;

public class TypeUtils {
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
