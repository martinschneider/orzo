package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

public class ConstantString implements Constant {

  private short val;

  public ConstantString(short val) {
    this.val = val;
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_STRING;
  }

  @Override
  public byte[] info() {
    return shortToByteArray(val);
  }
}
