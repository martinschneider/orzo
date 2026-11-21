package io.github.martinschneider.orzo.tests;

public class LongDoubleConstants {
  static final long BIG = 5000000000L;
  static final double TAU = 6.28;
  static final int SMALL = 70000;
  public static void main(String[] args) throws Exception {
    long x = 123456789012L;
    System.out.println(BIG + " " + TAU + " " + SMALL + " " + x + " " + 2.5);
    System.out.println(LongDoubleConstants.class.getDeclaredField("BIG").getLong(null));
  }
}
