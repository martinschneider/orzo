package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class FieldOfCallAssignment {
  public int count;
  public String name;

  public static void main(String[] args) {
    List<FieldOfCallAssignment> list = new ArrayList<>();
    list.add(new FieldOfCallAssignment());
    list.get(0).count = 5;
    list.get(0).name = "n" + list.get(0).count;
    System.out.println(list.get(0).count);
    System.out.println(list.get(0).name);
    for (FieldOfCallAssignment f : list) System.out.println(f.count + 1);
    Object o = list.get(0);
    ((FieldOfCallAssignment) o).count = 9;
    System.out.println(list.get(0).count);
  }
}
