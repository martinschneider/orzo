package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantFloat implements Constant {
  private float _val;

  public ConstantFloat(float val) {
    this._val = val;
  }

  @Override
  public byte[] info() {
    return intToByteArray(Float.floatToIntBits(_val));
  }

  @Override
  public byte tag() {
    return 4;
    // return ConstantTypes.CONSTANT_FLOAT;
  }
}
