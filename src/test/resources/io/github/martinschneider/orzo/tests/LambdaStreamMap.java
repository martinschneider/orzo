package io.github.martinschneider.orzo.tests;

import java.util.List;
import java.util.stream.Collectors;

public class LambdaStreamMap {
  public static void main(String[] args) {
    List<Integer> nums = List.of(1, 2, 3);
    String joined = nums.stream().map(x -> x.toString()).collect(Collectors.joining(", "));
    System.out.println(joined);
  }
}
