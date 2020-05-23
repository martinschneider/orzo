package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.DOUBLE;
import static io.github.martinschneider.kommpeiler.scanner.tokens.Type.LONG;

import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class VariableMap {
  private Map<Identifier, VariableInfo> variables;
  private int size;

  public VariableMap(Map<Identifier, VariableInfo> variables) {
    super();
    this.variables = variables;
  }

  public Map<Identifier, VariableInfo> getVariables() {
    return variables;
  }

  public void put(Identifier id, VariableInfo var) {
    variables.put(id, var);
    size++;
    // long and double take up two entries
    if (var.getType().equals(DOUBLE) || var.getType().equals(LONG)) {
      size++;
    }
  }

  public VariableInfo get(Object id) {
    return variables.get(id);
  }

  public int size() {
    return size;
  }
}
