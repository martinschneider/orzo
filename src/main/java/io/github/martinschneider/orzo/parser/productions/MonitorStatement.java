package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.util.ObjectUtils;

/** Enters or exits the monitor of a local variable's object (see SynchronizedParser). */
public class MonitorStatement implements Statement {
  public String var;
  public boolean enter;

  public MonitorStatement(String var, boolean enter) {
    this.var = var;
    this.enter = enter;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.hashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return ObjectUtils.equals(this, obj);
  }

  @Override
  public String toString() {
    return ObjectUtils.toString(this);
  }
}
