package io.github.martinschneider.orzo.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import java.io.IOException;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

// The expression trees built by the ExpressionParser (see ExpressionParserTest for their postfix
// form)
public class ExpressionTreeTest {
  private static Stream<Arguments> test() {
    return Stream.of(
        Arguments.of("a + b * c", "PLUS(a, TIMES(b, c))"),
        Arguments.of("a - b - c", "MINUS(MINUS(a, b), c)"),
        Arguments.of("a = b = c", "ASSIGN(a, ASSIGN(b, c))"),
        Arguments.of("a ** b ** c", "POW(POW(a, b), c)"),
        Arguments.of("-a * b", "TIMES(MINUS(a), b)"),
        Arguments.of("!a && b || c", "LOGICAL_OR(LOGICAL_AND(NEGATE(a), b), c)"),
        Arguments.of("a < b == c", "EQUAL(LESS(a, b), c)"),
        Arguments.of("i++ + ++j", "PLUS(POST_INCREMENT(i), PRE_INCREMENT(j))"),
        // (lexed as a prefix "++")
        Arguments.of("(i)++", "POST_INCREMENT(i)"),
        Arguments.of("c ? a : b ? x : y", "(c ? a : (b ? x : y))"),
        Arguments.of("x instanceof String && y", "LOGICAL_AND((x instanceof String), y)"),
        Arguments.of("a.b[i].f(x, 1).g()", "a.b[i].f(x, INT(1)).g()"),
        Arguments.of("new Foo(1).bar", "new Foo(INT(1)).bar"),
        Arguments.of("a + (int) b * c", "PLUS(a, TIMES(((int) b), c))"),
        Arguments.of("(int) a + b", "PLUS(((int) a), b)"),
        Arguments.of("(MAX) - 1", "MINUS(MAX, INT(1))"),
        Arguments.of("((String) o).length()", "((java.lang.String) o).length()"),
        Arguments.of("√x + ⌊y⌋", "PLUS(Math.sqrt(x), Math.round(Math.floor(y)))"),
        Arguments.of("int[].class", "int[].class"),
        // adjacent String literals are concatenated
        Arguments.of("\"a\" + \"b\" + \"c\"", "STR(abc)"),
        Arguments.of("s + \"a\" + \"b\" + t + \"c\"", "PLUS(PLUS(PLUS(s, STR(ab)), t), STR(c))"),
        Arguments.of("1 + 2 + \"a\"", "PLUS(PLUS(INT(1), INT(2)), STR(a))"),
        // (not modelled by the tree yet)
        Arguments.of("f(Math::min)", "f([Math::min])"));
  }

  @ParameterizedTest
  @MethodSource
  public void test(String input, String expected) throws IOException {
    TokenList tokens = new Lexer().getTokens(input);
    ExpressionParser parser = ParserContext.build(new CompilerErrors()).exprParser;
    assertEquals(expected, parser.parseTree(tokens).toString());
  }
}
