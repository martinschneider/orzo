package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.OpCodes.AASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ASTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.BASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.DASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.DSTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.FASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.FSTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.IASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ISTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.LASTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.LSTORE_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.SASTORE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BOOLEAN;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.CHAR;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.FLOAT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;
import static io.github.martinschneider.orzo.lexer.tokens.Type.REF;
import static io.github.martinschneider.orzo.lexer.tokens.Type.SHORT;

public class StoreGenerator {
  public static HasOutput store(DynamicByteArray out, String type, byte idx) {
    switch (type) {
      case LONG:
        return storeLong(out, idx);
      case FLOAT:
        return storeFloat(out, idx);
      case DOUBLE:
        return storeDouble(out, idx);
      case INT:
        return storeInteger(out, idx);
      case BYTE:
        return storeInteger(out, idx);
      case SHORT:
        return storeInteger(out, idx);
      case CHAR:
        return storeInteger(out, idx);
      case BOOLEAN:
        return storeInteger(out, idx);
    }
    return out;
  }

  public static HasOutput storeValue(DynamicByteArray out, String type, byte idx) {
    switch (type) {
      case BYTE:
        return storeInteger(out, idx);
      case CHAR:
        return storeInteger(out, idx);
      case SHORT:
        return storeInteger(out, idx);
      case LONG:
        return storeLong(out, idx);
      case INT:
        return storeInteger(out, idx);
      case BOOLEAN:
        return storeInteger(out, idx);
      case DOUBLE:
        return storeDouble(out, idx);
      case FLOAT:
        return storeFloat(out, idx);
      case REF:
        return storeReference(out, idx);
    }
    return out;
  }

  public static HasOutput storeLong(HasOutput out, int idx) {
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

  public static HasOutput storeReference(DynamicByteArray out, byte idx) {
    if (idx == 0) {
      out.write(ASTORE_0);
    } else if (idx == 1) {
      out.write(ASTORE_1);
    } else if (idx == 2) {
      out.write(ASTORE_2);
    } else if (idx == 3) {
      out.write(ASTORE_3);
    } else {
      out.write(ASTORE);
      out.write(idx);
    }
    return out;
  }

  public static HasOutput storeDouble(DynamicByteArray out, byte idx) {
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

  public static HasOutput storeFloat(DynamicByteArray out, byte idx) {
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

  public static HasOutput storeInteger(HasOutput out, int idx) {
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

  public static HasOutput storeInArray(HasOutput out, String type) {
    switch (type) {
      case BYTE:
        out.write(BASTORE);
        break;
      case SHORT:
        out.write(SASTORE);
        break;
      case LONG:
        out.write(LASTORE);
        break;
      case INT:
        out.write(IASTORE);
        break;
      case DOUBLE:
        out.write(DASTORE);
        break;
      case FLOAT:
        out.write(FASTORE);
        break;
      case REF:
        out.write(AASTORE);
        break;
    }
    return out;
  }
}
