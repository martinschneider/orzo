package io.github.martinschneider.orzo.tests;

public class ArrayElementDeclaration {
  public static void main(String[] args) {
    String[] words = new String[] {"x", "y"};
    String first = words[0];
    System.out.println(first);
    StringBuilder[] builders = new StringBuilder[] {new StringBuilder("q")};
    StringBuilder b = builders[0];
    System.out.println(b.length());
    long[] longs = new long[] {7L};
    long l = longs[0];
    System.out.println(l + 1);
  }
}
