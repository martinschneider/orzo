package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbol.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.TokenType.SYMBOL;
import static io.github.martinschneider.orzo.util.Factory.id;
import static io.github.martinschneider.orzo.util.Factory.list;

import java.util.ArrayList;
import java.util.List;

import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Scope;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;

public class EnumParser implements ProdParser<ParallelDeclaration> {

  public String fqn;

  public ParallelDeclaration parse(TokenList tokens) {
    List<Declaration> decls = new ArrayList<>();
    if (tokens.curr().isId()) {
      decls.add(
          new Declaration(
              list(Scope.PUBLIC.accFlag),
              TypeUtils.descr(fqn),
              id(tokens.curr().toString()),
              null));
      while (tokens.curr().eq(SYMBOL, COMMA)) {
        if (tokens.next().isId()) {
          decls.add(
              new Declaration(
                  list(Scope.PUBLIC.accFlag),
                  TypeUtils.descr(fqn),
                  id(tokens.curr().toString()),
                  null));
        }
      }
    }
    return new ParallelDeclaration(decls);
  }
}
