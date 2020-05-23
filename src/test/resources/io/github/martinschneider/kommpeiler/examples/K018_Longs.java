package io.github.martinschneider.kommpeiler.examples;

public class K018_Longs {
  public static void main(String[] args) {
    long a = 2147483647;
    a++;
    System.out.println(a);
    long b = -2147483648;
    b--;
    System.out.println(b);
    long c = a + b;
    System.out.println(c);
  }
}
