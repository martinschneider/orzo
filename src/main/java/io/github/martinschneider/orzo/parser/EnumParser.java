package io.github.martinschneider.orzo.parser;

import static io.github.martinschneider.orzo.lexer.tokens.Symbols.COMMA;
import static io.github.martinschneider.orzo.lexer.tokens.Token.sym;
import static io.github.martinschneider.orzo.util.FactoryHelper.id;
import static io.github.martinschneider.orzo.util.FactoryHelper.list;

import io.github.martinschneider.orzo.codegen.TypeUtils;
import io.github.martinschneider.orzo.lexer.TokenList;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.lexer.tokens.Scopes;
import io.github.martinschneider.orzo.parser.productions.Declaration;
import io.github.martinschneider.orzo.parser.productions.ParallelDeclaration;
import java.util.ArrayList;
import java.util.List;

public class EnumParser implements ProdParser<ParallelDeclaration> {

  public String fqn;

  public ParallelDeclaration parse(TokenList tokens) {
    List<Declaration> decls = new ArrayList<>();
    if (tokens.curr() instanceof Identifier) {
      decls.add(
          new Declaration(
              list(Scopes.PUBLIC.accFlag),
              TypeUtils.descr(fqn),
              id(tokens.curr().toString()),
              null));
      while (tokens.next().eq(sym(COMMA))) {
        if (tokens.next() instanceof Identifier) {
          decls.add(
              new Declaration(
                  list(Scopes.PUBLIC.accFlag),
                  TypeUtils.descr(fqn),
                  id(tokens.curr().toString()),
                  null));
        }
      }
    }
    return new ParallelDeclaration(decls);
  }
}
