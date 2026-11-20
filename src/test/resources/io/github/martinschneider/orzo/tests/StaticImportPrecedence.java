package io.github.martinschneider.orzo.tests;

import static java.lang.Math.*;

public class StaticImportPrecedence {
  public static void main(String[] args) {
    // PI is also an enum constant of the (same-run) StaticImportPrecedenceEnum, which must not
    // shadow the statically imported Math.PI
    double d = PI;
    StaticImportPrecedenceEnum e = StaticImportPrecedenceEnum.PI;
    System.out.println(e == StaticImportPrecedenceEnum.PI);
    System.out.println(d > 3.14 && d < 3.15);
  }
}
