package io.github.martinschneider.kommpeiler.codegen.constants;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;

public class ConstantString implements Constant {

  private short id;

  public ConstantString(short id) {
    this.id = id;
  }

  @Override
  public byte getTag() {
    return ConstantTypes.CONSTANT_STRING;
  }

  @Override
  public byte[] getInfo() {
    return shortToByteArray(id);
  }
}
