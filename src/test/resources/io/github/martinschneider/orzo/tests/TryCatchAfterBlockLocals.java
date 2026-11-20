package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class TryCatchAfterBlockLocals {
  List<String> candidates = new ArrayList<>();
  String fallback = "p";

  String resolve(String id) {
    if ("zero".equals(id)) {
      return "0";
    }
    // a block-local loop (iterator + element locals) followed by a try/catch whose
    // exception variable must not reuse (or be typed as) the loop's slots
    if (candidates != null) {
      for (String c : candidates) {
        if (c.startsWith(id)) {
          String num = c.substring(id.length());
          try {
            Integer.parseInt(num);
            return num;
          } catch (NumberFormatException e) {
            // ignore
          }
        }
      }
    }
    try {
      Integer.parseInt(id);
      return "#" + id;
    } catch (NumberFormatException e) {
      // ignore
    }
    if (fallback != null && !fallback.isEmpty()) {
      return fallback + "." + id;
    }
    return id;
  }

  public static void main(String[] args) {
    TryCatchAfterBlockLocals r = new TryCatchAfterBlockLocals();
    r.candidates.add("a12");
    r.candidates.add("bxy");
    System.out.println(r.resolve("zero"));
    System.out.println(r.resolve("a"));
    System.out.println(r.resolve("b"));
    System.out.println(r.resolve("42"));
  }
}
