package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableMap;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;

public class AssignmentGenerator implements StatementGenerator {
  private CGContext context;

  public AssignmentGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out, VariableMap variables, Method method, Statement stmt) {
    Assignment assignment = (Assignment) stmt;
    String type = variables.get(assignment.getLeft()).getType();
    context.exprGenerator.eval(out, variables, type, assignment.getRight());
    context.opsGenerator.assignValue(out, variables, type, assignment.getLeft());
    return out;
  }
}
