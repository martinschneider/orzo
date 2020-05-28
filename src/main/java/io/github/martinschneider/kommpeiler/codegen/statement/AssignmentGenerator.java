package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;

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
