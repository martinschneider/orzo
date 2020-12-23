package io.github.martinschneider.orzo.util.decompiler;

/** Very simple bytecode decompiler for testing. */
public class BytecodeDecompiler {
  public static String decompile(byte[] input) {
    StringBuilder strBuilder = new StringBuilder();
    for (int i = 0; i < input.length; i++) {
      short idx = input[i];
      // Java byte is signed (-128 to 127) but JVM op codes start from 0
      if (idx < 0) {
        idx += 256;
      }
      strBuilder.append(Mnemonic.OPCODE[idx]);
      for (int j = 0; j < Mnemonic.ADDITIONAL_BYTES[idx]; j++) {
        strBuilder.append(' ');
        try {
          if (idx == 196 && j == 0) { // we excpect another opcode after wide
            strBuilder.append(Mnemonic.OPCODE[input[++i]]);
          } else {
            // TODO: do not just print single byte values, for example when it's clear from the
            // opcode that the value should be interpreted as a short
            strBuilder.append(input[++i]);
          }
        } catch (ArrayIndexOutOfBoundsException e) {
          strBuilder.append("\nUNEXPECTED END OF INPUT");
          break;
        }
      }
      if (i < input.length - 1) {
        strBuilder.append("\n");
      }
    }
    return strBuilder.toString();
  }
}
