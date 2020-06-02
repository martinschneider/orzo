package io.github.martinschneider.orzo.parser.productions;

public class Break implements Statement {
  @Override
  public String toString() {
    return "break";
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Break;
  }
}
