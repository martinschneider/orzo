package io.github.martinschneider.kommpeiler.codegen.constants;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;

public class ConstantClass implements Constant {
  private short id;

  public ConstantClass(short id) {
    this.id = id;
  }

  @Override
  public byte[] getInfo() {
    return shortToByteArray(id);
  }

  @Override
  public byte getTag() {
    return ConstantTypes.CONSTANT_CLASS;
  }
}
