package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantMethodref implements Constant {
  private short classId;
  private short nameAndTypeId;

  public ConstantMethodref(short classId, short nameAndTypeId) {
    this.classId = classId;
    this.nameAndTypeId = nameAndTypeId;
  }

  @Override
  public byte[] info() {
    return intToByteArray((classId << 16) | (nameAndTypeId & 0xFFFF));
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_METHODREF;
  }
}
