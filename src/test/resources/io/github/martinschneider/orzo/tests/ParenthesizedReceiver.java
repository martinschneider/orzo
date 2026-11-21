package io.github.martinschneider.orzo.tests;

public class ParenthesizedReceiver {
  static String path(String pkg, String name) {
    return (pkg + "." + name).replace('.', '/');
  }

  public static void main(String[] args) {
    String a = "ab";
    String b = "cd";
    System.out.println((a + b).length());
    System.out.println(1 + (a + b).toUpperCase().length());
    System.out.println(path("a.b", "C"));
    boolean flag = a.isEmpty();
    System.out.println(((flag ? a : b) + "!").length());
    System.out.println((flag ? a : b + "x").toUpperCase());
  }
}
