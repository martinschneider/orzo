package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.longToByteArray;

public class ConstantDouble implements Constant {
  private double value;

  public ConstantDouble(double value) {
    this.value = value;
  }

  @Override
  public byte[] getInfo() {
    return longToByteArray(Double.doubleToLongBits(value));
  }

  @Override
  public byte getTag() {
    return ConstantTypes.CONSTANT_DOUBLE;
  }
}
