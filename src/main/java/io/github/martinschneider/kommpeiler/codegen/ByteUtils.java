package io.github.martinschneider.kommpeiler.codegen;

public class ByteUtils {
  public static byte[] shortToByteArray(short value) {
    return new byte[] {(byte) ((value >> 8) & 0xff), (byte) (value & 0xff)};
  }

  public static byte[] shortToByteArray(int value) {
    return new byte[] {(byte) (((short) value >> 8) & 0xff), (byte) (value & 0xff)};
  }

  public static byte[] intToByteArray(int value) {
    return new byte[] {
      (byte) ((value >> 24) & 0xff),
      (byte) ((value >> 16) & 0xff),
      (byte) ((value >> 8) & 0xff),
      (byte) (value & 0xff)
    };
  }

  public static byte[] longToByteArray(long value) {
    return new byte[] {
      (byte) ((value >> 56) & 0xff),
      (byte) ((value >> 48) & 0xff),
      (byte) ((value >> 40) & 0xff),
      (byte) ((value >> 32) & 0xff),
      (byte) ((value >> 24) & 0xff),
      (byte) ((value >> 16) & 0xff),
      (byte) ((value >> 8) & 0xff),
      (byte) (value & 0xff)
    };
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

  public static String bytesToHex(byte[] bytes) {
    StringBuilder result = new StringBuilder();
    for (byte temp : bytes) {
      int decimal = temp & 0xff;
      result.append(Integer.toHexString(decimal));
    }
    return result.toString();
  }
}
