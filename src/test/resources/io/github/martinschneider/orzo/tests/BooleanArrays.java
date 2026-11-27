package io.github.martinschneider.orzo.tests;

public class BooleanArrays {
  // boolean arrays: created with newarray T_BOOLEAN, accessed with baload/bastore
  static boolean[] flags = new boolean[3];

  public static void main(String[] args) {
    boolean[] r = new boolean[2];
    r[1] = true;
    boolean[] q = {true, false};
    flags[2] = r[1] && q[0];
    System.out.println(r[0] + " " + r[1] + " " + q.length + " " + flags[2]);
    for (boolean b : q) {
      System.out.println(!b);
    }
  }
}
