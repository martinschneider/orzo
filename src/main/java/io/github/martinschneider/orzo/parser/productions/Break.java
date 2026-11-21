package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;

public class Break implements Statement {
  // the target loop's label, e.g. "outer" in "break outer;" (null if none)
  public String label;

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }

  @Override
  public boolean equals(Object obj) {
    return ObjectUtils.equals(this, obj);
  }
}
