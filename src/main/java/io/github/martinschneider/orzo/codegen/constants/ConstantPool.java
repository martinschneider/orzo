package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_DOUBLE;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FLOAT;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_INTERFACEMETHODREF;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_LONG;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPool {

  // https://docs.oracle.com/javase/specs/jvms/se18/html/jvms-4.html#jvms-4.7.2-300-C.1
  public static final List<String> INT_CONSTANT_TYPES =
      List.of("int", "byte", "short", "char", "boolean");

  private static final String LOGGER_NAME = "constant pool";

  protected CGContext ctx;
  private List<Constant> entries;
  private Map<String, Integer> classMap;
  private Map<Integer, String> classNameByIdx;
  private Map<Integer, Integer> integerMap;
  private Map<Double, Integer> doubleMap;
  private Map<Float, Integer> floatMap;
  private Map<Long, Integer> longMap;
  private Map<String, Integer> stringMap;
  private Map<String, Integer> utf8Map;
  private Map<String, Integer> methodRefMap;
  private Map<String, Integer> interfaceMethodRefMap;
  private Map<String, Integer> fieldRefMap;
  private Map<String, Integer> nameAndTypesMap;
  // Reverse-lookup maps: CP index → descriptor, used for stack-map simulation
  private Map<Integer, String> methodDescByIdx;
  private Map<Integer, String> interfaceMethodDescByIdx;
  private Map<Integer, String> fieldDescByIdx;
  // CP index → JVM verification type for LDC constants ("I", "J", "F", "D", "Ljava/lang/String;")
  private Map<Integer, String> ldcTypeByIdx;

  public ConstantPool(CGContext ctx) {
    this.ctx = ctx;
    entries = new ArrayList<>();
    classMap = new HashMap<>();
    classNameByIdx = new HashMap<>();
    stringMap = new HashMap<>();
    integerMap = new HashMap<>();
    doubleMap = new HashMap<>();
    longMap = new HashMap<>();
    floatMap = new HashMap<>();
    utf8Map = new HashMap<>();
    methodRefMap = new HashMap<>();
    interfaceMethodRefMap = new HashMap<>();
    fieldRefMap = new HashMap<>();
    nameAndTypesMap = new HashMap<>();
    methodDescByIdx = new HashMap<>();
    interfaceMethodDescByIdx = new HashMap<>();
    fieldDescByIdx = new HashMap<>();
    ldcTypeByIdx = new HashMap<>();
  }

  private int size;

  public byte[] getBytes() {
    DynamicByteArray array = new DynamicByteArray();
    array.write((byte) ((size + 1 >> 8) & 0xFF));
    array.write((byte) (size + 1 & 0xFF));
    for (Constant constant : entries) {
      array.write(constant.tag());
      array.write(constant.info());
    }
    return array.getBytes();
  }

  // appends an entry, returns its index (a long or double takes two slots, JVM spec 4.4.5)
  private int append(Constant entry) {
    int idx = size + 1;
    size += (entry instanceof ConstantLong || entry instanceof ConstantDouble) ? 2 : 1;
    entries.add(entry);
    return idx;
  }

  public short indexOf(byte entryType, String classKey, String key, String type) {
    String compositeKey = classKey + "_" + key + "_" + type;
    switch (entryType) {
      case CONSTANT_METHODREF:
        {
          Integer id = methodRefMap.get(compositeKey);
          if (id == null) {
            addMethodRef(classKey, key, type);
          }
          return methodRefMap.get(compositeKey).shortValue();
        }
      case CONSTANT_INTERFACEMETHODREF:
        {
          Integer id = interfaceMethodRefMap.get(compositeKey);
          if (id == null) {
            addInterfaceMethodRef(classKey, key, type);
          }
          return interfaceMethodRefMap.get(compositeKey).shortValue();
        }
      case CONSTANT_FIELDREF:
        {
          Integer id = fieldRefMap.get(compositeKey);
          if (id == null) {
            addFieldRef(classKey, key, type);
          }
          return fieldRefMap.get(compositeKey).shortValue();
        }
      default:
        ctx.errors.addError(
            LOGGER_NAME,
            String.format(
                "expected key %s of type %s not found in constant pool", compositeKey, entryType),
            new RuntimeException().getStackTrace());
        return -1;
    }
  }

  public short indexOf(byte type, Object key) {
    return indexOf(type, key, false);
  }

  public short indexOf(byte type, Object key, boolean allowMissing) {
    Map<?, Integer> map;
    Object lookupKey = key;
    switch (type) {
      case CONSTANT_CLASS:
        map = classMap;
        break;
      case CONSTANT_UTF8:
        map = utf8Map;
        break;
      case CONSTANT_STRING:
        map = stringMap;
        break;
      case CONSTANT_INTEGER:
        map = integerMap;
        if (key instanceof Number) {
          lookupKey = ((Number) key).intValue();
        }
        break;
      case CONSTANT_DOUBLE:
        map = doubleMap;
        if (key instanceof Number) {
          lookupKey = ((Number) key).doubleValue();
        }
        break;
      case CONSTANT_FLOAT:
        map = floatMap;
        if (key instanceof Number) {
          lookupKey = ((Number) key).floatValue();
        }
        break;
      case CONSTANT_LONG:
        map = longMap;
        if (key instanceof Number) {
          lookupKey = ((Number) key).longValue();
        }
        break;
      default:
        map = null;
    }
    Integer id = (map != null) ? map.get(lookupKey) : null;
    if (id != null) {
      return id.shortValue();
    }
    if (!allowMissing) {
      ctx.errors.addError(
          LOGGER_NAME,
          String.format("expected key %s of type %s not found in constant pool", key, type),
          new RuntimeException().getStackTrace());
    }
    return -1;
  }

  // Each constant is added once (the maps below are keyed by the constant's content).

  public void addClass(String clazz) {
    classIdx(clazz);
  }

  private int classIdx(String clazz) {
    Integer idx = classMap.get(clazz);
    if (idx == null) {
      idx = append(new ConstantClass((short) utf8Idx(clazz)));
      classMap.put(clazz, idx);
      classNameByIdx.put(idx, clazz);
      ldcTypeByIdx.put(idx, "Ljava/lang/Class;");
    }
    return idx;
  }

  public void addUtf8(String text) {
    utf8Idx(text);
  }

  private int utf8Idx(String text) {
    Integer idx = utf8Map.get(text);
    if (idx == null) {
      idx = append(new ConstantUtf8(text));
      utf8Map.put(text, idx);
    }
    return idx;
  }

  public void addString(String string) {
    if (!stringMap.containsKey(string)) {
      int idx = append(new ConstantString((short) utf8Idx(string)));
      stringMap.put(string, idx);
      ldcTypeByIdx.put(idx, "Ljava/lang/String;");
    }
  }

  public void addMethodRef(String classKey, String name, String type) {
    String key = classKey + "_" + name + "_" + type;
    if (!methodRefMap.containsKey(key)) {
      int idx = append(new ConstantMethodref(clazz(classKey), nameAndType(name, type)));
      methodRefMap.put(key, idx);
      methodDescByIdx.put(idx, type);
    }
  }

  public void addInterfaceMethodRef(String classKey, String name, String type) {
    String key = classKey + "_" + name + "_" + type;
    if (!interfaceMethodRefMap.containsKey(key)) {
      int idx = append(new ConstantInterfaceMethodref(clazz(classKey), nameAndType(name, type)));
      interfaceMethodRefMap.put(key, idx);
      interfaceMethodDescByIdx.put(idx, type);
    }
  }

  public void addFieldRef(String classKey, String name, String type) {
    String key = classKey + "_" + name + "_" + type;
    if (!fieldRefMap.containsKey(key)) {
      int idx = append(new ConstantFieldref(clazz(classKey), nameAndType(name, type)));
      fieldRefMap.put(key, idx);
      fieldDescByIdx.put(idx, type);
    }
  }

  // (via indexOf(), which a test's mock constant pool overrides)
  private short clazz(String classKey) {
    addClass(classKey);
    return indexOf(ConstantTypes.CONSTANT_CLASS, classKey);
  }

  public void addNameAndType(String name, String type) {
    nameAndType(name, type);
  }

  private short nameAndType(String name, String type) {
    String key = name + "_" + type;
    Integer idx = nameAndTypesMap.get(key);
    if (idx == null) {
      idx = append(new ConstantNameAndType((short) utf8Idx(name), (short) utf8Idx(type)));
      nameAndTypesMap.put(key, idx);
    }
    return idx.shortValue();
  }

  public void addInteger(Integer val) {
    if (!integerMap.containsKey(val)) {
      int idx = append(new ConstantInteger(val));
      integerMap.put(val, idx);
      ldcTypeByIdx.put(idx, "I");
    }
  }

  public void addLong(Long val) {
    if (!longMap.containsKey(val)) {
      int idx = append(new ConstantLong(val));
      longMap.put(val, idx);
      ldcTypeByIdx.put(idx, "J");
    }
  }

  public void addDouble(Double val) {
    if (!doubleMap.containsKey(val)) {
      int idx = append(new ConstantDouble(val));
      doubleMap.put(val, idx);
      ldcTypeByIdx.put(idx, "D");
    }
  }

  public void addFloat(Float val) {
    if (!floatMap.containsKey(val)) {
      int idx = append(new ConstantFloat(val));
      floatMap.put(val, idx);
      ldcTypeByIdx.put(idx, "F");
    }
  }

  public void addByType(String type, Object val) {
    if (INT_CONSTANT_TYPES.contains(type)) {
      ctx.constPool.addInteger((Integer) val);
    } else if ("long".equals(type)) {
      if (val instanceof Integer) {
        val = Long.valueOf(((Integer) val).longValue());
      }
      ctx.constPool.addLong((Long) val);
    } else if ("float".equals(type)) {
      ctx.constPool.addFloat((Float) val);
    } else if ("double".equals(type)) {
      if (val instanceof Float) {
        val = Double.valueOf(((Float) val).doubleValue());
      }
      ctx.constPool.addDouble((Double) val);
    } else if ("String".equals(type)) {
      ctx.constPool.addString((String) val);
    }
  }

  /** Returns the method descriptor for the given CP index, or {@code null} if not found. */
  public String getMethodDescriptor(int cpIdx) {
    String d = methodDescByIdx.get(cpIdx);
    return d != null ? d : interfaceMethodDescByIdx.get(cpIdx);
  }

  /** Returns the class name (e.g. "java/lang/String") at the given CP index, or null. */
  public String getClassName(int cpIdx) {
    return classNameByIdx.get(cpIdx);
  }

  /** Returns the field type descriptor for the given CP index, or {@code null} if not found. */
  public String getFieldDescriptor(int cpIdx) {
    return fieldDescByIdx.get(cpIdx);
  }

  /**
   * Returns the JVM verification type for a LDC-able constant at the given CP index ("I", "J", "F",
   * "D", "Ljava/lang/String;", "Ljava/lang/Class;"), or {@code null} if not found.
   */
  public String getLdcType(int cpIdx) {
    return ldcTypeByIdx.get(cpIdx);
  }

  public byte getTypeByte(String type) {
    if (INT_CONSTANT_TYPES.contains(type)) {
      return CONSTANT_INTEGER;
    } else if ("long".equals(type)) {
      return CONSTANT_LONG;
    } else if ("double".equals(type)) {
      return CONSTANT_DOUBLE;
    } else if ("float".equals(type)) {
      return CONSTANT_FLOAT;
    } else if ("String".equals(type)) {
      return CONSTANT_STRING;
    }
    ctx.errors.addError(
        LOGGER_NAME,
        String.format("Unknown type: %s", type),
        new RuntimeException().getStackTrace());
    return -1;
  }
}
