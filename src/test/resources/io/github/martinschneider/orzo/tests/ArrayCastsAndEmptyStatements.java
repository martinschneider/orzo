package io.github.martinschneider.orzo.tests;

public class ArrayCastsAndEmptyStatements {
  // casts to array types, empty statements
  public static void main(String[] args) {
    Object o = new int[] {1, 2, 3};
    int[] a = (int[]) o;
    Object s = new String[] {"x", "y"};
    String[] strings = (String[]) s;
    System.out.println(a.length + " " + a[1] + " " + strings[1] + " " + ((int[]) o)[2]);
    int x = 1;
    ;
    try {
      x++;
    } catch (RuntimeException e) {
    }
    ;
    for (int i = 0; i < 2; i++)
      ;
    System.out.println(x);
  }
}
