package io.github.martinschneider.kommpeiler.scanner.tokens;

import io.github.martinschneider.kommpeiler.parser.productions.Selector;

public class Identifier extends Token {
  private Selector selector;

  public Identifier(final String value) {
    super(value);
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder(getValue().toString());
    if (selector != null) {
      strBuilder.append('.');
      strBuilder.append(selector.toString());
    }
    return strBuilder.toString();
  }

  public void setSelector(final Selector selector) {
    this.selector = selector;
  }

  public Selector getSelector() {
    return selector;
  }

  public String id() {
    return getValue().toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((selector == null) ? 0 : selector.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Identifier other = (Identifier) obj;
    if (selector == null) {
      if (other.selector != null) {
        return false;
      }
    } else if (!selector.equals(other.selector)) {
      return false;
    }
    return true;
  }
}
