package io.github.martinschneider.kommpeiler.parser.productions;

import java.util.List;

public interface LoopStatement extends Statement {
  List<Statement> getBody();

  Condition getCondition();
}
