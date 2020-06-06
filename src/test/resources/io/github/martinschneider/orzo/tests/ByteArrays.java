package io.github.martinschneider.orzo.tests;

public class ByteArrays {
  public static void main(String[] args) {
    byte[] a = new byte[] {1, 2, 3};
    byte[] b = new byte[] {4, 5, 6};
    printArray(a);
    printArray(b);
    a[0] = 7;
    a[1] = b[0] * 2;
    a[2] *= 3;
    printArray(a);
    printArray(b);
    b = a;
    printArray(a);
    printArray(b);
  }

  public static void printArray(byte[] a) {
    for (int i = 0; i <= 2; i++) {
      System.out.println(a[i]);
    }
  }
}
