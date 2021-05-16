package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.TestHelper.args;
import static io.github.martinschneider.orzo.codegen.MockConstantPool.constant;
import static io.github.martinschneider.orzo.lexer.tokens.Type.BYTE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.DOUBLE;
import static io.github.martinschneider.orzo.lexer.tokens.Type.INT;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_PUBLIC;
import static io.github.martinschneider.orzo.util.FactoryHelper.clazz;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;
import static io.github.martinschneider.orzo.util.FactoryHelper.stream;
import static io.github.martinschneider.orzo.util.FactoryHelper.varInfo;
import static io.github.martinschneider.orzo.util.FactoryHelper.varMap;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.martinschneider.orzo.codegen.generators.ExpressionGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.parser.ArrayInitParser;
import io.github.martinschneider.orzo.parser.ArraySelectorParser;
import io.github.martinschneider.orzo.parser.CastParser;
import io.github.martinschneider.orzo.parser.ConstructorCallParser;
import io.github.martinschneider.orzo.parser.ExpressionParser;
import io.github.martinschneider.orzo.parser.FloorParser;
import io.github.martinschneider.orzo.parser.MethodCallParser;
import io.github.martinschneider.orzo.parser.ParserContext;
import io.github.martinschneider.orzo.parser.SqrtParser;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
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
public class ExpressionGeneratorTest {

