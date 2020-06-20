package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.orzo.codegen.OpCodes.SIPUSH;

public class PushGenerator {
  public static HasOutput sipush(DynamicByteArray out, int number) {
    out.write(SIPUSH);
    out.write((short) number);
    return out;
  }

  public static HasOutput bipush(DynamicByteArray out, int number) {
    out.write(BIPUSH);
    out.write((byte) number);
    return out;
  }

  public static HasOutput pushBool(DynamicByteArray out, boolean value) {
    return bipush(out, (value) ? 1 : 0);
  }
}
