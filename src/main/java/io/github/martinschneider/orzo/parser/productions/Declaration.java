package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;

public class Declaration implements Statement {
  public Identifier name;
  public int arrDim;
  public String type;
  public Expression val;

  public Declaration(String type, int arrDim, Identifier name, Expression val) {
    this.type = type;
    this.arrDim = arrDim;
    this.name = name;
    this.val = val;
  }

  public Declaration(String type, Identifier name, Expression val) {
    this(type, 0, name, val);
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder(type.toString());
    for (int i = 0; i < arrDim; i++) {
      strBuilder.append("[]");
    }
    strBuilder.append(' ');
    strBuilder.append(name);
    strBuilder.append('=');
    strBuilder.append(val);
    return strBuilder.toString();
  }

  @Override
  public int hashCode() {
    int prime = 31;
    int result = 1;
    result = prime * result + arrDim;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((val == null) ? 0 : val.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Declaration other = (Declaration) obj;
    if (arrDim != other.arrDim) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (type == null) {
      if (other.type != null) {
        return false;
      }
    } else if (!type.equals(other.type)) {
      return false;
    }
    if (val == null) {
      if (other.val != null) {
        return false;
      }
    } else if (!val.equals(other.val)) {
      return false;
    }
    return true;
  }
}
