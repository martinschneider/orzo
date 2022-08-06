package io.github.martinschneider.orzo.codegen;

public class ByteUtils {

  public static byte[] shortToByteArray(short val) {
    return new byte[] {(byte) ((val >> 8) & 255), (byte) (val & 255)};
  }

  public static byte[] intToByteArray(int val) {
    return new byte[] {
      (byte) ((val >> 24) & 255),
      (byte) ((val >> 16) & 255),
      (byte) ((val >> 8) & 255),
      (byte) (val & 255)
    };
  }

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

  public static byte[] combine(byte left, byte right) {
    return combine(new byte[] {left}, new byte[] {right});
  }

  public static byte[] combine(byte left, byte[] right) {
    return combine(new byte[] {left}, right);
  }

  public static byte[] combine(byte[] left, byte right) {
    return combine(left, new byte[] {right});
  }

  public static byte[] combine(byte left, short right) {
    return combine(left, shortToByteArray(right));
  }

  public static byte[] combine(short left, byte right) {
    return combine(shortToByteArray(left), right);
  }

  public static byte[] combine(byte[] left, byte[] right) {
    byte[] result = new byte[left.length + right.length];
    for (int i = 0; i < left.length; i++) {
      result[i] = left[i];
    }
    for (int i = 0; i < right.length; i++) {
      result[i + left.length] = right[i];
    }
    return result;
  }
}
