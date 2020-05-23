package io.github.martinschneider.kommpeiler.codegen.constants;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.longToByteArray;

public class ConstantLong implements Constant {
  private Long value;

  public ConstantLong(Long value) {
    this.value = value;
  }

  @Override
  public byte getTag() {
    return ConstantTypes.CONSTANT_LONG;
  }

  @Override
  public byte[] getInfo() {
    return longToByteArray(value);
  }
}
