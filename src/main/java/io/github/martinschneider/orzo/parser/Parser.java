package io.github.martinschneider.orzo.parser;

import io.github.martinschneider.orzo.error.CompilerErrors;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.parser.productions.Clazz;

public class Parser {
  public ParserContext ctx;

  public Parser(CompilerErrors errors) {
    ctx = ParserContext.build(errors);
  }

  public Clazz parse(TokenList tokens) {
    return new ClassParser(ctx).parse(tokens);
  }
}
