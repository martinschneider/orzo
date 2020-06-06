package io.github.martinschneider.orzo.tests;

public class DoubleArrays {
  public static void main(String[] args) {
    double[] a = new double[] {1.0, 2.0, 3.0};
    double[] b = new double[] {4.0, 5.0, 6.0};
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

  public static void printArray(double[] a) {
    for (int i = 0; i <= 2; i++) {
      System.out.println(a[i]);
    }
  }
}
