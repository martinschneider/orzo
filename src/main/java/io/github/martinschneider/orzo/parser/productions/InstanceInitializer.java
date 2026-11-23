package io.github.martinschneider.orzo.parser.productions;

import java.util.List;

/** An instance initializer block, "{ ... }" in a class body. */
public class InstanceInitializer implements ClassMember {
  public List<Statement> body;

  public InstanceInitializer(List<Statement> body) {
    this.body = body;
  }
}
