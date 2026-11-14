package io.github.martinschneider.orzo.tests;

import java.io.ByteArrayOutputStream;

public class ArrayWriteOverload {
  public static void main(String[] args) {
    byte[] data = new byte[] {72, 105};
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(data);
    System.out.println(baos.size());
  }
}
