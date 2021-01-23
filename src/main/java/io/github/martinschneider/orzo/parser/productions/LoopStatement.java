package io.github.martinschneider.orzo.parser.productions;

import java.util.List;

public class LoopStatement implements Statement {
  public List<Statement> body;
  public Expression cond;
}
