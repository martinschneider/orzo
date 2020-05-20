package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.ExpressionCodeGenerator;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.OpsCodeGenerator;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.ParallelAssignment;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.List;
import java.util.Map;

public class ParallelAssignmentGenerator implements StatementGenerator {
  private ExpressionCodeGenerator expressionCodeGenerator;
  private OpsCodeGenerator opsCodeGenerator;

  public ParallelAssignmentGenerator(
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
    ParallelAssignment assignment = (ParallelAssignment) statement;
    int tmpCount = 0;
    for (int i = 0; i < assignment.getLeft().size(); i++) {
      Identifier left = assignment.getLeft().get(i);
      Expression right = assignment.getRight().get(i);
      byte leftIdx = variables.get(left).byteValue();
      expressionCodeGenerator.evaluateExpression(out, variables, right);
      if (replaceIds(assignment.getRight(), left, tmpCount)) {
        replaceIds(assignment.getRight(), left, tmpCount);
        byte tmpIdx =
            variables.computeIfAbsent(id("tmp_" + tmpCount), x -> variables.size()).byteValue();
        opsCodeGenerator.loadInteger(out, leftIdx);
        opsCodeGenerator.storeInteger(out, tmpIdx);
        tmpCount++;
      }
      opsCodeGenerator.storeInteger(out, leftIdx);
    }
    return out;
  }

  private boolean replaceIds(List<Expression> expressions, Identifier id, int idx) {
    Identifier tmpId = new Identifier("tmp_" + idx);
    boolean retValue = false;
    for (Expression expression : expressions) {
      expression.getInfix().replaceAll(x -> (x.eq(id)) ? tmpId : x);
      if (expression.getInfix().contains(tmpId)) {
        retValue = true;
      }
    }
    return retValue;
  }
}
