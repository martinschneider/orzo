package io.github.martinschneider.orzo.tests;

public class FloatArrays {
  public static void main(String[] args) {
    float[] a = new float[] {1.0f, 2.0f, 3.0f};
    float[] b = new float[] {4.0f, 5.0f, 6.0f};
    printArray(a);
    printArray(b);
    a[0] = 7.0f;
    a[1] = b[0] * 2;
    a[2] *= 3;
    printArray(a);
    printArray(b);
    b = a;
    printArray(a);
    printArray(b);
  }

  public static void printArray(float[] a) {
    for (int i = 0; i <= 2; i++) {
      System.out.println(a[i]);
    }
  }
}
