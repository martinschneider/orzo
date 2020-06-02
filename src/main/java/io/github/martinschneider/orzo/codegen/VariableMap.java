package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.Map;

public class VariableMap {
  private Map<String, VariableInfo> variables;
  private int size;

  public VariableMap(Map<String, VariableInfo> variables) {
    super();
    this.variables = variables;
  }

  public Map<String, VariableInfo> getVariables() {
    return variables;
  }

  public void put(Identifier id, VariableInfo var) {
    variables.put(id.getValue().toString(), var);
    size++;
    // long and double take up two entries
    if (var.getType().equals(DOUBLE) || var.getType().equals(LONG)) {
      size++;
    }
  }

  public VariableInfo get(Token id) {
    return variables.get(id.getValue().toString());
  }

  public int size() {
    return size;
  }

  public boolean containsKey(Identifier id) {
    return variables.containsKey(id.getValue().toString());
  }
}
