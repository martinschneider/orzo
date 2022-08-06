package io.github.martinschneider.orzo.codegen.identifier;

import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentifierMap {
  // long and double take up two entries
  private static final List<String> DOUBLE_SIZE = List.of(DOUBLE, LONG);
  public Map<String, VariableInfo> localMap = new HashMap<>();
  public Map<String, VariableInfo> fieldMap = new HashMap<>();
  public int localSize;
  public int fieldSize;
  public int tmpCount;

  public void putField(Identifier id, VariableInfo var) {
    fieldMap.put(id.val.toString(), var);
    fieldSize++;
    if (DOUBLE_SIZE.contains(var.type)) {
      fieldSize++;
    }
  }

  public void putLocal(Identifier id, VariableInfo var) {
    localMap.put(id.val.toString(), var);
    localSize++;
    if (DOUBLE_SIZE.contains(var.type)) {
      localSize++;
    }
  }

  public VariableInfo get(Token id) {
    VariableInfo ret = fieldMap.get(id.val.toString());
    if (ret == null) {
      ret = localMap.get(id.val.toString());
      // TODO: error handling
    }
    return ret;
  }

  public boolean containsKey(Identifier id) {
    return fieldMap.containsKey(id.val.toString()) || localMap.containsKey(id.val.toString());
  }
}
