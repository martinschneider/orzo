package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.longToByteArray;

public class ConstantLong implements Constant {
  private long val;

  public ConstantLong(long val) {
    this.val = val;
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_LONG;
  }

  @Override
  public byte[] info() {
    return longToByteArray(val);
  }
}
