package io.github.martinschneider.orzo.tests;

public class TryCatchLocals {
  public static void main(String[] args) {
    try {
      byte[] data = new byte[3];
      int len = data.length;
      System.out.println(len);
      return;
    } catch (Exception e) {
      System.out.println(-1);
    }
  }
}
