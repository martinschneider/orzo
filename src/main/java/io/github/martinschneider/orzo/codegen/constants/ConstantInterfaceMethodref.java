package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantInterfaceMethodref implements Constant {
  private short _classId;
  private short _nameAndTypeId;

  public ConstantInterfaceMethodref(short classId, short nameAndTypeId) {
    this._classId = classId;
    this._nameAndTypeId = nameAndTypeId;
  }

  @Override
  public byte[] info() {
    return intToByteArray((_classId << 16) | (_nameAndTypeId & 65535));
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_INTERFACEMETHODREF;
  }
}
