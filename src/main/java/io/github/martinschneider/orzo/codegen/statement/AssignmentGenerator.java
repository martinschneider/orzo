package io.github.martinschneider.orzo.codegen.statement;

import io.github.martinschneider.orzo.codegen.CGContext;
import io.github.martinschneider.orzo.codegen.DynamicByteArray;
import io.github.martinschneider.orzo.codegen.HasOutput;
import io.github.martinschneider.orzo.codegen.VariableMap;
import io.github.martinschneider.orzo.lexer.tokens.Identifier;
import io.github.martinschneider.orzo.parser.productions.Assignment;
import io.github.martinschneider.orzo.parser.productions.Method;
import io.github.martinschneider.orzo.parser.productions.Statement;

public class AssignmentGenerator implements StatementGenerator {
  private CGContext context;

  public AssignmentGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    Assignment assignment = (Assignment) stmt;
    Identifier id = assignment.getLeft();
    if (id.getSelector() != null) {
      context.opsGenerator.assignInArray(out, variables, id, assignment.getRight());
    } else {
      String type = variables.get(id).getType();
      context.exprGenerator.eval(out, variables, type, assignment.getRight());
      context.opsGenerator.assign(out, variables, type, id);
    }
    return out;
  }
}
