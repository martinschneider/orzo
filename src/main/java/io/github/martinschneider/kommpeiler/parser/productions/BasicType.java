package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * BasicType
 *
 * @author Martin Schneider
 */
public enum BasicType {
  INT("I"),
  DOUBLE("D"),
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
