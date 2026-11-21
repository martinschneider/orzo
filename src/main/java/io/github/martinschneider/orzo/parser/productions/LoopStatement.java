package io.github.martinschneider.orzo.parser.productions;

import java.util.List;

public class LoopStatement implements Statement {
  // the loop's label (e.g. "outer" in "outer: for (...)"), null if it has none
  public String label;

  public List<Statement> body;
  public Expression cond;
}
