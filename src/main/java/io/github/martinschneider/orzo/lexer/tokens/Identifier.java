package io.github.martinschneider.orzo.lexer.tokens;

import io.github.martinschneider.orzo.parser.productions.ArraySelector;

public class Identifier extends Token {
  public ArraySelector arrSel;
  public Identifier next;

  public Identifier(String val) {
    super(val);
  }

  public Identifier(String val, ArraySelector arrSel) {
    super(val);
    this.arrSel = arrSel;
  }

  public Identifier wLoc(Location loc) {
    this.loc = loc;
    return this;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder();
    Identifier id = this;
    while (id != null) {
      strBuilder.append(id.val);
      if (arrSel != null) {
        strBuilder.append(arrSel.toString());
      }
      id = id.next;
      if (id != null) {
        strBuilder.append('.');
      }
    }
    return strBuilder.toString();
  }

  public String id() {
    return val.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((arrSel == null) ? 0 : arrSel.hashCode());
    result = prime * result + ((next == null) ? 0 : next.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;
    Identifier other = (Identifier) obj;
    if (arrSel == null) {
      if (other.arrSel != null) return false;
    } else if (!arrSel.equals(other.arrSel)) return false;
    if (next == null) {
      if (other.next != null) return false;
    } else if (!next.equals(other.next)) return false;
    return true;
  }
}
