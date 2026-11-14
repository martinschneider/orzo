package io.github.martinschneider.orzo.tests;

public class ReferenceArrayLiteral {
  public static void main(String[] args) {
    String[] names = new String[] {"a", "b", "c"};
    for (String name : names) {
      System.out.println(name);
    }
    names[1] = "z";
    System.out.println(names[1]);
  }
}
