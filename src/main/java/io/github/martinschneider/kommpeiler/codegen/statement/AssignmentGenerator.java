package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.ExpressionResult;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.OpsCodeGenerator;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class AssignmentGenerator implements StatementGenerator {
  private ExpressionCodeGenerator expressionCodeGenerator;
  private OpsCodeGenerator opsCodeGenerator;

  public AssignmentGenerator(
      ExpressionCodeGenerator expressionCodeGenerator, OpsCodeGenerator opsCodeGenerator) {
    this.expressionCodeGenerator = expressionCodeGenerator;
    this.opsCodeGenerator = opsCodeGenerator;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement statement,
      Method method,
      Clazz clazz) {
    Assignment assignment = (Assignment) statement;
    // TODO: store type in variable map
    // TODO: type conversion
    ExpressionResult result =
        expressionCodeGenerator.evaluateExpression(out, variables, assignment.getRight());
    opsCodeGenerator.assignValue(out, variables, result.getType(), assignment.getLeft());
    return out;
  }
}
