package io.github.martinschneider.orzo.tests;

public class IfElseConditions {
  public static void main(String[] args) {
    int a = 2;
    if (a >= 1) {
      System.out.println(">=1");
    } else if (a >= 2) {
      System.out.println(">=2");
    }
    if (a == 0) {
      System.out.println("=0");
    } else if (a == 1) {
      System.out.println("=1");
    } else {
      System.out.println(">=2");
    }
  }
}
