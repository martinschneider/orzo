package io.github.martinschneider.orzo.codegen;

public class ByteUtils2 {
  // TODO: make this compile properly and move back to ByteUtils
  public static byte[] longToByteArray(long val) {
    return new byte[] {
      (byte) ((val >> 56) & 255),
      (byte) ((val >> 48) & 255),
      (byte) ((val >> 40) & 255),
      (byte) ((val >> 32) & 255),
      (byte) ((val >> 24) & 255),
      (byte) ((val >> 16) & 255),
      (byte) ((val >> 8) & 255),
      (byte) (val & 255)
    };
  }
}
