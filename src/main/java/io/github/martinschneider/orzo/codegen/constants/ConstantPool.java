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

  public int add(Constant entry) {
    int idx = entries.indexOf(entry);
    if (idx != -1) {
      return idx;
    } else {
      if (entry instanceof ConstantLong || entry instanceof ConstantDouble) {
        // JVM spec §4.4.5: Long and Double take two consecutive constant-pool slots.
        // After size+=2, the valid index is the slot BEFORE the increment (size-1 post-increment,
        // or size pre-increment). Returning `size` here may produce an off-by-one index.
        // Tests pass currently because Long/Double constants in self-compiled code are rare.
        // TODO: verify this is correct or fix — see docs/TODO.md "ConstantPool.add()".
        size += 2;
        entries.add(entry);
        return size;
      } else {
        size++;
        entries.add(entry);
        return size;
      }
    }
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
    switch (type) {
      case CONSTANT_CLASS:
        {
          Integer id = classMap.get(key);
          if (id != null) {
            return id.shortValue();
          }
        }
      case CONSTANT_UTF8:
        {
          Integer id = utf8Map.get(key);
          if (id != null) {
            return id.shortValue();
          }
        }
      case CONSTANT_STRING:
        {
          Integer id = stringMap.get(key);
          if (id != null) {
            return id.shortValue();
          }
        }
      case CONSTANT_INTEGER:
        {
          Integer id = integerMap.get(key);
          if (id != null) {
            return id.shortValue();
          }
        }
      case CONSTANT_DOUBLE:
        {
          Integer id = doubleMap.get(key);
          if (id != null) {
            return id.shortValue();
          }
        }
      case CONSTANT_FLOAT:
        {
          Integer id = floatMap.get(key);
          if (id != null) {
            return id.shortValue();
          }
        }
      case CONSTANT_LONG:
        {
          if (key instanceof Integer) {
            key = Long.valueOf((Integer) (key)).longValue();
          }
          Integer id = longMap.get(key);
          if (id != null) {
            return id.shortValue();
          }
        }
      default:
        if (!allowMissing) {
          ctx.errors.addError(
              LOGGER_NAME,
              String.format("expected key %s of type %s not found in constant pool", key, type),
              new RuntimeException().getStackTrace());
        }
        return -1;
    }
  }

  public void addClass(String clazz) {
    int idx = add(new ConstantClass((short) (size + 2)));
    classMap.put(clazz, idx);
    ldcTypeByIdx.put(idx, "Ljava/lang/Class;");
    addUtf8(clazz);
  }

  public void addUtf8(String text) {
    utf8Map.put(text, add(new ConstantUtf8(text)));
  }

  public void addString(String string) {
    int idx = add(new ConstantString((short) (size + 2)));
    stringMap.put(string, idx);
    ldcTypeByIdx.put(idx, "Ljava/lang/String;");
    addUtf8(string);
  }

  public void addMethodRef(String classKey, String name, String type) {
    int cpIdx =
        add(
            new ConstantMethodref(
                indexOf(ConstantTypes.CONSTANT_CLASS, classKey), (short) (size + 2)));
    methodRefMap.put(classKey + "_" + name + "_" + type, cpIdx);
    methodDescByIdx.put(cpIdx, type);
    addNameAndType(name, type);
  }

  public void addInterfaceMethodRef(String classKey, String name, String type) {
    int cpIdx =
        add(
            new ConstantInterfaceMethodref(
                indexOf(ConstantTypes.CONSTANT_CLASS, classKey), (short) (size + 2)));
    interfaceMethodRefMap.put(classKey + "_" + name + "_" + type, cpIdx);
    interfaceMethodDescByIdx.put(cpIdx, type);
    addNameAndType(name, type);
  }

  public void addFieldRef(String classKey, String name, String type) {
    int cpIdx =
        add(
            new ConstantFieldref(
                indexOf(ConstantTypes.CONSTANT_CLASS, classKey), (short) (size + 2)));
    fieldRefMap.put(classKey + "_" + name + "_" + type, cpIdx);
    fieldDescByIdx.put(cpIdx, type);
    addNameAndType(name, type);
  }

  public void addNameAndType(String name, String type) {
    nameAndTypesMap.put(
        name + "_" + type, add(new ConstantNameAndType((short) (size + 2), (short) (size + 3))));
    addUtf8(name);
    addUtf8(type);
  }

  public void addInteger(Integer val) {
    int idx = add(new ConstantInteger(val));
    integerMap.put(val, idx);
    ldcTypeByIdx.put(idx, "I");
  }

  public void addLong(Long val) {
    int idx = add(new ConstantLong(val));
    longMap.put(val, idx);
    ldcTypeByIdx.put(idx, "J");
  }

  public void addDouble(Double val) {
    int idx = add(new ConstantDouble(val));
    doubleMap.put(val, idx);
    ldcTypeByIdx.put(idx, "D");
  }

  public void addFloat(Float val) {
    int idx = add(new ConstantFloat(val));
    floatMap.put(val, idx);
    ldcTypeByIdx.put(idx, "F");
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
