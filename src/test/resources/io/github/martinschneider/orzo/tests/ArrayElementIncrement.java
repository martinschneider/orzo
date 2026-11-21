package io.github.martinschneider.orzo.tests;

public class ArrayElementIncrement {
  static int calls;
  static int[] counts = new int[3];
  long[] wide = new long[2];
  byte[] small = {127, 0};
  char[] chars = {'a', 'b'};
  double[] ds = {1.5};

  static int next() {
    return calls++;
  }

  public static void main(String[] args) {
    counts[next()]++;
    int v = counts[next()]++;
    int w = ++counts[next()];
    ArrayElementIncrement r = new ArrayElementIncrement();
    long l = r.wide[1]++;
    long m = --r.wide[1];
    r.small[0]++;
    char c = r.chars[0]++;
    double d = ++r.ds[0];
    int i = 0;
    int[] a = {5, 6};
    a[i++]++;
    System.out.println(calls + " " + counts[0] + counts[1] + counts[2] + " " + v + w + " " + l + m + " " + r.small[0] + " " + c + r.chars[0] + " " + d + " " + a[0] + a[1] + i);
  }
}
