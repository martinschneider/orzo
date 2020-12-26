package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.intToByteArray;

public class ConstantInterfaceMethodref implements Constant {
  private short classId;
  private short nameAndTypeId;

  public ConstantInterfaceMethodref(short classId, short nameAndTypeId) {
    this.classId = classId;
    this.nameAndTypeId = nameAndTypeId;
  }

  @Override
  public byte[] info() {
    return intToByteArray((classId << 16) | (nameAndTypeId & 0xFFFF));
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_INTERFACEMETHODREF;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + classId;
    result = prime * result + nameAndTypeId;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    ConstantInterfaceMethodref other = (ConstantInterfaceMethodref) obj;
    if (classId != other.classId) return false;
    if (nameAndTypeId != other.nameAndTypeId) return false;
    return true;
  }
}
