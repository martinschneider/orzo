package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.codegen.MockConstantPool.constant;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.clazz;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static io.github.martinschneider.orzo.parser.TestHelper.varInfo;
import static io.github.martinschneider.orzo.parser.TestHelper.varMap;
import static io.github.martinschneider.orzo.util.decompiler.BytecodeDecompiler.decompile;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.martinschneider.orzo.codegen.generators.MethodGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.parser.MethodParser;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
public class MethodGeneratorTest {

  private Lexer lexer = new Lexer();
  private MethodGenerator target;
  private MethodParser parser = new MethodParser(ParserContext.build(new CompilerErrors()));
  private HasOutput out;

  // all tests assume the methods are part of this class
  private static final Clazz clazz =
      clazz(
          "",
          emptyList(),
          scope(PUBLIC),
          "Martin",
          emptyList(),
          Clazz.JAVA_LANG_OBJECT,
          emptyList(),
          emptyList());

  @BeforeAll
  public void init() {
    target = new MethodGenerator(new CGContext());
    parser.ctx.currClazz = clazz;
    out = new DynamicByteArray();
  }

  @BeforeEach
  public void reset() {
    out = new DynamicByteArray();
  }

  private static Stream<Arguments> test() {
    // TODO: comparing the raw hex representation is not very elegant
    return stream(
        args(
            "public void test(){x=1;}",
            list(constant("()V", 1), constant("Code", 2), constant("test", 3)),
            varMap(list(varInfo("x", "int", 0))),
            clazz,
            "000100030001000100020000000F00020001000000",
            list("iconst_1", "istore_0", "return"),
            "000000"),
        args(
            "public Martin(){x=1;y=2}",
            list(
                constant(2L, 1),
                constant("java/math/BigInteger", 2),
                constant("valueOf", 3),
                constant("pow", 4),
                constant("longValue", 5),
                constant("Code", 6),
                constant("Martin", 7),
                constant("().Martin", 8)),
            varMap(list(varInfo("x", "int", 0), varInfo("y", "int", 1))),
            clazz,
            "000100070008000100060000001100030001000000",
            list("iconst_1", "istore_0", "iconst_2", "istore_1", "return"),
            "000000"));
  }

  @ParameterizedTest
  @MethodSource
  public void test(
      String input,
      List<Constant> constants,
      VariableMap fields,
      Clazz clazz,
      String expectedHeadStr,
      List<String> expectedLines,
      String expectedFootStr)
      throws IOException {
    target.ctx.init(new CompilerErrors(), 0, list(clazz));
    target.ctx.constPool = new MockConstantPool(target.ctx, constants, fields);
    parser.parse(lexer.getTokens(input));
    target.generate(out, parser.parse(lexer.getTokens(input)), fields, clazz);
    byte[] expectedHead = hexStringToByteArray(expectedHeadStr);
    byte[] actualHead = Arrays.copyOfRange(out.getBytes(), 0, expectedHead.length);
    byte[] expectedFoot = hexStringToByteArray(expectedFootStr);
    byte[] actualFoot =
        Arrays.copyOfRange(
            out.getBytes(), out.getBytes().length - expectedFoot.length, out.getBytes().length);
    byte[] code =
        Arrays.copyOfRange(
            out.getBytes(),
            expectedHead.length + 1,
            out.getBytes().length - expectedFoot.length - 1);
    String actualHeadStr = bytesToHex(actualHead);
    String actualFootStr = bytesToHex(actualFoot);
    // we also compare the String representations because it makes debugging easier
    assertEquals(expectedHeadStr, actualHeadStr);
    assertArrayEquals(expectedHead, actualHead);
    assertEquals(String.join("\n", expectedLines), decompile(code));
    assertEquals(expectedFootStr, actualFootStr);
    assertArrayEquals(expectedFoot, actualFoot);
    assertFalse(target.ctx.errors.count() > 0, "Compilation errors " + target.ctx.errors);
  }

  private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

  private static String bytesToHex(byte[] bytes) {
    byte[] hexChars = new byte[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars, StandardCharsets.UTF_8);
  }

  private static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }
}
