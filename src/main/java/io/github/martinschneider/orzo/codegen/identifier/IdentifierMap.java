package io.github.martinschneider.orzo.codegen.identifier;

import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.LONG;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Token;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IdentifierMap {
  // long and double take up two entries
  private static final List<String> DOUBLE_SIZE = List.of(DOUBLE, LONG);
  public Map<String, VariableInfo> localMap = new HashMap<>();
  public Map<String, VariableInfo> fieldMap = new HashMap<>();
  public Set<String> inheritedFieldNames = new HashSet<>();
  // Every local ever declared anywhere in the current method, by slot index, regardless of
  // block scope — never cleared when a nested block's scope is restored (unlike localMap,
  // which IS scope-restored so a block-local doesn't leak into resolution of code outside its
  // block). StackMapTableBuilder needs this: it runs once, after the whole method's bytecode is
  // generated, and determines each local slot's declared type for building frames — it has no
  // other way to find out that slot N held, say, an int for *part* of the method if that local's
  // scope has already been restored away from localMap by then.
  public Map<Integer, VariableInfo> allLocalsBySlot = new HashMap<>();
  public int localSize;
  // High-water mark for localSize across the whole method: a nested block (e.g. an if-block,
  // via IfGenerator's scope restore) can shrink localSize back down after its body finishes, to
  // let a later sibling block reuse the same slot indices — but the method's max_locals must
  // stay at the PEAK slot count ever reached, or an instruction that legitimately used a slot
  // while it was live ends up referencing an index beyond what max_locals declared.
  public int localSizeMax;
  public int fieldSize;
  public int tmpCount;

  public void putField(Identifier id, VariableInfo var) {
    fieldMap.put(id.val.toString(), var);
    fieldSize++;
    if (DOUBLE_SIZE.contains(var.type)) {
      fieldSize++;
    }
  }

  public void putInheritedField(String name, VariableInfo var) {
    fieldMap.put(name, var);
    inheritedFieldNames.add(name);
  }

  public void putLocal(Identifier id, VariableInfo var) {
    localMap.put(id.val.toString(), var);
    allLocalsBySlot.put((int) var.idx, var);
    localSize++;
    if (DOUBLE_SIZE.contains(var.type)) {
      localSize++;
    }
    localSizeMax = Math.max(localSizeMax, localSize);
  }

  public VariableInfo get(Token id) {
    VariableInfo ret = localMap.get(id.val.toString());
    if (ret == null) {
      ret = fieldMap.get(id.val.toString());
      // TODO: error handling
    }
    return ret;
  }

  public boolean containsKey(Identifier id) {
    return fieldMap.containsKey(id.val.toString()) || localMap.containsKey(id.val.toString());
  }
}
