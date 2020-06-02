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

import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPool {
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

  public ConstantPool() {
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

  public void setEntries(final List<Constant> entries) {
    this.entries = entries;
  }

  public List<Constant> getEntries() {
    return entries;
  }

  public byte[] getBytes() {
    DynamicByteArray array = new DynamicByteArray();
    array.write((byte) ((size + 1 >> 8) & 0xFF));
    array.write((byte) (size + 1 & 0xFF));
    for (Constant constant : entries) {
      array.write(constant.getTag());
      array.write(constant.getInfo());
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
      } else {
        size++;
      }
      entries.add(entry);
    }
    return size;
  }

  public short indexOf(byte entryType, String classKey, String name, String type) {
    String compositeKey = classKey + "_" + name + "_" + type;
    switch (entryType) {
      case CONSTANT_METHODREF:
        {
          Integer id = methodRefMap.get(compositeKey);
          if (id == null) {
            addMethodRef(classKey, name, type);
          }
          return methodRefMap.get(compositeKey).shortValue();
        }
      case CONSTANT_FIELDREF:
        {
          Integer id = fieldRefMap.get(compositeKey);
          if (id == null) {
            addFieldRef(classKey, name, type);
          }
          return fieldRefMap.get(compositeKey).shortValue();
        }
      default:
        return -1;
    }
  }

  public short indexOf(byte type, Object key) {
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
          Integer id = longMap.get(key);
          if (id != null) {
            return id.shortValue();
          }
        }
      default:
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

  public void addInteger(Integer value) {
    integerMap.put(value, add(new ConstantInteger(value)));
  }

  public void addLong(Long value) {
    longMap.put(value, add(new ConstantLong(value)));
  }

  public void addDouble(Double value) {
    doubleMap.put(value, add(new ConstantDouble(value)));
  }

  public void addFloat(Float value) {
    floatMap.put(value, add(new ConstantFloat(value)));
  }
}
