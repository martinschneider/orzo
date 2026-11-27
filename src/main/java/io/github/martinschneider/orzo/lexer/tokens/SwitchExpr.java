package io.github.martinschneider.orzo.lexer.tokens;

import io.github.martinschneider.orzo.parser.productions.Expression;
import io.github.martinschneider.orzo.parser.productions.Statement;
import java.util.List;

/**
 * A switch expression, e.g. "switch (x) { case 1 -> "one"; default -> { yield "many"; } }": the
 * switch statement, labeled with the name of a temporary variable, that assigns each result to it
 * and leaves the labeled statement ("yield v;" becomes "$switchN = v; break $switchN;").
 */
public class SwitchExpr extends Token {
  // the temporary variable (and the label of the statement)
  public String var;
  public Statement statement;
  // the results (for the type of the switch expression)
  public List<Expression> results;

  public SwitchExpr(String var, Statement statement, List<Expression> results) {
    super(var);
    this.var = var;
    this.statement = statement;
    this.results = results;
  }

  @Override
  public String toString() {
    return "switch " + var;
  }
}
