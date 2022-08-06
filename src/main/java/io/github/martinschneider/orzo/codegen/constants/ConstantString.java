package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

public class ConstantString implements Constant {

  private short _val;

  public ConstantString(short val) {
    this._val = val;
  }

  @Override
  public byte tag() {
    return 8;
    // return ConstantTypes.CONSTANT_STRING;
  }

  @Override
  public byte[] info() {
    return shortToByteArray(_val);
  }
}
