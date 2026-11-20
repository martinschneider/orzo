package io.github.martinschneider.orzo.tests;

public class ForEachArrayFieldInIf {
  int[] arr = new int[] {1, 2};
  boolean flag = true;

  void run() {
    if (flag) {
      for (int x : arr) {
        System.out.println(x);
      }
    }
  }


  public static void main(String[] args) {
    new ForEachArrayFieldInIf().run();
  }
}
