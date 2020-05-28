package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.codegen.ArrayTypes.BYTE_ARRAY;
import static io.github.martinschneider.kommpeiler.codegen.ArrayTypes.DOUBLE_ARRAY;
import static io.github.martinschneider.kommpeiler.codegen.ArrayTypes.FLOAT_ARRAY;
import static io.github.martinschneider.kommpeiler.codegen.ArrayTypes.INT_ARRAY;
import static io.github.martinschneider.kommpeiler.codegen.ArrayTypes.LONG_ARRAY;
import static io.github.martinschneider.kommpeiler.codegen.ArrayTypes.SHORT_ARRAY;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.BALOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.BASTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DALOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.DASTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FALOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.FASTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IALOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.IASTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LALOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.LASTORE;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.SALOAD;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.SASTORE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.BYTE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.DOUBLE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.FLOAT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.INT;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.SHORT;

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
    }
    return 0;
  }
}
