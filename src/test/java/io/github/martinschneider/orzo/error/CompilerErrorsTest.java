package io.github.martinschneider.orzo.error;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.martinschneider.orzo.lexer.Lexer;
import io.github.martinschneider.orzo.lexer.TokenList;
import org.junit.jupiter.api.Test;

public class CompilerErrorsTest {
  @Test
  public void reportShowsTheSourceLineAndColumn() {
    CompilerErrors errors = new CompilerErrors();
    errors.file = "A.java";
    TokenList tokens = new Lexer(errors).getTokens("class A {\n\tint x = ;\n}\n");
    tokens.setIdx(6); // the ";"
    errors.addError(tokens.curr(), "test", "expected an expression", null);
    assertEquals(
        "A.java:2:10: error: expected an expression\n"
            + "\tint x = ;\n"
            + "\t        ^\n"
            + "1 error\n",
        errors.toString());
  }
}
