package io.github.martinschneider.orzo.tests;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NestedConstructorArg {
  public static void main(String[] args) {
    PrintStream ps = new PrintStream(new ByteArrayOutputStream());
    ps.println("hi");
    System.out.println("done");
  }
}
