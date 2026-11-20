package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;

public class ChainedArgumentTypes {
  static String describe(String s) {
    return "string " + s;
  }

  static String describe(int i) {
    return "int " + i;
  }

  static String describe(Class<?> c) {
    return "class " + c.getSimpleName();
  }

  public static void main(String[] args) {
    Object o = String.class;
    // cast chain: must pick StringBuilder(String), not StringBuilder(int)
    StringBuilder sb = new StringBuilder(((Class<?>) o).getName());
    System.out.println(sb.toString());
    // element of an array returned by a reflected method
    Class<?> c = ArrayList.class;
    System.out.println(describe(c.getInterfaces()[0]));
    // call on a generic field of another object, followed by a further call
    GenericChainHolder holder = new GenericChainHolder();
    holder.items.add("xyz");
    System.out.println(describe(holder.items.get(0)));
    System.out.println(describe(holder.items.get(0).length()));
    // cast chain followed by a field and a call
    Object h = holder;
    System.out.println(describe(((GenericChainHolder) h).items.toString()));
  }
}
