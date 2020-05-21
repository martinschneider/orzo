package io.github.martinschneider.kommpeiler.codegen.statement;

import static io.github.martinschneider.kommpeiler.scanner.tokens.Token.id;

import io.github.martinschneider.kommpeiler.codegen.CGContext;
import io.github.martinschneider.kommpeiler.codegen.DynamicByteArray;
import io.github.martinschneider.kommpeiler.codegen.HasOutput;
import io.github.martinschneider.kommpeiler.codegen.VariableInfo;
import io.github.martinschneider.kommpeiler.parser.productions.Expression;
import io.github.martinschneider.kommpeiler.parser.productions.Method;
import io.github.martinschneider.kommpeiler.parser.productions.ParallelAssignment;
import io.github.martinschneider.kommpeiler.parser.productions.Statement;
import io.github.martinschneider.kommpeiler.scanner.tokens.Identifier;
import java.util.List;
import java.util.Map;

public class ParallelAssignmentGenerator implements StatementGenerator {
  private CGContext context;

  public ParallelAssignmentGenerator(CGContext context) {
    this.context = context;
  }

  @Override
  public HasOutput generate(
      DynamicByteArray out,
      Map<Identifier, VariableInfo> variables,
      Method method,
      Statement stmt) {
    ParallelAssignment assignment = (ParallelAssignment) stmt;
    int tmpCount = 0;
    for (int i = 0; i < assignment.getLeft().size(); i++) {
      Identifier left = assignment.getLeft().get(i);
      Expression right = assignment.getRight().get(i);
      byte leftIdx = variables.get(left).getIdx();
      context.exprGenerator.eval(out, variables, right);
      if (replaceIds(assignment.getRight(), left, tmpCount)) {
        replaceIds(assignment.getRight(), left, tmpCount);
        String name = "tmp_" + tmpCount;
        String type = variables.get(left).getType();
        byte tmpIdx =
            variables
                .computeIfAbsent(
                    id(name), x -> new VariableInfo(name, type, (byte) variables.size()))
                .getIdx();
        context.opsGenerator.loadInteger(out, leftIdx);
        context.opsGenerator.storeInteger(out, tmpIdx);
        tmpCount++;
      }
      context.opsGenerator.storeInteger(out, leftIdx);
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
