package io.github.martinschneider.orzo.codegen;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class ByteUtilsTest {

  @Test
  public void shortToByteArrayTest() {
    assertArrayEquals(new byte[] {0, 0}, ByteUtils.shortToByteArray((short) 0));
    assertArrayEquals(new byte[] {0, 1}, ByteUtils.shortToByteArray((short) 1));
    assertArrayEquals(new byte[] {-1, -1}, ByteUtils.shortToByteArray((short) -1));
    assertArrayEquals(new byte[] {16, 0}, ByteUtils.shortToByteArray((short) 4096));
    assertArrayEquals(new byte[] {127, -1}, ByteUtils.shortToByteArray(Short.MAX_VALUE));
    assertArrayEquals(new byte[] {-128, 0}, ByteUtils.shortToByteArray(Short.MIN_VALUE));
  }

  @Test
  public void intToByteArrayTest() {
    assertArrayEquals(new byte[] {0, 0, 0, 0}, ByteUtils.intToByteArray(0));
    assertArrayEquals(new byte[] {0, 0, 0, 1}, ByteUtils.intToByteArray(1));
    assertArrayEquals(new byte[] {-1, -1, -1, -1}, ByteUtils.intToByteArray(-1));
    assertArrayEquals(new byte[] {0, 0, 16, 0}, ByteUtils.intToByteArray(4096));
    assertArrayEquals(new byte[] {127, -1, -1, -1}, ByteUtils.intToByteArray(Integer.MAX_VALUE));
    assertArrayEquals(new byte[] {-128, 0, 0, 0}, ByteUtils.intToByteArray(Integer.MIN_VALUE));
  }

  @Test
  public void combineTest() {
    assertArrayEquals(
        new byte[] {1, 2, 3, 4}, ByteUtils.combine(new byte[] {1, 2}, new byte[] {3, 4}));
    assertArrayEquals(new byte[] {1, 2, 3}, ByteUtils.combine((byte) 1, new byte[] {2, 3}));
    assertArrayEquals(new byte[] {1, 2, 3}, ByteUtils.combine(new byte[] {1, 2}, (byte) 3));
    assertArrayEquals(new byte[] {1, 2}, ByteUtils.combine((byte) 1, (byte) 2));
    assertArrayEquals(new byte[] {1, 0, 2}, ByteUtils.combine((byte) 1, (short) 2));
    assertArrayEquals(new byte[] {0, 1, 2}, ByteUtils.combine((short) 1, (byte) 2));
  }
}
