package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class AssignmentGenerator implements StatementGenerator {
  private CGContext context;

  public AssignmentGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out,
      Map<Identifier, VariableInfo> variables,
      Method method,
      Statement stmt) {
    Assignment assignment = (Assignment) stmt;
    // TODO: store type in variable map
    // TODO: type conversion
    context.exprGenerator.eval(out, variables, assignment.getRight());
    String type = variables.get(assignment.getLeft()).getType();
    context.opsGenerator.assignValue(out, variables, type, assignment.getLeft());
    return out;
  }
}
