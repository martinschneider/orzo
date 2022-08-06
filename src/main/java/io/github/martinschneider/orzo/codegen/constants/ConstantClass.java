package io.github.martinschneider.orzo.codegen.constants;

// import static io.github.martinschneider.orzo.codegen.constants.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;

public class ConstantClass implements Constant {
  private short _val;

  public ConstantClass(short val) {
    this._val = val;
  }

  public byte[] info() {
    return shortToByteArray(_val);
  }

  public byte tag() {
    return 7;
    // return ConstantTypes.CONSTANT_CLASS;
  }
}
