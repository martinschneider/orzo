package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.OpCodes.ILOAD;
import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.codegen.generators.BasicGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.util.decompiler.BytecodeDecompiler;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(Lifecycle.PER_CLASS)
public class BasicGeneratorTest {
  private BasicGenerator target;
  private DynamicByteArray out;
  private CGContext ctx;

  private static Stream<Arguments> convertTest() throws IOException {
    return stream(
        args("int", "int", emptyList(), 0),
        args("int", "long", list("i2l"), 0),
        args("int", "byte", list("i2b"), 0),
        args("int", "short", list("i2s"), 0),
        args("int", "boolean", emptyList(), 1),
        args("int", "char", list("i2c"), 0),
        args("int", "double", list("i2d"), 0),
        args("int", "float", list("i2f"), 0),
        args("long", "int", list("l2i"), 0),
        args("long", "long", emptyList(), 0),
        args("long", "byte", list("l2i", "i2b"), 0),
        args("long", "short", list("l2i", "i2s"), 0),
        args("long", "boolean", emptyList(), 1),
        args("long", "char", list("l2i", "i2c"), 0),
        args("long", "double", list("l2d"), 0),
        args("long", "float", list("l2f"), 0),
        args("byte", "int", emptyList(), 0),
        args("byte", "long", list("i2l"), 0),
        args("byte", "byte", emptyList(), 0),
        args("byte", "short", list("i2s"), 0),
        args("byte", "boolean", emptyList(), 1),
        args("byte", "char", list("i2c"), 0),
        args("byte", "double", list("i2d"), 0),
        args("byte", "float", list("i2f"), 0),
        args("short", "int", emptyList(), 0),
        args("short", "long", list("i2l"), 0),
        args("short", "byte", list("i2b"), 0),
        args("short", "short", emptyList(), 0),
        args("short", "boolean", emptyList(), 1),
        args("short", "char", list("i2c"), 0),
        args("short", "double", list("i2d"), 0),
        args("short", "float", list("i2f"), 0),
        args("boolean", "int", emptyList(), 1),
        args("boolean", "long", emptyList(), 1),
        args("boolean", "byte", emptyList(), 1),
        args("boolean", "short", emptyList(), 1),
        args("boolean", "boolean", emptyList(), 1),
        args("boolean", "char", emptyList(), 1),
        args("boolean", "double", emptyList(), 1),
        args("boolean", "float", emptyList(), 1),
        args("char", "int", emptyList(), 0),
        args("char", "long", list("i2l"), 0),
        args("char", "byte", list("i2b"), 0),
        args("char", "short", list("i2s"), 0),
        args("char", "boolean", emptyList(), 1),
        args("char", "char", emptyList(), 0),
        args("char", "double", list("i2d"), 0),
        args("char", "float", list("i2f"), 0),
        args("double", "int", list("d2i"), 0),
        args("double", "long", list("d2l"), 0),
        args("double", "byte", list("d2i", "i2b"), 0),
        args("double", "short", list("d2i", "i2s"), 0),
        args("double", "boolean", emptyList(), 1),
        args("double", "char", list("d2i", "i2c"), 0),
        args("double", "double", emptyList(), 0),
        args("double", "float", list("d2f"), 0),
        args("float", "int", list("f2i"), 0),
        args("float", "long", list("f2l"), 0),
        args("float", "byte", list("f2i", "i2b"), 0),
        args("float", "short", list("f2i", "i2s"), 0),
        args("float", "boolean", emptyList(), 1),
        args("float", "char", list("f2i", "i2c"), 0),
        args("float", "double", list("f2d"), 0),
        args("float", "float", emptyList(), 0));
  }

