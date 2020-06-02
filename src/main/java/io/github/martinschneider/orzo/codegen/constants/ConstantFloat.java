package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantFloat implements Constant {
  private float value;

  public ConstantFloat(float value) {
    this.value = value;
  }

  @Override
  public byte[] getInfo() {
    return intToByteArray(Float.floatToIntBits(value));
  }

  @Override
  public byte getTag() {
    return ConstantTypes.CONSTANT_FLOAT;
  }
}
