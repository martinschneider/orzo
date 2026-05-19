package io.github.martinschneider.orzo.tests;

public class Unless {
  public static void main(String[] args) {
    int x = 5;
    unless (x == 0) {
      System.out.println(x);
    }
    unless (x > 0) {
      System.out.println(-1);
    }
    boolean flag = false;
    unless (flag) {
      System.out.println(1);
    }
  }
}
