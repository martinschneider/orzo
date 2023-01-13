package io.github.martinschneider.orzo.parser.util.decompiler;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.util.Factory.list;
import static io.github.martinschneider.orzo.util.Factory.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.util.decompiler.BytecodeDecompiler;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BytecodeDecompilerTest {

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args(
            new byte[] {(byte) 0xbc, 0x0a, 0x59, 0x03, 0x10, 0x01},
            list("newarray 10", "dup", "iconst_0", "bipush 1")),
        args(new byte[] {(byte) 0xbc}, list("newarray ", "UNEXPECTED END OF INPUT")));
  }

  @ParameterizedTest
  @MethodSource
  public void test(byte[] input, List<String> expectedLines) throws IOException {
    assertEquals(String.join("\n", expectedLines), BytecodeDecompiler.decompile(input));
  }
}
