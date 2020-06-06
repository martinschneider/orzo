package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantInteger implements Constant {
  private Integer val;

  public ConstantInteger(Integer val) {
    this.val = val;
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_INTEGER;
  }

  @Override
  public byte[] info() {
    return intToByteArray(val);
  }
}
