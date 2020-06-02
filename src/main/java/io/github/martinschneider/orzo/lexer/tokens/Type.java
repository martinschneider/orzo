package io.github.martinschneider.orzo.lexer.tokens;

import java.util.List;

public class Type extends Token {
  public static final String VOID = "void";
  public static final String INT = "int";
  public static final String LONG = "long";
  public static final String BYTE = "byte";
  public static final String SHORT = "short";
  public static final String DOUBLE = "double";
  public static final String FLOAT = "float";
  public static final String CHAR = "char";
  public static final String BOOLEAN = "boolean";
  public static final String REF = "reference";
  public static final String STRING = "String";
  public static final List<String> BASIC_TYPES =
      List.of(VOID, INT, LONG, BYTE, SHORT, DOUBLE, FLOAT, CHAR, BOOLEAN, STRING);
  private String name;
  private int array; // 0 = no array, 1 = [], 2= [][] etc.

  public Type(String name) {
    this(name, 0);
  }

  public Type(String name, int array) {
    super(name);
    this.name = name;
    this.setArray(array);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getArray() {
    return array;
  }

  public void setArray(int array) {
    this.array = array;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    Type other = (Type) obj;
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    StringBuilder strBuilder = new StringBuilder(name);
    for (int i = 0; i < array; i++) {
      strBuilder.append("[]");
    }
    return strBuilder.toString();
  }
}
