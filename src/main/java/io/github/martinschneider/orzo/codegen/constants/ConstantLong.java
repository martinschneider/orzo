package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils2.longToByteArray;

public class ConstantLong implements Constant {
  private long _val;

  public ConstantLong(long val) {
    this._val = val;
  }

  @Override
  public byte tag() {
    return 5;
    // return ConstantTypes.CONSTANT_LONG;
  }

  @Override
  public byte[] info() {
    return longToByteArray(_val);
  }
}
