package io.github.martinschneider.orzo.tests;

import io.github.martinschneider.orzo.util.ObjectUtils;

public class ObjectUtilsUsagePoint {
  public int x;
  public String label;

  public ObjectUtilsUsagePoint(int x, String label) {
    this.x = x;
    this.label = label;
  }

  public int hashCode() {
    return ObjectUtils.hashCode(this);
  }

  public boolean equals(Object o) {
    return ObjectUtils.equals(this, o);
  }

  public String toString() {
    return ObjectUtils.toString(this);
  }
}
