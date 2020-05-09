package io.github.martinschneider.kommpeiler.codegen.constants;

import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_CLASS;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_FIELDREF;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_INTEGER;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_METHODREF;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_STRING;
import static io.github.martinschneider.kommpeiler.codegen.constants.ConstantTypes.CONSTANT_UTF8;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstantPool {

  private List<Constant> entries;
  private Map<String, Integer> classMap;
  private Map<Integer, Integer> integerMap;
  private Map<String, Integer> stringMap;
  private Map<String, Integer> utf8Map;
  private Map<String, Integer> methodRefMap;
  private Map<String, Integer> fieldRefMap;
  private Map<String, Integer> nameAndTypesMap;

  public ConstantPool() {
    entries = new ArrayList<Constant>();
    classMap = new HashMap<String, Integer>();
    stringMap = new HashMap<String, Integer>();
    integerMap = new HashMap<Integer, Integer>();
    utf8Map = new HashMap<String, Integer>();
    methodRefMap = new HashMap<String, Integer>();
    fieldRefMap = new HashMap<String, Integer>();
    nameAndTypesMap = new HashMap<String, Integer>();
  }

  public int size() {
    return entries.size();
  }

  public void setEntries(final List<Constant> entries) {
    this.entries = entries;
  }

  public List<Constant> getEntries() {
    return entries;
  }

  public byte[] getBytes() {
    DynamicByteArray array = new DynamicByteArray();
    array.write((byte) ((entries.size() + 1 >> 8) & 0xFF));
    array.write((byte) (entries.size() + 1 & 0xFF));
    for (Constant constant : entries) {
      array.write(constant.getTag());
      array.write(constant.getInfo());
    }
    return array.getBytes();
  }

  public int add(Constant entry) {
    entries.add(entry);
    return entries.size();
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
          if (id == null) {
            addClass(key.toString());
          }
          return classMap.get(key).shortValue();
        }
      case CONSTANT_UTF8:
        {
          Integer id = utf8Map.get(key);
          if (id == null) {
            addUtf8(key.toString());
          }
          return utf8Map.get(key).shortValue();
        }
      case CONSTANT_STRING:
        {
          Integer id = stringMap.get(key);
          if (id == null) {
            addString(key.toString());
          }
          return stringMap.get(key).shortValue();
        }
      case CONSTANT_INTEGER:
        {
          Integer id = integerMap.get(key);
          if (id == null) {
            addInteger((Integer) key);
          }
          return integerMap.get(key).shortValue();
        }
      default:
        return -1;
    }
  }

  public void addClass(String clazz) {
    classMap.put(clazz, add(new ConstantClass((short) (size() + 2))));
    addUtf8(clazz);
  }

  public void addUtf8(String text) {
    utf8Map.put(text, add(new ConstantUtf8(text)));
  }

  public void addString(String string) {
    stringMap.put(string, add(new ConstantString((short) (size() + 2))));
    addUtf8(string);
  }

  public void addMethodRef(String classKey, String name, String type) {
    methodRefMap.put(
        classKey + "_" + name + "_" + type,
        add(
            new ConstantMethodref(
                indexOf(ConstantTypes.CONSTANT_CLASS, classKey), (short) (size() + 2))));
    addNameAndType(name, type);
  }

  public void addFieldRef(String classKey, String name, String type) {
    fieldRefMap.put(
        classKey + "_" + name + "_" + type,
        add(
            new ConstantFieldref(
                indexOf(ConstantTypes.CONSTANT_CLASS, classKey), (short) (size() + 2))));
    addNameAndType(name, type);
  }

  public void addNameAndType(String name, String type) {
    nameAndTypesMap.put(
        name + "_" + type,
        add(new ConstantNameAndType((short) (size() + 2), (short) (size() + 3))));
    addUtf8(name);
    addUtf8(type);
  }

  public void addInteger(Integer integer) {
    integerMap.put(integer, add(new ConstantInteger(integer)));
  }
}
