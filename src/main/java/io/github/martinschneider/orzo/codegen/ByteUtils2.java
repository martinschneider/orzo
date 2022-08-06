package io.github.martinschneider.orzo.codegen;

public class ByteUtils2 {
  /*
   * TODO: This is broken in some nasty way leading to out of memory errors:
   * java.lang.OutOfMemoryError: Java heap space at
   * java.base/java.util.Arrays.copyOf(Arrays.java:3537) at
   * java.base/java.io.ByteArrayOutputStream.ensureCapacity(ByteArrayOutputStream.
   * java:100) at
   * java.base/java.io.ByteArrayOutputStream.write(ByteArrayOutputStream.java:130)
   * at java.base/java.io.PrintStream.write(PrintStream.java:568) at
   * java.base/sun.nio.cs.StreamEncoder.writeBytes(StreamEncoder.java:234) at
   * java.base/sun.nio.cs.StreamEncoder.implFlushBuffer(StreamEncoder.java:313) at
   * java.base/sun.nio.cs.StreamEncoder.flushBuffer(StreamEncoder.java:111) at
   * java.base/java.io.OutputStreamWriter.flushBuffer(OutputStreamWriter.java:178)
   * at java.base/java.io.PrintStream.writeln(PrintStream.java:723) at
   * java.base/java.io.PrintStream.println(PrintStream.java:956) at
   * io.github.martinschneider.orzo.tests.Longs.main(Longs.java)
   *
   */
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
