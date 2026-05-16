package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

public class ConstantClass implements Constant {
  private short val;

  public ConstantClass(short val) {
    this.val = val;
  }

  public byte[] info() {
    return shortToByteArray(val);
  }

  public byte tag() {
    return ConstantTypes.CONSTANT_CLASS;
  }
}
