package io.github.martinschneider.orzo.tests;

public class IntArrays {
  public static void main(String[] args) {
    int[] a = new int[] {1, 2, 3};
    int[] b = new int[3];
    b[0] = 4;
    b[1] = 5;
    b[2] = 6;
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

  public static void printArray(int[] a) {
    for (int i = 0; i <= 2; i++) {
      System.out.println(a[i]);
    }
  }
}
