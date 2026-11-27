package io.github.martinschneider.orzo.tests;

public class SyntaxCorners {
  // C-style array declarators, empty array initializers and enums, Unicode escapes, calls on enum
  // constants, nested JDK classes named with dots
  static int f[] = {7, 8}, g = 3;

  enum None {
    ,
  }

  enum Color {
    RED,
    GREEN,
    ;

    String label() {
      return name().toLowerCase();
    }
  }

  public static void main(String args[]) {
    int a[] = {1, 2, 3}, b = 4;
    String s[] = new String[1];
    s[0] = "x";
    System.out.println(a[2] + b + s[0] + f[1] + g);
    int[] empty = {,};
    int[] trailing = new int[] {1, 2,};
    System.out.println(empty.length + " " + trailing.length + " " + None.values().length);
    String abc = "Aé";
    System.out.println(abc + " " + "\\u0041".length());
    String n = Color.GREEN.name() + Color.RED.label() + Color.GREEN.ordinal();
    System.out.println(n);
    System.out.println(Thread.State.NEW.name() + " " + java.util.Map.Entry.class.getSimpleName());
  }
}
