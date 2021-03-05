package io.github.martinschneider.orzo.codegen;

import io.github.martinschneider.orzo.codegen.constants.ConstantPool;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockConstantPool extends ConstantPool {
  private Map<Object, Short> lookup = new HashMap<>();

  public MockConstantPool(CGContext ctx, List<Constant> constants) {
    super(ctx);
    for (Constant constant : constants) {
      lookup.put(constant.key, constant.id);
    }
  }

  public short indexOf(byte entryType, String classKey, String key, String type) {
    return indexOf((byte) 0, key);
  }

  public short indexOf(byte type, Object key) {
    short id = lookup.getOrDefault(key, Short.valueOf((short) -1));
    if (id == -1) {
      ctx.errors.addError(
          "mock constant pool", String.format("constant for key %s undefined", key));
    }
    return id;
  }

  public static Constant constant(Object key, int id) {
    Constant constant = new Constant();
    constant.id = (short) id;
    constant.key = key;
    return constant;
  }
}
