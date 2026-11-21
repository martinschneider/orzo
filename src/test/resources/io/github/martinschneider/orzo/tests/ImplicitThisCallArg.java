package io.github.martinschneider.orzo.tests;

public class ImplicitThisCallArg {
  private String prefix = "p";

  String path(String name) {
    return prefix + "/" + name;
  }

  void run(Object output) {
    boolean flag = output != null && prefix.length() > 0;
    if (flag) {
      System.out.println("flag");
    }
    if (output == null) {
      // instance call with an implicit "this" receiver as a constructor argument
      StringBuilder sb = new StringBuilder(path("x"));
      System.out.println(sb.length());
    }
  }

  public static void main(String[] args) {
    new ImplicitThisCallArg().run(null);
    new ImplicitThisCallArg().run("o");
  }
}
