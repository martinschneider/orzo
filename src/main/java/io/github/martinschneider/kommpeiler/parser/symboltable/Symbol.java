package io.github.martinschneider.kommpeiler.parser.symboltable;

import io.github.martinschneider.kommpeiler.parser.productions.IdFactor;
import io.github.martinschneider.kommpeiler.parser.productions.IntFactor;
import io.github.martinschneider.kommpeiler.scanner.tokens.SymbolType;

/**
 * Symbol for Symbol Table
 *
 * @author Martin Schneider
 */
public class Symbol {
  private String name;
  private SymbolClass clazz;
  private SymbolType type;
  private Object value;
  private boolean hasValue;
  private int adress;

  /**
   * @param name symbol name
   * @param clazz symbol class
   * @param type symbol type
   * @param adress memory adress
   * @param value symbol value
   * @param hasValue true if value is known
   */
  public Symbol(
      final String name,
      final SymbolClass clazz,
      final SymbolType type,
      final int adress,
      final Object value,
      final boolean hasValue) {
    super();
    this.name = name;
    this.clazz = clazz;
    this.type = type;
    this.adress = adress;
    this.value = value;
    this.setHasValue(hasValue);
  }

  /**
   * @param name symbol name
   * @param clazz symbol class
   * @param type symbol type
   */
  public Symbol(final String name, final SymbolClass clazz, final SymbolType type) {
    super();
    this.name = name;
    this.clazz = clazz;
    this.type = type;
    value = null;
    setHasValue(false);
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public SymbolClass getClazz() {
    return clazz;
  }

  public void setClazz(final SymbolClass clazz) {
    this.clazz = clazz;
  }

  public SymbolType getType() {
    return type;
  }

  public void setType(final SymbolType type) {
    this.type = type;
  }

  // FIXME: better impl
  /** @return value */
  public Object getValue() {
    if (value instanceof IntFactor) {
      return ((IntFactor) value).getValue();
    } else if (value instanceof IdFactor) {
      return ((IdFactor) value).getValue();
    } else {
      return value;
    }
  }

  public void setValue(final Object value) {
    this.value = value;
  }

  public void setHasValue(final boolean hasValue) {
    this.hasValue = hasValue;
  }

  /** @return true if value is known */
  public boolean hasValue() {
    return hasValue;
  }

  public void setAdress(final int adress) {
    this.adress = adress;
  }

  public int getAdress() {
    return adress;
  }
}
