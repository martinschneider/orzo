package io.github.martinschneider.orzo.util.decompiler;

import io.github.martinschneider.orzo.util.decompiler.Mnemonic.ParameterType;

/** Very simple bytecode decompiler for testing. */
public class BytecodeDecompiler {
  public static String decompile(byte[] input) {
    StringBuilder strBuilder = new StringBuilder();
    for (int i = 0; i < input.length; i++) {
      // The index i must point at an opcode at the beginning of each iteration (this also implies
      // that the first byte in the input must be an opcode).
      short idx = fixSign(input[i]);
      strBuilder.append(Mnemonic.OPCODE[idx]);
      try {
        ParameterType type = Mnemonic.ADDITIONAL_BYTES[idx];
        // special handling for WIDE (196) op code
        if (idx == 196) {
          strBuilder.append(' ');
          idx = fixSign(input[++i]);
          strBuilder.append(Mnemonic.OPCODE[idx]);
          // if WIDE is followed by IINC (132) two shorts will follow (index and increment)
          if (idx == 132) {
            type = ParameterType.TWO_SHORTS;
          }
          // otherwise (iload, fload, aload, lload, dload, istore, fstore, astore, lstore, dstore,
          // ret) one short will follow (index)
          else {
            type = ParameterType.SHORT;
          }
        }
        // TODO: special handling for TABLESWITCH AND LOOKUPSWITCH
        else if (idx == 170 || idx == 171) {
          strBuilder.append(
              " tableswitch and lookupswitch are not supported yet. the remaining output will likely be broken!");
        }
        switch (type) {
          case BYTE:
            i = appendByte(input, i, strBuilder);
            break;
          case TWO_BYTES:
            i = appendByte(input, i, strBuilder);
            i = appendByte(input, i, strBuilder);
            break;
          case SHORT:
            i = appendShort(input, i, strBuilder);
            break;
          case TWO_SHORTS:
            i = appendShort(input, i, strBuilder);
            i = appendShort(input, i, strBuilder);
            break;
          case SHORT_PLUS_ONE_BYTE:
            i = appendShort(input, i, strBuilder);
            i = appendByte(input, i, strBuilder);
            break;
          case SHORT_PLUS_TWO_BYTES:
            i = appendShort(input, i, strBuilder);
            i = appendByte(input, i, strBuilder);
            i = appendByte(input, i, strBuilder);
            break;
          case INT:
            i = appendInt(input, i, strBuilder);
            break;
          case EMPTY:
            break;
          default:
            break;
        }
      } catch (ArrayIndexOutOfBoundsException e) {
        strBuilder.append("\nUNEXPECTED END OF INPUT");
        break;
      }
      if (i < input.length - 1) {
        strBuilder.append("\n");
      }
    }
    return strBuilder.toString();
  }

  private static int appendByte(byte[] input, int i, StringBuilder strBuilder) {
    strBuilder.append(' ');
    strBuilder.append(input[++i]);
    return i;
  }

  private static int appendShort(byte[] input, int i, StringBuilder strBuilder) {
    strBuilder.append(' ');
    strBuilder.append((short) ((input[++i] << 8) | input[++i]));
    return i;
  }

  private static int appendInt(byte[] input, int i, StringBuilder strBuilder) {
    strBuilder.append(' ');
    strBuilder.append(
        (int) ((input[++i] << 24) | (input[++i] << 16) | (input[++i] << 8) | input[++i]));
    return i;
  }

  // Java byte is signed (-128 to 127) but JVM op codes start from 0
  private static short fixSign(short s) {
    if (s < 0) {
      s += 256;
    }
    return s;
  }
}
