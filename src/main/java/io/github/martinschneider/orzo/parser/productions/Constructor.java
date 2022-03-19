package io.github.martinschneider.orzo.parser.productions;

import static io.github.martinschneider.orzo.lexer.tokens.Type.VOID;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import java.util.List;

public class Constructor extends Method {

  public Constructor(
      String fqClassName,
      List<AccessFlag> accFlags,
      Identifier name,
      List<Argument> arguments,
      List<Statement> body) {
    super(fqClassName, accFlags, VOID, name, arguments, body);
  }
}
