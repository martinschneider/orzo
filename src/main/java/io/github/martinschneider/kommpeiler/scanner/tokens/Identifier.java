package io.github.martinschneider.kommpeiler.scanner.tokens;

import io.github.martinschneider.kommpeiler.parser.productions.ArraySelector;

public class Identifier extends Token {
  private ArraySelector selector;

  public Identifier(String value) {
    super(value);
  }

  public Identifier(String value, ArraySelector selector) {
    super(value);
    this.selector = selector;
  }

  public Identifier wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder(getValue().toString());
    if (selector != null) {
      strBuilder.append(selector.toString());
    }
    return strBuilder.toString();
  }

  public void setSelector(final ArraySelector selector) {
    this.selector = selector;
  }

  public ArraySelector getSelector() {
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
