package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class InterfaceObjectMethods {
  public static void main(String[] args) {
    // the methods of Object on a value of an interface type
    List<String> list = new ArrayList<>();
    list.add("a");
    list.add("b");
    System.out.println(list.toString());
    System.out.println(list.toString().replace('[', '('));
    List<String> other = new ArrayList<>(list);
    System.out.println(list.equals(other));
    CharSequence chars = "xyz";
    System.out.println(chars.toString().length());
  }
}
