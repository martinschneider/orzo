package io.github.martinschneider.kommpeiler.parser.productions;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

public class Declaration implements Statement {
  private Identifier name;
  private int array; // array dimension
  private String type;
  private Expression value;

  public Declaration(
      final String type,
      final int array,
      final Identifier name,
      final Expression value,
      final boolean hasValue) {
    super();
    this.type = type;
    this.array = array;
    this.name = name;
    this.value = value;
  }

  public Declaration(
      final String type, final Identifier name, final Expression value, final boolean hasValue) {
    this(type, 0, name, value, hasValue);
  }

  public Identifier getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Expression getValue() {
    return value;
  }

  public void setName(final Identifier name) {
    this.name = name;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public void setValue(final Expression value) {
    this.value = value;
  }

  public int getArray() {
    return array;
  }

  public void setArray(int array) {
    this.array = array;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder(type.toString());
    for (int i = 0; i < array; i++) {
      strBuilder.append("[]");
    }
    strBuilder.append(' ');
    strBuilder.append(name);
    strBuilder.append('=');
    strBuilder.append(value);
    return strBuilder.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + array;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    if (array != other.array) {
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
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }
}
