package io.github.martinschneider.kommpeiler.codegen.constants;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.intToByteArray;

public class ConstantNameAndType implements Constant {

  private short nameId;
  private short typeId;

  public ConstantNameAndType(short nameId, short typeId) {
    this.nameId = nameId;
    this.typeId = typeId;
  }

  @Override
  public byte getTag() {
    return ConstantTypes.CONSTANT_NAMEANDTYPE;
  }

  @Override
  public byte[] getInfo() {
    return intToByteArray((nameId << 16) | (typeId & 0xFFFF));
  }
}
