package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils2.longToByteArray;

public class ConstantDouble implements Constant {
  private double val;

  public ConstantDouble(double val) {
    this.val = val;
  }

  @Override
  public byte[] info() {
    return longToByteArray(Double.doubleToLongBits(val));
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_DOUBLE;
  }
}
