package io.github.martinschneider.kommpeiler.scanner.tokens;

public class Comparator extends Sym {
  public Comparator(final Comparators value) {
    super(value);
  }

  public Comparators cmpValue() {
    return (Comparators) getValue();
  }
}
