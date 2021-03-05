package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PRIVATE;
import static io.github.martinschneider.orzo.lexer.tokens.Scopes.PUBLIC;
import static io.github.martinschneider.orzo.lexer.tokens.Token.id;
import static io.github.martinschneider.orzo.lexer.tokens.Token.scope;
import static io.github.martinschneider.orzo.lexer.tokens.Token.type;
import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;
import static io.github.martinschneider.orzo.parser.TestHelper.args;
import static io.github.martinschneider.orzo.parser.TestHelper.assertTokenIdx;
import static io.github.martinschneider.orzo.parser.TestHelper.assign;
import static io.github.martinschneider.orzo.parser.TestHelper.clazz;
import static io.github.martinschneider.orzo.parser.TestHelper.list;
import static io.github.martinschneider.orzo.parser.TestHelper.method;
import static io.github.martinschneider.orzo.parser.TestHelper.stream;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Clazz;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ClassParserTest {
  private ClassParser target = new ClassParser(ParserContext.build(new CompilerErrors()));

  private static Stream<Arguments> testClass() throws IOException {
    return stream(
        args("", null),
        args(
            "public class Martin{public void test(){x=0;}}",
            clazz(
                "",
                emptyList(),
                scope(PUBLIC),
                "Martin",
                emptyList(),
                Clazz.JAVA_LANG_OBJECT,
                list(
                    method(
                        scope(PUBLIC),
                        type(VOID).toString(),
                        id("test"),
                        emptyList(),
                        list(assign("x=0")))),
                emptyList())),
        args(
            "private class Laura{}",
            clazz(
                "",
                emptyList(),
                scope(PRIVATE),
                "Laura",
                emptyList(),
                Clazz.JAVA_LANG_OBJECT,
                emptyList(),
                emptyList())),
        args(
            "class Empty{}",
            clazz(
                "",
                emptyList(),
                null,
                "Empty",
                emptyList(),
                Clazz.JAVA_LANG_OBJECT,
                emptyList(),
                emptyList())),
        args(
            "class Empty implements Interface1{}",
            clazz(
                "",
                emptyList(),
                null,
                "Empty",
                list("Interface1"),
                Clazz.JAVA_LANG_OBJECT,
                emptyList(),
                emptyList())),
        args(
            "class Empty implements Interface1, Interface2{}",
            clazz(
                "",
                emptyList(),
                null,
                "Empty",
                list("Interface1", "Interface2"),
                Clazz.JAVA_LANG_OBJECT,
                emptyList(),
                emptyList())),
        args(
            "class Empty implements Interface1, Interface2 extends BaseClass{}",
            clazz(
                "",
                emptyList(),
                null,
                "Empty",
                list("Interface1", "Interface2"),
                "BaseClass",
                emptyList(),
                emptyList())),
        args(
            "class Empty extends BaseClass implements Interface1, Interface2{}",
            clazz(
                "",
                emptyList(),
                null,
                "Empty",
                list("Interface1", "Interface2"),
                "BaseClass",
                emptyList(),
                emptyList())));
  }

  @ParameterizedTest
  @MethodSource
  public void testClass(String input, Clazz expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    assertEquals(expected, target.parse(tokens));
    assertTokenIdx(tokens, (expected == null));
  }
}
