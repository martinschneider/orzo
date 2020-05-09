package io.github.martinschneider.kommpeiler.codegen.constants;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.intToByteArray;

public class ConstantInteger implements Constant {

  private Integer value;

  public ConstantInteger(Integer value) {
    this.value = value;
  }

  @Override
  public byte getTag() {
    return (byte) 0x3;
  }

  @Override
  public byte[] getInfo() {
    return intToByteArray(value);
  }
}
