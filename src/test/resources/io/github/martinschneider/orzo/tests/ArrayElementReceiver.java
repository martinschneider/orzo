package io.github.martinschneider.orzo.tests;

import java.util.List;

public class ArrayElementReceiver {
  public static void main(String[] args) {
    String[] words = new String[] {"ab", "cde"};
    int i = 1;
    System.out.println(words[0].length());
    System.out.println(words[i].length() + 1);
    if (!words[0].equals("x")) {
      System.out.println(words[i].toUpperCase());
    }
    StringBuilder[] builders = new StringBuilder[] {new StringBuilder("q")};
    builders[0].append("r");
    builders[0].append("s").append("t");
    System.out.println(builders[0].toString());
    StringBuilder[] empty = new StringBuilder[3];
    System.out.println(empty.length);
    List<?>[] lists = new java.util.List<?>[2];
    System.out.println(lists.length);
  }
}
