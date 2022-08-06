package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantNameAndType implements Constant {

  private short _nameId;
  private short _typeId;

  public ConstantNameAndType(short nameId, short typeId) {
    this._nameId = nameId;
    this._typeId = typeId;
  }

  @Override
  public byte tag() {
    return 12;
    // return ConstantTypes.CONSTANT_NAMEANDTYPE;
  }

  @Override
  public byte[] info() {
    // return intToByteArray((_nameId << 16) | (_typeId & 0xFFFF));
    return intToByteArray((_nameId << 16) | (_typeId & 65535));
  }
}
