package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericChainCalls {
  private List<StringBuilder> builders = new ArrayList<>();

  public static void main(String[] args) {
    List<String> words = new ArrayList<>();
    words.add("hello");
    words.add("orzo");
    System.out.println(words.get(1).length());
    System.out.println(words.get(0).toUpperCase().length());
    System.out.println(3 + words.get(0).length());
    Map<String, List<String>> groups = new HashMap<>();
    groups.put("w", words);
    System.out.println(groups.get("w").size());
    System.out.println(groups.get("w").get(1).charAt(0));
    System.out.println(firstLength(words));
    GenericChainCalls g = new GenericChainCalls();
    g.run();
    GenericChainHolder holder = new GenericChainHolder();
    holder.items.add("abc");
    System.out.println(holder.items.get(0).length());
  }

  static int firstLength(List<String> list) {
    return list.get(0).length();
  }

  void run() {
    builders.add(new StringBuilder("x"));
    builders.get(0).append("yz");
    System.out.println(builders.get(0).length());
  }
}
