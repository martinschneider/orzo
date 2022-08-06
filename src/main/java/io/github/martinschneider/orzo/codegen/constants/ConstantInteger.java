package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantInteger implements Constant {
  private int _val;

  public ConstantInteger(int val) {
    this._val = val;
  }

  @Override
  public byte tag() {
    return 3;
    // return ConstantTypes.CONSTANT_INTEGER;
  }

  @Override
  public byte[] info() {
    return intToByteArray(_val);
  }
}