  private ExpressionGenerator target = new ExpressionGenerator();
  private ExpressionParser parser;
  protected Method method;
  protected DynamicByteArray out;
  protected CGContext ctx;

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("1+2", INT, emptyList(), emptyList(), list("iconst_1", "iconst_2", "iadd")),
        args("1-2", INT, emptyList(), emptyList(), list("iconst_1", "iconst_2", "isub")),
        args("1*2", INT, emptyList(), emptyList(), list("iconst_1", "iconst_2", "imul")),
        args("1/2", INT, emptyList(), emptyList(), list("iconst_1", "iconst_2", "idiv")),
        args("1%2", INT, emptyList(), emptyList(), list("iconst_1", "iconst_2", "irem")),
        args(
            "1**2",
            INT,
            emptyList(),
            list(
                constant(2L, 1),
                constant("java/math/BigInteger", 2),
                constant("valueOf", 3),
                constant("pow", 4),
                constant("longValue", 5)),
            list("ldc2_w 0", "invokestatic 3", "iconst_1", "invokevirtual 4", "invokevirtual 5")),
        args(
            "x+10",
            INT,
            list(varInfo("x", "int", 1)),
            emptyList(),
            list("iload_1", "bipush 10", "iadd")),
        args(
            "x-110",
            INT,
            list(varInfo("x", "int", 2)),
            emptyList(),
            list("iload_2", "bipush 110", "isub")),
        args(
            "x*1200",
            INT,
            list(varInfo("x", "int", 3)),
            emptyList(),
            list("iload_3", "sipush 1200", "imul")),
        args(
            "x/13000",
            INT,
            list(varInfo("x", "int", 4)),
            emptyList(),
            list("iload 4", "sipush 13000", "idiv")),
        args(
            "x%14000",
            INT,
            list(varInfo("x", "int", 5)),
            emptyList(),
            list("iload 5", "sipush 14000", "irem")),
        args(
            "x**15000",
            INT,
            list(varInfo("x", "int", 6)),
            list(
                constant(15000L, 1),
                constant("java/math/BigInteger", 2),
                constant("valueOf", 3),
                constant("pow", 4),
                constant("longValue", 5)),
            list("ldc2_w 0", "invokestatic 3", "iload 6", "invokevirtual 4", "invokevirtual 5")),
        args("1+2", BYTE, emptyList(), emptyList(), list("iconst_1", "iconst_2", "iadd")),
        args("1-2", BYTE, emptyList(), emptyList(), list("iconst_1", "iconst_2", "isub")),
        args("1*2", BYTE, emptyList(), emptyList(), list("iconst_1", "iconst_2", "imul")),
        args("1/2", BYTE, emptyList(), emptyList(), list("iconst_1", "iconst_2", "idiv")),
        args("1%2", BYTE, emptyList(), emptyList(), list("iconst_1", "iconst_2", "irem")),
        args(
            "1**2",
            BYTE,
            emptyList(),
            list(
                constant(2L, 1),
                constant("java/math/BigInteger", 2),
                constant("valueOf", 3),
                constant("pow", 4),
                constant("longValue", 5)),
            list("ldc2_w 0", "invokestatic 3", "iconst_1", "invokevirtual 4", "invokevirtual 5")),
        args(
            "x+10",
            BYTE,
            list(varInfo("x", "int", 1)),
            emptyList(),
            list("iload_1", "bipush 10", "iadd")),
        args(
            "x-110",
            BYTE,
            list(varInfo("x", "int", 2)),
            emptyList(),
            list("iload_2", "bipush 110", "isub")),
        args(
            "x*120",
            BYTE,
            list(varInfo("x", "int", 3)),
            emptyList(),
            list("iload_3", "bipush 120", "imul")),
        args(
            "x/121",
            BYTE,
            list(varInfo("x", "int", 4)),
            emptyList(),
            list("iload 4", "bipush 121", "idiv")),
        args(
            "x%122",
            BYTE, list(varInfo("x", "int", 5)), emptyList(), list("iload 5", "bipush 122", "irem")),
        args(
            "x**123",
            BYTE,
            list(varInfo("x", "int", 6)),
            list(
                constant(123L, 1),
                constant("java/math/BigInteger", 2),
                constant("valueOf", 3),
                constant("pow", 4),
                constant("longValue", 5)),
            list("ldc2_w 0", "invokestatic 3", "iload 6", "invokevirtual 4", "invokevirtual 5")),
        args(
            "√2",
            DOUBLE,
            emptyList(),
            list(constant(Math.sqrt(2), 1), constant("java/math/Math", 2), constant("sqrt", 3)),
            list("ldc2_w 0")),
        args(
            "√x",
            DOUBLE,
            list(varInfo("x", "int", 100)),
            list(constant("java/lang/Math", 1), constant("sqrt", 2)),
            list("iload 100", "i2d", "invokestatic 2")));
  }

  @BeforeAll
  public void init() {
    method =
        new Method(
            "pkg.Clazz",
            list(ACC_PUBLIC),
            "void",
            new Identifier("testMethod"),
            emptyList(),
            emptyList());
    ctx = new CGContext();
    ctx.init(
        new CompilerErrors(),
        0,
        list(
            clazz(
                "pkg",
                emptyList(),
                new Scope(Scopes.PUBLIC),
                "Clazz",
                emptyList(),
                Clazz.JAVA_LANG_OBJECT,
                list(method),
                emptyList())));
    target.ctx = ctx;
    ParserContext parserCtx = new ParserContext();
    parserCtx.arrayInitParser = new ArrayInitParser(parserCtx);
    parserCtx.arraySelectorParser = new ArraySelectorParser(parserCtx);
    parserCtx.castParser = new CastParser();
    parserCtx.methodCallParser = new MethodCallParser(parserCtx);
    parserCtx.floorParser = new FloorParser(parserCtx);
    parserCtx.sqrtParser = new SqrtParser();
    parserCtx.constrCallParser = new ConstructorCallParser(parserCtx);
    parserCtx.exprParser = new ExpressionParser(parserCtx);
    parser = new ExpressionParser(parserCtx);
  }

  @BeforeEach
  public void reset() {
    out = new DynamicByteArray();
  }

  @ParameterizedTest
  @MethodSource
  public void test(
      String input,
      String type,
      List<VariableInfo> varInfos,
      List<Constant> constants,
      List<String> expectedLines)
      throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    ctx.constPool = new MockConstantPool(ctx, constants);
    target.eval(out, varMap(varInfos), type, parser.parse(tokens));
    assertFalse(ctx.errors.count() > 0, "Compilation errors " + ctx.errors);
    assertEquals(String.join("\n", expectedLines), BytecodeDecompiler.decompile(out.getBytes()));
  }
}
