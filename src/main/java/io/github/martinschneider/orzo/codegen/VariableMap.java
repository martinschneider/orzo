package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.HashMap;
import java.util.Map;

public class VariableMap {
  private Map<String, VariableInfo> variables;
  public int size;
  public int tmpCount;

  public VariableMap(Map<String, VariableInfo> variables) {
    this.variables = variables;
  }

  public VariableMap(VariableMap globalFields) {
    this.variables = new HashMap<>(globalFields.variables);
    this.size = globalFields.size;
  }

  public Map<String, VariableInfo> getVariables() {
    return variables;
  }

  public void put(Identifier id, VariableInfo var) {
    variables.put(id.val.toString(), var);
    size++;
    // long and double take up two entries
    if (var.type.equals(DOUBLE) || var.type.equals(LONG)) {
      size++;
    }
  }

  public VariableInfo get(Token id) {
    VariableInfo ret = variables.get(id.val.toString());
    if (ret == null) {
      // TODO: error handling
    }
    return variables.get(id.val.toString());
  }

  public boolean containsKey(Identifier id) {
    return variables.containsKey(id.val.toString());
  }
}
