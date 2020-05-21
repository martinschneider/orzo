package io.github.martinschneider.kommpeiler.codegen;

public class VariableInfo {
  private String name;
  private String type;
  private byte idx;

  public VariableInfo(String name, String type, byte idx) {
    super();
    this.name = name;
    this.type = type;
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
}
