package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

public class ConstantClass implements Constant {
  private short id;

  public ConstantClass(short id) {
    this.id = id;
  }

  @Override
  public byte[] info() {
    return shortToByteArray(id);
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_CLASS;
  }
}
