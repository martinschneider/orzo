package io.github.martinschneider.orzo.codegen;

public class VariableInfo {
  public String name;
  public String type;
  public boolean isField;
  public String arrType; // for arrays, type is reference and subtype is the type of elements in
  // the array
  public short idx;

  public VariableInfo(String name, String type, boolean isField, short idx) {
    this(name, type, null, isField, idx);
  }

  public VariableInfo(String name, String type, String arrayType, boolean isField, short idx) {
    this.name = name;
    this.type = type;
    this.arrType = arrayType;
    this.isField = isField;
    this.idx = idx;
  }

  @Override
  public String toString() {
    return "VAR[name=" + name + ", type=" + type + ", subtype=" + arrType + ", idx=" + idx + "]";
  }
}
