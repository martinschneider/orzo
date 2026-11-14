package io.github.martinschneider.orzo.tests;

public class FullyQualifiedTypeDeclaration {
  public static void main(String[] args) {
    java.lang.String s = "hello";
    System.out.println(s);
    java.lang.Object o = s;
    System.out.println(o.toString());
  }
}