  private static Stream<Arguments> convert1Test() throws IOException {
    return stream(
        args("int", "int", emptyList(), 0),
        args("int", "long", list("i2l"), 0),
        args("int", "byte", emptyList(), 0),
        args("int", "short", emptyList(), 0),
        args("int", "boolean", emptyList(), 1),
        args("int", "char", emptyList(), 0),
        args("int", "double", list("i2d"), 0),
        args("int", "float", list("i2f"), 0),
        args("long", "int", list("l2i"), 0),
        args("long", "long", emptyList(), 0),
        args("long", "byte", list("l2i"), 0),
        args("long", "short", list("l2i"), 0),
        args("long", "boolean", emptyList(), 1),
        args("long", "char", list("l2i"), 0),
        args("long", "double", list("l2d"), 0),
        args("long", "float", list("l2f"), 0),
        args("byte", "int", emptyList(), 0),
        args("byte", "long", list("i2l"), 0),
        args("byte", "byte", emptyList(), 0),
        args("byte", "short", emptyList(), 0),
        args("byte", "boolean", emptyList(), 1),
        args("byte", "char", emptyList(), 0),
        args("byte", "double", list("i2d"), 0),
        args("byte", "float", list("i2f"), 0),
        args("short", "int", emptyList(), 0),
        args("short", "long", list("i2l"), 0),
        args("short", "byte", emptyList(), 0),
        args("short", "short", emptyList(), 0),
        args("short", "boolean", emptyList(), 1),
        args("short", "char", emptyList(), 0),
        args("short", "double", list("i2d"), 0),
        args("short", "float", list("i2f"), 0),
        args("boolean", "int", emptyList(), 1),
        args("boolean", "long", emptyList(), 1),
        args("boolean", "byte", emptyList(), 1),
        args("boolean", "short", emptyList(), 1),
        args("boolean", "boolean", emptyList(), 1),
        args("boolean", "char", emptyList(), 1),
        args("boolean", "double", emptyList(), 1),
        args("boolean", "float", emptyList(), 1),
        args("char", "int", emptyList(), 0),
        args("char", "long", list("i2l"), 0),
        args("char", "byte", emptyList(), 0),
        args("char", "short", emptyList(), 0),
        args("char", "boolean", emptyList(), 1),
        args("char", "char", emptyList(), 0),
        args("char", "double", list("i2d"), 0),
        args("char", "float", list("i2f"), 0),
        args("double", "int", list("d2i"), 0),
        args("double", "long", list("d2l"), 0),
        args("double", "byte", list("d2i"), 0),
        args("double", "short", list("d2i"), 0),
        args("double", "boolean", emptyList(), 1),
        args("double", "char", list("d2i"), 0),
        args("double", "double", emptyList(), 0),
        args("double", "float", list("d2f"), 0),
        args("float", "int", list("f2i"), 0),
        args("float", "long", list("f2l"), 0),
        args("float", "byte", list("f2i"), 0),
        args("float", "short", list("f2i"), 0),
        args("float", "boolean", emptyList(), 1),
        args("float", "char", list("f2i"), 0),
        args("float", "double", list("f2d"), 0),
        args("float", "float", emptyList(), 0));
  }

  private static Stream<Arguments> wideTest() throws IOException {
    return stream(
        args((short) 0, ILOAD, list("iload", "0")),
        args((short) 128, ILOAD, list("wide", "iload", "0", "-128"))); // TODO: decompiler should
    // support multi-byte values
  }

  @BeforeAll
  public void init() {
    ctx = new CGContext();
    ctx.opStack = new OperandStack();
    ctx.errors = new CompilerErrors();
    target = new BasicGenerator(ctx);
  }

  @BeforeEach
  public void reset() {
    out = new DynamicByteArray();
    ctx.errors = new CompilerErrors();
  }

  @ParameterizedTest
  @MethodSource
  public void convertTest(String from, String to, List<String> expectedLines, int expectedErrors)
      throws IOException {
    target.convert(out, from, to);
    assertEquals(String.join("\n", expectedLines), BytecodeDecompiler.decompile(out.getBytes()));
    assertEquals(expectedErrors, ctx.errors.count());
  }

  @ParameterizedTest
  @MethodSource
  public void convert1Test(String from, String to, List<String> expectedLines, int expectedErrors)
      throws IOException {
    target.convert1(out, from, to);
    assertEquals(String.join("\n", expectedLines), BytecodeDecompiler.decompile(out.getBytes()));
    assertEquals(expectedErrors, ctx.errors.count());
  }

  @ParameterizedTest
  @MethodSource
  public void wideTest(short idx, byte opCode, List<String> expectedLines) throws IOException {
    target.wide(out, idx, opCode);
    assertEquals(String.join(" ", expectedLines), BytecodeDecompiler.decompile(out.getBytes()));
  }
}
