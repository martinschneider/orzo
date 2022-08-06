package io.github.martinschneider.orzo.codegen.identifier;

import io.github.martinschneider.orzo.parser.productions.AccessFlag;
import io.github.martinschneider.orzo.parser.productions.Expression;
import java.util.List;

public class FieldInfo extends VariableInfo {

  public FieldInfo(String name, String type, List<AccessFlag> accFlags, short idx, Expression val) {
    super(name, type, accFlags, true, idx, val);
  }
}
