package io.github.martinschneider.orzo.tests;

public class FloorOperator {
  public static void main(String[] args) {
    // "⌊x⌋" is Math.round(Math.floor(x))
    double x = 2.7;
    long floor = ⌊x⌋;
    System.out.println(floor);
    System.out.println(⌊-x⌋ + ⌊x * 2⌋);
  }
}
