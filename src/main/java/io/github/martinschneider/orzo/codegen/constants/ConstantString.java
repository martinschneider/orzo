package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

public class ConstantString implements Constant {

  private short id;

  public ConstantString(short id) {
    this.id = id;
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_STRING;
  }

  @Override
  public byte[] info() {
    return shortToByteArray(id);
  }
}
