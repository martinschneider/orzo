package io.github.martinschneider.orzo.tests;

public class LargeIntInArrayInit {
  public static void main(String[] args) {
    int x = 70000;
    int[] values = new int[] {x & 0xFFFF, 100000, -40000};
    System.out.println(values[0] + " " + values[1] + " " + values[2]);
  }
}
