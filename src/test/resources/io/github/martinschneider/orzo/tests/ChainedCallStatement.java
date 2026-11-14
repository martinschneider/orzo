package io.github.martinschneider.orzo.tests;

public class ChainedCallStatement {
  public static void main(String[] args) {
    StringBuilder sb = new StringBuilder();
    sb.append("a").append('=').append("b");
    System.out.println(sb.toString());
    if (true) {
      sb.append("c").append("d");
    }
    System.out.println(sb.toString());
  }
}
