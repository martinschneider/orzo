package io.github.martinschneider.orzo.tests;

import java.util.ArrayList;
import java.util.List;

public class InstanceInitializers {
  // instance initializer blocks run with the field initializers, in source order
  List<String> log = new ArrayList<>();

  {
    log.add("block 1");
  }

  int x = log.size();

  {
    log.add("block 2 (x = " + x + ")");
  }

  InstanceInitializers() {
    log.add("constructor");
  }

  InstanceInitializers(String name) {
    this();
    log.add(name);
  }

  public static void main(String[] args) {
    System.out.println(new InstanceInitializers().log);
    System.out.println(new InstanceInitializers("named").log);
  }
}
