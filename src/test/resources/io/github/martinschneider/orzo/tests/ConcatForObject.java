package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class ConcatForObject {
  static final String U = "u";
  String inst = "i";

  public static void main(String[] args) {
    int pos = 7;
    String w = "w";
    String c = "k";
    List<Object> l = new ArrayList<>();
    // a string concatenation passed/assigned as an Object
    l.add(w + pos);
    l.add(U + pos + ":" + (c != null ? c : "x"));
    Object o = U + pos;
    ConcatForObject r = new ConcatForObject();
    l.add(r.inst + pos);
    System.out.println(l.get(0) + " " + l.get(1) + " " + o + " " + l.get(2));
  }
}
