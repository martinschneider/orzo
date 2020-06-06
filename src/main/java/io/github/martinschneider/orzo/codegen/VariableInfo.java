package io.github.martinschneider.orzo.codegen;

public class VariableInfo {
  public String name;
  public String type;
  public String arrType; // for arrays, type is reference and subtype is the type of elements in
  // the array
  public byte idx;

  public VariableInfo(String name, String type, byte idx) {
    this(name, type, null, idx);
  }

  public VariableInfo(String name, String type, String arrayType, byte idx) {
    this.name = name;
    this.type = type;
    this.arrType = arrayType;
    this.idx = idx;
  }

  @Override
  public String toString() {
    return "VAR[name=" + name + ", type=" + type + ", subtype=" + arrType + ", idx=" + idx + "]";
  }
}
