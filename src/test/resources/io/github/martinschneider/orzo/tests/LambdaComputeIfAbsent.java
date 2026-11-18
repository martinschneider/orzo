package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LambdaComputeIfAbsent {
  public static void main(String[] args) {
    Map<Integer, List<Integer>> map = new HashMap<>();
    List<Integer> list = map.computeIfAbsent(1, k -> new ArrayList<>());
    list.add(42);
    System.out.println(map.get(1));
  }
}
