package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.combine;
import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

import java.io.UnsupportedEncodingException;

public class ConstantUtf8 implements Constant {
  private String value;

  public ConstantUtf8(String value) {
    this.value = value;
  }

  @Override
  public byte[] getInfo() {
    try {
      byte[] data = value.getBytes("UTF-8");
      byte[] length = shortToByteArray((short) data.length);
      return combine(length, data);
    } catch (UnsupportedEncodingException e) {
      return value.getBytes();
    }
  }

  @Override
  public byte getTag() {
    return ConstantTypes.CONSTANT_UTF8;
  }
}
