package io.github.martinschneider.kommpeiler.parser.productions;

public enum BasicType {
  INT("I"),
  DOUBLE("D"),
  SHORT("S"),
  BYTE("B"),
  LONG("L"),
  VOID("V"),
  STRING("java.lang.String");
  private String label;

  private BasicType(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
