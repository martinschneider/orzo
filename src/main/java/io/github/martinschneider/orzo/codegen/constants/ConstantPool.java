package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_DOUBLE;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_FLOAT;
import static io.github.martinschneider.orzo.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
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

  // https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.7.2-300-C.1
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
  private Map<String, Integer> fieldRefMap;
  private Map<String, Integer> nameAndTypesMap;

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
    fieldRefMap = new HashMap<>();
    nameAndTypesMap = new HashMap<>();
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
        size += 2;
        entries.add(entry);
        return size; // TODO: shouldn't this be size-1?
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
                "expected key %s of type %s not found in constant pool", compositeKey, entryType));
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
              String.format(
                  "expected key %s of type %s not found in constant pool %s",
                  key, type, new RuntimeException().getStackTrace()[2]));
        }
        return -1;
    }
  }

  public void addClass(String clazz) {
    classMap.put(clazz, add(new ConstantClass((short) (size + 2))));
    addUtf8(clazz);
  }

  public void addUtf8(String text) {
    utf8Map.put(text, add(new ConstantUtf8(text)));
  }

  public void addString(String string) {
    stringMap.put(string, add(new ConstantString((short) (size + 2))));
    addUtf8(string);
  }

  public void addMethodRef(String classKey, String name, String type) {
    methodRefMap.put(
        classKey + "_" + name + "_" + type,
        add(
            new ConstantMethodref(
                indexOf(ConstantTypes.CONSTANT_CLASS, classKey), (short) (size + 2))));
    addNameAndType(name, type);
  }

  public void addFieldRef(String classKey, String name, String type) {
    fieldRefMap.put(
        classKey + "_" + name + "_" + type,
        add(
            new ConstantFieldref(
                indexOf(ConstantTypes.CONSTANT_CLASS, classKey), (short) (size + 2))));
    addNameAndType(name, type);
  }

  public void addNameAndType(String name, String type) {
    nameAndTypesMap.put(
        name + "_" + type, add(new ConstantNameAndType((short) (size + 2), (short) (size + 3))));
    addUtf8(name);
    addUtf8(type);
  }

  public void addInteger(Integer val) {
    integerMap.put(val, add(new ConstantInteger(val)));
  }

  public void addLong(Long val) {
    longMap.put(val, add(new ConstantLong(val)));
  }

  public void addDouble(Double val) {
    doubleMap.put(val, add(new ConstantDouble(val)));
  }

  public void addFloat(Float val) {
    floatMap.put(val, add(new ConstantFloat(val)));
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
    ctx.errors.addError(LOGGER_NAME, String.format("Unknown type: %s", type));
    return -1;
  }
}
