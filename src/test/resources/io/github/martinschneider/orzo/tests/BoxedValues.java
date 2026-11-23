package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoxedValues {
  // (not a constant: no ConstantValue attribute)
  final int computed = twice(21);
  static final int MIN = -2147483648;
  static final int ALL_BITS = 0xFFFFFFFF;

  static int twice(int x) {
    return 2 * x;
  }

  // increments of boxed variables, literals of wrapper types, bound method references
  public static void main(String[] args) {
    Integer v = 2;
    v++;
    ++v;
    int a = v++;
    int b = ++v;
    Integer w = (v > 5) ? v-- : v++;
    Long l = 5L;
    l--;
    Double d = 1.5;
    d++;
    Float f = 2.5f;
    System.out.println(v + " " + a + " " + b + " " + w + " " + l + " " + d + " " + f);
    List<String> list = new ArrayList<>();
    Consumer<String> add = list::add;
    add.accept("x");
    Supplier<Integer> size = list::size;
    System.out.println(list + " " + size.get() + " " + new BoxedValues().computed);
    System.out.println(MIN + " " + ALL_BITS);
    // (numeric branches of different types are promoted)
    int x = 5;
    double promoted = x > 4 ? 9 : 9.9;
    System.out.println("is " + (x > 4 ? 9 : 9.9) + " " + (x < 4 ? 1 : 2L) + " " + promoted);
  }
}
