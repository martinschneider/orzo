package io.github.martinschneider.kommpeiler.codegen.statement;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionResult;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.parser.productions.Assignment;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.Map;

public class AssignmentGenerator implements StatementGenerator {
  private ExpressionGenerator exprGenerator;
  private OpsCodeGenerator opsGenerator;

  public AssignmentGenerator(ExpressionGenerator exprGenerator, OpsCodeGenerator opsGenerator) {
    this.exprGenerator = exprGenerator;
    this.opsGenerator = opsGenerator;
  }

  @Override
  public HasOutput generate(
      Map<Identifier, Integer> variables,
      DynamicByteArray out,
      Statement stmt,
      Method method,
      Clazz clazz) {
    Assignment assignment = (Assignment) stmt;
    // TODO: store type in variable map
    // TODO: type conversion
    ExpressionResult result = exprGenerator.eval(out, variables, assignment.getRight());
    opsGenerator.assignValue(out, variables, result.getType(), assignment.getLeft());
    return out;
  }
}
