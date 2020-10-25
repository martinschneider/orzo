package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.ByteUtils.combine;
import static io.github.martinschneider.orzo.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.orzo.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_2;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_3;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_4;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_5;
import static io.github.martinschneider.orzo.codegen.OpCodes.ICONST_M1;
import static io.github.martinschneider.orzo.codegen.OpCodes.SIPUSH;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import io.github.martinschneider.orzo.codegen.generators.PushGenerator;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(Lifecycle.PER_CLASS)
public class CodeGeneratorTest {
  private PushGenerator target = new PushGenerator(new CGContext());

  @BeforeAll
  public void setup() {
    target.ctx.opStack = new OperandStack();
  }

  private static Stream<Arguments> testIntegerConstants() {
    return stream(
        args(-32768, combine(SIPUSH, shortToByteArray((short) -32768))),
        args(-129, combine(SIPUSH, (short) -129)),
        args(-128, combine(BIPUSH, (byte) -128)),
        args(-2, combine(BIPUSH, (byte) -2)),
        args(-1, new byte[] {ICONST_M1}),
        args(0, new byte[] {ICONST_0}),
        args(1, new byte[] {ICONST_1}),
        args(2, new byte[] {ICONST_2}),
        args(3, new byte[] {ICONST_3}),
        args(4, new byte[] {ICONST_4}),
        args(5, new byte[] {ICONST_5}),
        args(6, combine(BIPUSH, (byte) 6)),
        args(127, combine(BIPUSH, (byte) 127)),
        args(128, combine(SIPUSH, (short) 128)),
        args(32767, combine(SIPUSH, (short) 32767)));
  }

  @ParameterizedTest
  @MethodSource
  /** verify that ICONST_*, BIPUSH and SIPUSH are used where applicable */
  public void testIntegerConstants(int val, byte[] expected) {
    assertArrayEquals(target.push(new DynamicByteArray(), INT, val).getBytes(), expected);
  }
}
