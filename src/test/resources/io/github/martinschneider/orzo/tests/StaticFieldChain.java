package io.github.martinschneider.orzo.tests;

import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import java.util.concurrent.TimeUnit;

public class StaticFieldChain {
  public static void main(String[] args) {
    // a static field of an imported class (not compiled in this run), then an instance field
    System.out.println(java.awt.Point.class.getSimpleName().length());
    System.out.println(TimeUnit.SECONDS.name().length());
    int max = Integer.MAX_VALUE;
    System.out.println(max > 0);
    System.out.println(java.lang.Boolean.TRUE.booleanValue());
    int flag = AccessFlag.ACC_STATIC.val;
    System.out.println(flag);
    System.out.println(io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_PUBLIC.val);
  }
}
