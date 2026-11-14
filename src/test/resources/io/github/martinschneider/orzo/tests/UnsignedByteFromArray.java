package io.github.martinschneider.orzo.tests;

public class UnsignedByteFromArray {
  public static void main(String[] args) {
    byte[] data = new byte[] {(byte) 0xa3};
    System.out.println(data[0] & 0xFF);
    System.out.println(0xFF & data[0]);
    int i = 0;
    System.out.println(data[i] & 0xFF);
  }
}
