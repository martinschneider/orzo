package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantNameAndType implements Constant {

  private short nameId;
  private short typeId;

  public ConstantNameAndType(short nameId, short typeId) {
    this.nameId = nameId;
    this.typeId = typeId;
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_NAMEANDTYPE;
  }

  @Override
  public byte[] info() {
    return intToByteArray((nameId << 16) | (typeId & 0xFFFF));
  }
}
