package io.github.martinschneider.orzo.tests;

public class TernaryArgument {
  public static void main(String[] args) {
    String[] empty = new String[0];
    boolean b = empty.length == 0;
    System.out.println(empty.length == 0);
    StringBuilder sb = new StringBuilder();
    sb.append('<').append(b ? "yes" : "no").append('>');
    System.out.println(sb.toString());
    System.out.println(String.valueOf(b ? 1.5 : 2));
    System.out.println(new StringBuilder(b ? "s" : "t").length());
  }
}
