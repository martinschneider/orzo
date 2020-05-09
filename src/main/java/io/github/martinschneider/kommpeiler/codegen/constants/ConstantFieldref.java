package io.github.martinschneider.kommpeiler.codegen.constants;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.intToByteArray;

public class ConstantFieldref implements Constant {
  private short classId;
  private short nameAndTypeId;

  public ConstantFieldref(short classId, short nameAndTypeId) {
    this.classId = classId;
    this.nameAndTypeId = nameAndTypeId;
  }

  @Override
  public byte[] getInfo() {
    return intToByteArray((classId << 16) | (nameAndTypeId & 0xFFFF));
  }

  @Override
  public byte getTag() {
    return ConstantTypes.CONSTANT_FIELDREF;
  }
}
