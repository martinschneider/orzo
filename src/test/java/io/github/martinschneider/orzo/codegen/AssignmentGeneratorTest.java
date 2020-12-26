package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.clazz;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static io.github.martinschneider.orzo.parser.TestHelper.varInfo;
import static io.github.martinschneider.orzo.parser.TestHelper.varInfoArr;
import static io.github.martinschneider.orzo.parser.TestHelper.varMap;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.codegen.generators.AssignmentGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.parser.AssignmentParser;
import io.github.martinschneider.orzo.parser.ParserContext;
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
public class AssignmentGeneratorTest {
  private AssignmentParser parser;
  private AssignmentGenerator target;
  private Method method;
  private DynamicByteArray out;

  private static Stream<Arguments> test() throws IOException {
    return stream(
        args("x=5", list(varInfo("x", "int", 2)), list("iconst_5", "istore_2")),
        args("x=true", list(varInfo("x", "boolean", 1)), list("iconst_1", "istore_1")),
        args("x=false", list(varInfo("x", "int", 12)), list("iconst_0", "istore 12")),
        args(
            "x=5*12-3/6+12%4",
            list(varInfo("x", "int", 2)),
            list(
                "iconst_5",
                "bipush 12",
                "imul",
                "iconst_3",
                "bipush 6",
                "idiv",
                "isub",
                "bipush 12",
                "iconst_4",
                "irem",
                "iadd",
                "istore_2")),
        args(
            "x[1]=3",
            list(varInfoArr("x", "int", 2)),
            list("aload_2", "iconst_1", "iconst_3", "iastore")),
        args(
            "a[1],b=b,c",
            list(varInfoArr("a", "int", 2), varInfo("b", "int", 1), varInfo("c", "int", 0)),
            list("aload_2", "iconst_1", "iload_1", "iastore", "iload_0", "istore_1")),
        args(
            "a,c=b[1],d",
            list(
                varInfo("a", "int", 2),
                varInfoArr("b", "int", 7),
                varInfo("c", "int", 10),
                varInfo("d", "int", 3)),
            list("aload 7", "iconst_1", "iaload", "istore_2", "iload_3", "istore 10")),
        args(
            "array[left],array[right]=array[right],array[left]",
            list(
                varInfo("left", "int", 0),
                varInfo("right", "int", 1),
                varInfoArr("array", "int", 2)),
            list(
                "aload_2",
                "iload_0",
                "iaload",
                "istore_0",
                "aload_2",
                "iload_0",
                "aload_2",
                "iload_1",
                "iaload",
                "iastore",
                "aload_2",
                "iload_1",
                "iload_0",
                "iastore")),
        args(
            "a,b=b+1,a+1",
            list(varInfo("a", "int", 0), varInfo("b", "int", 1)),
            list(
                "iload_0",
                "istore_0",
                "iload_1",
                "iconst_1",
                "iadd",
                "istore_0",
                "iload_0",
                "iconst_1",
                "iadd",
                "istore_1")),
        args(
            "a,b,c=d,e,f",
            list(
                varInfo("a", "int", 1),
                varInfo("b", "int", 2),
                varInfo("c", "int", 3),
                varInfo("d", "int", 4),
                varInfo("e", "int", 5),
                varInfo("f", "int", 6)),
            list("iload 4", "istore_1", "iload 5", "istore_2", "iload 6", "istore_3"))
        // TODO: assert method call code generation
        // args("a,b=test(b),a", list(varInfo("a", "int", 1), varInfo("b", "int", 2)),
        // list("iconst_5", "istore_2")),
        // args("a,b,c=1+2*3/4%5>>6>>>7<<8,test(3*4),a+test(1/2)",
        // list(varInfo("a", "int", 1), varInfo("b", "int", 2), varInfo("c", "int", 3)),
        // list("iconst_5", "istore_2"))
        );
  }

  @BeforeAll
  public void init() {
    Method method =
        new Method(
            "pkg.Clazz",
            new Scope(Scopes.PUBLIC),
            "void",
            new Identifier("testMethod"),
            emptyList(),
            emptyList());
    CGContext ctx = new CGContext();
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
    target = new AssignmentGenerator(ctx);
    parser = new AssignmentParser(ParserContext.build(new CompilerErrors()));
  }

  @BeforeEach
  public void reset() {
    out = new DynamicByteArray();
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, List<VariableInfo> varInfos, List<String> expectedLines)
      throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    target.generate(out, varMap(varInfos), method, parser.parse(tokens));
    assertEquals(String.join("\n", expectedLines), BytecodeDecompiler.decompile(out.getBytes()));
  }
}
