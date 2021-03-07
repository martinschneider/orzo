package io.github.martinschneider.orzo.parser.productions;

import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import java.util.List;

public class Constructor extends Method {

  public Constructor(
      String fqClassName,
      List<AccessFlag> accFlags,
      String type,
      Identifier name,
      List<Argument> arguments,
      List<Statement> body) {
    super(fqClassName, accFlags, type, name, arguments, body);
  }
}
