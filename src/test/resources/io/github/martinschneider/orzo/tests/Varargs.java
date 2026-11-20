package io.github.martinschneider.orzo.tests;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

public class Varargs {
  static int sum(int... xs) {
    int total = 0;
    for (int x : xs) {
      total += x;
    }
    return total;
  }

  static String join(String sep, String... parts) {
    StringBuilder sb = new StringBuilder();
    for (String p : parts) {
      sb.append(p).append(sep);
    }
    return sb.toString();
  }

  public static void main(String[] args) {
    System.out.println(String.format("%s-%s", "a", "b"));
    System.out.println(Arrays.asList("a", "b", "c").size());
    List<String> l = asList("x", "y");
    System.out.println(l.size());
    System.out.println(sum(1, 2, 3));
    System.out.println(sum());
    System.out.println(sum(new int[] {4, 5}));
    System.out.println(join(",", "p", "q"));
    VarargsHolder h = new VarargsHolder("n1", "n2", "n3");
    System.out.println(h.count);
  }
}
