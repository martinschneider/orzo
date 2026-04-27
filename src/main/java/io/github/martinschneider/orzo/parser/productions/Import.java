package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;

public class Import {
  public String id;
  public boolean isStatic;

  public Import(String id, boolean isStatic) {
    this.id = id;
    this.isStatic = isStatic;
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
