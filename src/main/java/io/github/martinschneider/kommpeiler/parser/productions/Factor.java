package io.github.martinschneider.kommpeiler.parser.productions;

/**
 * Factor
 *
 * @author Martin Schneider
 */
public interface Factor {

  /** @return value */
  Object getValue();

  /** @return value type */
  ValueType getValueType();

  /** @param valueType valueType */
  void setValueType(ValueType valueType);
}
