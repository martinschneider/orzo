package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class BoxedForEach {
  public static void main(String[] args) {
    List<Integer> list = new ArrayList<>();
    list.add(4);
    list.add(5);
    StringBuilder sb = new StringBuilder();
    for (Integer n : list) {
      try {
        sb.append(n).append(";");
      } catch (Exception e) {
      }
    }
    System.out.println(sb.toString());
  }
}
