package io.github.martinschneider.orzo.codegen.constants;

import static io.github.martinschneider.orzo.codegen.ByteUtils.combine;
import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

import java.io.UnsupportedEncodingException;

public class ConstantUtf8 implements Constant {
  private String val;

  public ConstantUtf8(String val) {
    this.val = val;
  }

  @Override
  public byte[] info() {
    try {
      byte[] data = val.getBytes("UTF-8");
      byte[] length = shortToByteArray((short) data.length);
      return combine(length, data);
    } catch (UnsupportedEncodingException e) {
      return val.getBytes();
    }
  }

  @Override
  public byte tag() {
    return ConstantTypes.CONSTANT_UTF8;
  }
}
