package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils2.longToByteArray;

public class ConstantDouble implements Constant {
  private double _val;

  public ConstantDouble(double val) {
    this._val = val;
  }

  @Override
  public byte[] info() {
    return longToByteArray(Double.doubleToLongBits(_val));
  }

  @Override
  public byte tag() {
    return 6;
    // return ConstantTypes.CONSTANT_DOUBLE;
  }
}
