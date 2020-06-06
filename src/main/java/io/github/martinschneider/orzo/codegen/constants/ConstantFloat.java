package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantFloat implements Constant {
  private float val;

  public ConstantFloat(float val) {
    this.val = val;
  }

  @Override
  public byte[] info() {
    return intToByteArray(Float.floatToIntBits(val));
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_FLOAT;
  }
}
