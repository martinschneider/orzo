package io.github.martinschneider.orzo;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.TokenList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

public class TestHelper {
  public static void assertTokenIdx(TokenList tokens, boolean resetIdx) {
    if (resetIdx) {
      Assertions.assertEquals(0, tokens.idx());
    } else {
      Assertions.assertEquals(tokens.size(), tokens.idx());
    }
  }

  /**
   * @see io.github.martinschneider.orzo.parser.ProdParser *
   */
  public static void assertTokenIdx(TokenList tokens, CompilerErrors errors, boolean resetIdx) {
    if (!errors.getErrors().isEmpty()) {
      // TODO: define and test error scenarios
      return;
    }
    assertTokenIdx(tokens, resetIdx);
  }

  @SafeVarargs
  public static Arguments args(Object... e) {
    return Arguments.of(e);
  }
}
