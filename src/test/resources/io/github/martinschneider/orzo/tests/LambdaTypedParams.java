package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LambdaTypedParams {
  public static void main(String[] args) {
    List<StringBuilder> builders = new ArrayList<>();
    builders.add(new StringBuilder("ab"));
    builders.add(new StringBuilder("cde"));
    // the lambda parameter is a StringBuilder (from Stream<StringBuilder>.map), not an Object
    System.out.println(builders.stream().map(x -> x.length()).collect(Collectors.toList()));
    System.out.println(
        builders.stream().map(x -> x.toString().toUpperCase()).collect(Collectors.joining(",")));
    List<GenericChainHolder> holders = new ArrayList<>();
    GenericChainHolder h = new GenericChainHolder();
    h.items.add("q");
    holders.add(h);
    // field access on the lambda parameter
    System.out.println(holders.stream().map(e -> e.items.size()).collect(Collectors.toList()));
  }
}
