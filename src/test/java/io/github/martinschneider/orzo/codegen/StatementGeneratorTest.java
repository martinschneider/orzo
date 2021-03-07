package io.github.martinschneider.orzo.codegen;

import static io.github.martinschneider.orzo.parser.TestHelper.clazz;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.varMap;
import static io.github.martinschneider.orzo.parser.productions.AccessFlag.ACC_PUBLIC;
import static io.github.martinschneider.orzo.util.decompiler.BytecodeDecompiler.decompile;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.github.martinschneider.orzo.codegen.generators.StatementGenerator;
import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.parser.ProdParser;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class StatementGeneratorTest<T extends Statement> {
  protected ProdParser<T> parser;
  protected StatementGenerator<T> target;
  protected Method method;
  protected DynamicByteArray out;
  protected CGContext ctx;

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
  }

  @BeforeEach
  public void reset() {
    out = new DynamicByteArray();
  }

  @ParameterizedTest
  @MethodSource
  public void test(
      String input,
      List<VariableInfo> varInfos,
      List<Constant> constants,
      List<String> expectedLines)
      throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    ctx.constPool = new MockConstantPool(ctx, constants);
    target.generate(out, varMap(varInfos), method, parser.parse(tokens));
    assertEquals(String.join("\n", expectedLines), decompile(out.getBytes()));
    assertFalse(ctx.errors.count() > 0, "Compilation errors " + ctx.errors);
  }
}
