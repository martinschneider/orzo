package io.github.martinschneider.kommpeiler.codegen;

import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.combine;
import static io.github.martinschneider.kommpeiler.codegen.ByteUtils.shortToByteArray;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.BIPUSH;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_0;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_2;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_3;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_4;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_5;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.ICONST_M1;
import static io.github.martinschneider.kommpeiler.codegen.OpCodes.SIPUSH;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import io.github.martinschneider.kommpeiler.codegen.constants.ConstantPool;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class StackCodeGeneratorTest {
  private static Stream<Arguments> testIntegerConstants() {
    return Stream.of(
        Arguments.of(-32768, combine(SIPUSH, shortToByteArray(-32768))),
        Arguments.of(-129, combine(SIPUSH, (short) -129)),
        Arguments.of(-128, combine(BIPUSH, (byte) -128)),
        Arguments.of(-2, combine(BIPUSH, (byte) -2)),
        Arguments.of(-1, new byte[] {ICONST_M1}),
        Arguments.of(0, new byte[] {ICONST_0}),
        Arguments.of(1, new byte[] {ICONST_1}),
        Arguments.of(2, new byte[] {ICONST_2}),
        Arguments.of(3, new byte[] {ICONST_3}),
        Arguments.of(4, new byte[] {ICONST_4}),
        Arguments.of(5, new byte[] {ICONST_5}),
        Arguments.of(6, combine(BIPUSH, (byte) 6)),
        Arguments.of(127, combine(BIPUSH, (byte) 127)),
        Arguments.of(128, combine(SIPUSH, (short) 128)),
        Arguments.of(32767, combine(SIPUSH, (short) 32767)));
  }

  @ParameterizedTest
  @MethodSource
  /** verify that ICONST_*, BIPUSH and SIPUSH are used where applicable */
  public void testIntegerConstants(int value, byte[] expected) {
    assertArrayEquals(
        StackCodeGenerator.pushInteger(new DynamicByteArray(), new ConstantPool(), value)
            .getBytes(),
        expected);
  }
}
