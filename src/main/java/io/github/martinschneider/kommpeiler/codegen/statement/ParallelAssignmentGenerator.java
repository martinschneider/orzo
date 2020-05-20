package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;

import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.parser.productions.Clazz;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.ParallelAssignment;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.List;
import java.util.Map;

public class ParallelAssignmentGenerator implements StatementGenerator {
  private ExpressionGenerator exprGenerator;
  private OpsCodeGenerator opsGenerator;

  public ParallelAssignmentGenerator(
      ExpressionGenerator exprGenerator, OpsCodeGenerator opsGenerator) {
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
    ParallelAssignment assignment = (ParallelAssignment) stmt;
    int tmpCount = 0;
    for (int i = 0; i < assignment.getLeft().size(); i++) {
      Identifier left = assignment.getLeft().get(i);
      Expression right = assignment.getRight().get(i);
      byte leftIdx = variables.get(left).byteValue();
      exprGenerator.eval(out, variables, right);
      if (replaceIds(assignment.getRight(), left, tmpCount)) {
        replaceIds(assignment.getRight(), left, tmpCount);
        byte tmpIdx =
            variables.computeIfAbsent(id("tmp_" + tmpCount), x -> variables.size()).byteValue();
        opsGenerator.loadInteger(out, leftIdx);
        opsGenerator.storeInteger(out, tmpIdx);
        tmpCount++;
      }
      opsGenerator.storeInteger(out, leftIdx);
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
