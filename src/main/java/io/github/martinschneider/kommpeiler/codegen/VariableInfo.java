package io.github.martinschneider.kommpeiler.codegen;

public class VariableInfo {
  private String name;
  private String type;
  private String arrayType; // for arrays, type is reference and subtype is the type of elements in
  // the array
  private byte idx;

  public VariableInfo(String name, String type, byte idx) {
    this(name, type, null, idx);
  }

  public VariableInfo(String name, String type, String arrayType, byte idx) {
    super();
    this.name = name;
    this.type = type;
    this.arrayType = arrayType;
    this.idx = idx;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public byte getIdx() {
    return idx;
  }

  public void setIdx(byte idx) {
    this.idx = idx;
  }

  @Override
  public String toString() {
    return "VariableInfo [name="
        + name
        + ", type="
        + type
        + ", subtype="
        + arrayType
        + ", idx="
        + idx
        + "]";
  }

  public String getArrayType() {
    return arrayType;
  }

  public void setArrayType(String arrayType) {
    this.arrayType = arrayType;
  }
}
